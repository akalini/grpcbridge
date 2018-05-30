package grpcbridge;

import static com.google.common.util.concurrent.Uninterruptibles.getUninterruptibly;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Message;

import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcCall;
import grpcbridge.rpc.RpcMessage;
import grpcbridge.util.ProtoJson;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

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
    private Channel forwardChannel;

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
     */
    Bridge(List<Route> routes) {
        this.routes = routes;
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
     * Finds the {@link RpcCall} associated with the given http request
     * @param httpRequest HTTP request
     * @return
     */
    private RpcCall getRpcCall(HttpRequest httpRequest) {
        for (Route route : routes) {
            Optional<RpcCall> call = route.match(httpRequest);
            if (call.isPresent()) {
                return call.get();
            }
        }
        throw new Exceptions.RouteNotFoundException(String.format(
                "Mapper not found: %s %s",
                httpRequest.getMethod(),
                httpRequest.getPath()));
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
        if (this.forwardChannel != null) {
            return proxy(httpRequest);
        } else {
            ListenableFuture<RpcMessage> response = this.getRpcCall(httpRequest).execute();
            return Futures.transform(response, new RpcToHttpMessage());
        }

    }
    
    /**
     * Handles incoming HTTP request asynchronously. The request is matched
     * against the set of available routes and if a route is found, the request
     * is transformed according to the gRPC service method annotations and
     * then sent as a client request to the {@link Bridge#forwardChannel}
     * @param httpRequest HTTP request
     * @return
     */
    public ListenableFuture<HttpResponse> proxy(HttpRequest httpRequest) {
        final SettableFuture<HttpResponse> listener = SettableFuture.create();
        RpcCall rpc = this.getRpcCall(httpRequest);
        ClientCall<Message, Message> call = this.forwardChannel.newCall(rpc.getMethod().getMethodDescriptor(), CallOptions.DEFAULT);
        call.start(new RpcListenerToHttpMessage(listener), httpRequest.getHeaders());
        call.sendMessage(rpc.getRequest().getBody());
        call.halfClose();
        call.request(1);
        
        return listener;
    }

    void setForwardChannel(Channel forwardChannel) {
        this.forwardChannel = forwardChannel;
    }

    /**
     * Translates gRPC responses to the HTTP responses.
     */
    private static class RpcToHttpMessage implements Function<RpcMessage, HttpResponse> {
        @Override public HttpResponse apply(RpcMessage response) {
            return ProtoJson.serialize(response);
        }

        @Override public boolean equals(Object object) {
            return false;
        }
    }
    
    /**
     * Listener to the response of an RPC {@link ClientCall}
     * This will record the message and then transform and send it to the http listener when 
     * the RPC connection is closed.
     *
     */
    private static class RpcListenerToHttpMessage extends Listener<Message> {
        private SettableFuture<HttpResponse> httpListener;
        private Message message;
        
        public RpcListenerToHttpMessage(SettableFuture<HttpResponse> httpListener) {
            this.httpListener = httpListener;
        }
        
        @Override
        public void onMessage(Message message) {
            if (this.message == null) {
                /** Only unary supported */
                this.message = message;
            }
        }
        
        @Override
        public void onClose(Status status, Metadata trailers) {
            if (status.isOk()) {
                this.httpListener.set(new RpcToHttpMessage().apply(new RpcMessage(this.message, trailers)));
            } else {
                this.httpListener.setException(new StatusRuntimeException(status, trailers));
            }
        }
    }
}
