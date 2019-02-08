package grpcbridge.monitoring;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opencensus.contrib.http.util.HttpPropagationUtil.getCloudTraceFormat;

import com.google.common.util.concurrent.ListenableFuture;
import grpcbridge.http.HttpRequest;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;
import io.grpc.Metadata.Key;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * OpenCensus tracer. Extracts an existing context from the request if present.
 * Executes an action within the new span.
 */
public final class Tracer {
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
                                .orElse(null);
                    }
                });
            } catch (SpanContextParseException ignored) {
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
