package grpcbridge.parser;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;

public interface Parser {
    boolean accept(Collection<String> accepted);

    RpcMessage parse(HttpRequest httpRequest, Message.Builder builder);

    HttpResponse serialize(@Nonnull JsonFormat.Printer printer, @Nonnull RpcMessage message);

    Function<RpcMessage, HttpResponse> serializeAsync(JsonFormat.Printer printer);
}
