package grpcbridge.monitoring;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opencensus.contrib.http.util.HttpPropagationUtil.getCloudTraceFormat;

import com.google.common.util.concurrent.ListenableFuture;
import grpcbridge.http.HttpRequest;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;
import io.grpc.Metadata.Key;
import io.opencensus.trace.*;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OpenCensus tracer. Extracts an existing context from the request if present.
 * Executes an action within the new span.
 */
public final class Tracer {
    private static final Logger logger = Logger.getLogger(Tracer.class.getName());
    private static final Random RANDOM = new Random();

    public static void trace(Route route, HttpRequest request, Consumer<TracingSpan> action) {
        trace(route, request, span -> {
            action.accept(span);
            return null;
        });
    }

    public static <T> T trace(Route route, HttpRequest request, Function<TracingSpan, T> action) {
        TracingSpan span = newSpan(route, request);
        return action.apply(span);
    }

    public static void trace(
            Route route,
            HttpRequest request,
            ListenableFuture<RpcMessage> result) {
        newSpan(route, request).attachTo(result);
    }

    public static TracingSpan newSpan(Route route, HttpRequest request) {
        TextFormat formatter = getCloudTraceFormat();
        SpanContext context = SpanContext.INVALID;

        // If trace id header is not present exit early. This is because the
        // code below throws exception if the header is not present and we
        // don't want to have to catch an exception for the happy path.
        if (formatter.fields()
                .stream()
                .anyMatch(header -> request.getHeaders().containsKey(key(header)))) {
            try {
                context = formatter.extract(request, new TextFormat.Getter<HttpRequest>() {
                    @Override
                    public String get(HttpRequest carrier, String key) {
                        return formatter.fields().stream()
                                .map(header -> carrier.getHeaders().get(key(header)))
                                .filter(Objects::nonNull)
                                .findFirst()
                                .map(v -> {
                                    // The formatter requires span-id/trace-id format.
                                    if (v.contains("/")) {
                                        return v;
                                    } else {
                                        return v + "/0";
                                    }
                                })
                                .orElse(null);
                    }
                });

                if (context.isValid()) {
                    // Don't let the client force sampling; always reset trace options.
                    context = SpanContext.create(
                            context.getTraceId(),
                            context.getSpanId(),
                            TraceOptions.DEFAULT,
                            Tracestate.builder().build());
                } else if (context.getTraceId().isValid() && !context.getSpanId().isValid()) {
                    // Generate a new Span ID if the client provides an empty ID.
                    context = SpanContext.create(
                            context.getTraceId(),
                            SpanId.generateRandomId(RANDOM),
                            TraceOptions.DEFAULT,
                            Tracestate.builder().build());
                }
            } catch (SpanContextParseException e) {
                logger.log(Level.WARNING, e, () -> "Failed to parse tracing context");
            }
        }

        Span span = Tracing.getTracer()
                .spanBuilderWithRemoteParent("grpcbridge/http", context)
                .startSpan();
        span.putAttribute(
                "service",
                AttributeValue.stringAttributeValue(route.getService()));
        span.putAttribute(
                "method",
                AttributeValue.stringAttributeValue(route.getMethod()));
        return new TracingSpan(span);
    }

    private static Key<String> key(String header) {
        return Key.of(header, ASCII_STRING_MARSHALLER);
    }
}
