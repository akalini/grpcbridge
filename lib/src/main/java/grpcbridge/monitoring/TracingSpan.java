package grpcbridge.monitoring;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import grpcbridge.rpc.RpcMessage;
import java.util.function.Supplier;

import io.grpc.StatusRuntimeException;
import io.opencensus.common.Scope;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracing;

public class TracingSpan {
    private final Span span;

    protected TracingSpan(Span span) {
        this.span = span;
    }

    public <T> T callInContext(Supplier<T> action) {
        try (Scope scope = Tracing.getTracer().withSpan(span)) {
            return action.get();
        }
    }

    public void attachTo(ListenableFuture<RpcMessage> future) {
        Futures.addCallback(
                future,
                new FutureCallback<RpcMessage>() {
                    @Override
                    public void onSuccess(RpcMessage result) {
                        close(Status.OK);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        if (t instanceof StatusRuntimeException) {
                            close(((StatusRuntimeException)t).getStatus());
                        } else {
                            close(Status.UNKNOWN);
                        }
                    }
                },
                MoreExecutors.directExecutor());
    }

    public void close(io.grpc.Status status) {
        close(Status.CanonicalCode.valueOf(status.getCode().name())
                .toStatus()
                .withDescription(status.getDescription()));
    }

    public void close(Status status) {
        span.end(EndSpanOptions.builder()
                .setStatus(status)
                .build());
    }
}
