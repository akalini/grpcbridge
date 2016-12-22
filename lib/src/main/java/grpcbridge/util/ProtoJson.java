package grpcbridge.util;

import static java.lang.String.format;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nullable;

/**
 * Helper methods to convert from JSON to protobuf messages and back.
 */
public final class ProtoJson {
    private ProtoJson() {}

    public static RpcMessage parse(HttpRequest httpRequest, Message.Builder builder) {
        return new RpcMessage(
                parse(httpRequest.getBody().orElse(null), builder),
                httpRequest.getHeaders());
    }

    public static RpcMessage parse(HttpResponse httpResponse, Message.Builder builder) {
        return new RpcMessage(parse(httpResponse.getBody(), builder), httpResponse.getTrailers());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T parse(@Nullable String body, T.Builder builder) {
        if (!Strings.isNullOrEmpty(body)) {
            try {
                JsonFormat.parser().merge(body, builder);
            } catch (InvalidProtocolBufferException e) {
                throw new ParsingException(format("Failed to parse a message: {%s}", body), e);
            }
        }
        return (T) builder.build();
    }

    public static HttpResponse serialize(RpcMessage message) {
        String body = serialize(message.getBody());
        return new HttpResponse(body, message.getMetadata());
    }

    public static String serialize(Message message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new ParsingException(format("Failed to serialize a message: {%s}", message), e);
        }
    }
}
