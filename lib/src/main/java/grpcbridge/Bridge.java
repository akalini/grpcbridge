package grpcbridge;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;
import static java.lang.String.format;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcCall;
import grpcbridge.rpc.RpcMessage;
import grpcbridge.parser.Parser;
import grpcbridge.parser.ProtoJsonParser;
import io.grpc.Metadata;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * HTTP to gPRC bridge implementation. The bridge is not HTTP library dependent.
 * The library relies on gRPC service method annotations that Google uses for
 * Google public APIs. The annotations describe how an HTTP request can be
 * translated into gRPC request. The supported format is described here:
 *
 * <a href="https://github.com/googleapis/googleapis/blob/master/google/api/annotations.proto">Annotations Proto</a>
 * <a href="https://github.com/googleapis/googleapis/blob/master/google/api/http.proto">HTTP Annotation</a>
 *
 * <p>
 * The {@link #handle(HttpRequest)} and {@link #handleAsync(HttpRequest)}
 * methods are meant to be invoked by the underlying HTTP layer and result in
 * the request being transformed and forwarded to the specified gPRC handlers.
 *
 * <p>
 * Use {@link BridgeBuilder} to create a {@link Bridge} instance as such:
 *
 * <pre>
 * {@code
 *   // Create a service implementation.
 *   // Bind it with an gRPC server and start the server.
 *   TestService testService = new TestService();
 *   Server rpcServer = ServerBuilder
 *       .forPort(9000)
 *       .addService(testService.bindService())
 *       .build();
 *   rpcServer.start();
 *
 *   // Create a new Bridge instance and bind it to the same implementation.
 *   Bridge bridge = Bridge.builder()
 *       .addFile(grpcbridge.test.proto.Test.getDescriptor())
 *       .addService(testService.bindService())
 *       .build();
 *
 *   // Now one can call {@link #handle} and {@link #handleAsync} methods
 *   // to bridge HTTP requests to the gRPC implementation.
 * }
 * </pre>
 *
 * See {@link BridgeBuilder} for more details.
 */
public final class Bridge {
    private final List<Route> routes;
    private final List<Parser> parsers;

    /**
     * Creates new {@link BridgeBuilder} that is used to setup a bridge.
     *
     * @return builder instance
     */
    public static BridgeBuilder builder() {
        return new BridgeBuilder();
    }

    /**
     * Creates a new bridge, use {@link BridgeBuilder} to create bridge
     * instances.
     *
     * @param routes list of available routes
     * @param parsers list of parsers used for deserializing and serializing
     */
    Bridge(List<Route> routes, List<Parser> parsers) {
        this.routes = routes;
        this.parsers = parsers;
        if (this.parsers.isEmpty()) {
            this.parsers.add(ProtoJsonParser.INSTANCE);
        }
    }

    /**
     * Creates a new bridge, use {@link BridgeBuilder} to create bridge
     * instances.
     *
     * @param routes list of available routes
     */
    Bridge(List<Route> routes) {
        this(routes, Arrays.asList(ProtoJsonParser.INSTANCE));
    }

    /**
     * Handles incoming HTTP request. The request is matched against the set of
     * available routes and if a route is found, the request is transformed
     * according to the gRPC service method annotations and forwarded to the
     * appropriate handler.
     *
     * @param httpRequest HTTP request
     * @return HTTP response
     */
    public HttpResponse handle(HttpRequest httpRequest) {
        try {
            return getUninterruptibly(handleAsync(httpRequest));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new Exceptions.BridgeException("Unhandled error", e);
            }
        }
    }

    /**
     * Handles incoming HTTP request asynchronously. The request is matched
     * against the set of available routes and if a route is found, the request
     * is transformed according to the gRPC service method annotations and
     * forwarded to the appropriate handler.
     *
     * @param httpRequest HTTP request
     * @return HTTP response future
     */
    public ListenableFuture<HttpResponse> handleAsync(HttpRequest httpRequest) {
        for (Route route : routes) {
            Optional<RpcCall> optionalCall = route.match(httpRequest);
            if (optionalCall.isPresent()) {
                RpcCall call = optionalCall.get();
                ListenableFuture<RpcMessage> response = call.execute();
                Iterable<String> acceptedTypes = httpRequest.getHeaders().getAll(Metadata.Key.of(
                    "accept",
                    Metadata.ASCII_STRING_MARSHALLER));
                final Parser parser;
                if (acceptedTypes == null) {
                    parser = ProtoJsonParser.INSTANCE;
                } else {
                    parser = parsers.stream()
                        .filter( it -> it.accept(acceptedTypes))
                        .findFirst()
                        .orElse(null);
                }
                if (parser == null) {
                    throw new Exceptions.BridgeException(
                        format(
                            "does not support any of the accepted content types: %s",
                            acceptedTypes
                        )
                    );
                }
                return Futures.transform(response, parser.rpcToHttpTransformer(route));
            }
        }

        throw new Exceptions.RouteNotFoundException(format(
                "Mapper not found: %s %s",
                httpRequest.getMethod(),
                httpRequest.getPath()));
    }
}
