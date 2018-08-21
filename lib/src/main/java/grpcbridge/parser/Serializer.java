package grpcbridge.parser;

import com.google.common.net.MediaType;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

public interface Serializer {

    HttpResponse serialize(@Nonnull JsonFormat.Printer printer, @Nonnull RpcMessage message);

    Function<RpcMessage, HttpResponse> serializeAsync(JsonFormat.Printer printer);

    boolean supportsAny(Collection<MediaType> accepted);
}
