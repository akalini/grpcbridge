package grpcbridge.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Message;
import io.grpc.*;

/**
 * Abstracts away a gRPC method invocation. The invocation holds a pointer to
 * the underlying gRPC method implementation and the request protobuf for the
 * invocation.
 */
public final class RpcCall {
    private final ServerCallHandler<Message, Message> handler;
    private RpcMessage request;

    /**
     * @param handler gRPC method handler, maps to the actual implementation
     * @param request gRPC request
     */
    public RpcCall(ServerCallHandler<Message, Message> handler, RpcMessage request) {
        this.handler = handler;
        this.request = request;
    }

    /**
     * Executes the gRPC request and returns response future.
     *
     * @return gRPC response future
     */
    public ListenableFuture<RpcMessage> execute() {
        SettableFuture<RpcMessage> result = SettableFuture.create();
        ServerCall.Listener<Message> listener = handler.startCall(new AsyncCall(result), request.getMetadata());

        listener.onMessage(request.getBody());
        listener.onHalfClose();

        return result;
    }
}
