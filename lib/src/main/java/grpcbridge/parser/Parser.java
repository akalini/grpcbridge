package grpcbridge.parser;

import com.google.common.base.Function;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Parser {

    Function<RpcMessage, HttpResponse> rpcToHttpTransformer(Route route);

    boolean accept(Iterable<String> accepted);

    RpcMessage parse(HttpRequest httpRequest, Message.Builder builder);


    String serialize(@Nonnull JsonFormat.Printer printer, @Nonnull Message message);
    HttpResponse serialize(@Nonnull JsonFormat.Printer printer, @Nonnull RpcMessage message);
    String serialize(
        @Nullable Integer index,
        @Nonnull JsonFormat.Printer printer,
        @Nonnull Message message
    );
}
