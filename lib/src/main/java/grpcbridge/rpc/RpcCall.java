package grpcbridge.rpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Message;
import io.grpc.ServerCall;
import io.grpc.ServerMethodDefinition;

/**
 * Abstracts away a gRPC method invocation. The invocation holds a pointer to
 * the underlying gRPC method implementation and the request protobuf for the
 * invocation.
 */
public final class RpcCall {
    private final ServerMethodDefinition<Message, Message> method;
    private RpcMessage request;

    /**
     * @param method gRPC method descriptor
     * @param request gRPC request
     */
    public RpcCall(
            ServerMethodDefinition<Message, Message> method,
            RpcMessage request) {
        this.method = method;
        this.request = request;
    }

    /**
     * Executes the gRPC request and returns response future.
     *
     * @return gRPC response future
     */
    public ListenableFuture<RpcMessage> execute() {
        SettableFuture<RpcMessage> result = SettableFuture.create();
        ServerCall.Listener<Message> listener = method
                .getServerCallHandler()
                .startCall(new AsyncCall(method.getMethodDescriptor(), result), request.getMetadata());

        listener.onMessage(request.getBody());
        listener.onHalfClose();

        return result;
    }
}
