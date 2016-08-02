package grpcbridge.rpc;

import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Message;
import io.grpc.*;

/**
 * A helper class that is used to translate gRPC async {@link ServerCall} into
 * {@link SettableFuture} that is much easier to work with.
 */
final class AsyncCall extends ServerCall<Message, Message> {
    private Metadata headers;
    private SettableFuture<RpcMessage> delegate;

    public AsyncCall(SettableFuture<RpcMessage> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void request(int numMessages) {
    }

    @Override
    public void sendHeaders(Metadata headers) {
        this.headers = headers;
    }

    @Override
    public void sendMessage(Message message) {
        delegate.set(new RpcMessage(message, headers));
    }

    @Override
    public void close(Status status, Metadata trailers) {
        if (!status.isOk()) {
            delegate.setException(new StatusRuntimeException(status));
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public MethodDescriptor<Message, Message> getMethodDescriptor() {
        return null;
    }
}
