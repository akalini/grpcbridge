package grpcbridge.util;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

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

    @SuppressWarnings("rawtypes")
    public static <T extends Message> List<T> parseStream(@Nullable String body, T.Builder builder) {
        List<T> messages = new ArrayList<>();
        if (!Strings.isNullOrEmpty(body)) {
            /** Parses the containing json which is not gRPC parsable
             * then splits them into individual json strings for each message so JsonFormat
             * can be used to parse them */
            Gson gson = new Gson();
            Map[] jsonMsgs = gson.fromJson(body, Map[].class);
            for (Map message : jsonMsgs) {
                messages.add(parse(gson.toJson(message), builder));
                builder.clear();
            }
        }
        return messages;
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

    public static HttpResponse serialize(@Nonnull RpcMessage message) {
        return serialize(false, message);
    }

    public static HttpResponse serialize(boolean preserveProtoFieldNames, @Nonnull RpcMessage message) {
        if (message.getMethodType().serverSendsOneMessage()) {
            String httpBody = !message.getBody().isEmpty() ? serialize(preserveProtoFieldNames,
                    message.getBody().get(0))
                    : "";
            return new HttpResponse(httpBody, message.getMetadata());
        } else {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < message.getBody().size(); i++) {
                builder.append((i > 0 ? "," : "") + serialize(preserveProtoFieldNames, message
                        .getBody().get(i)));
            }
            builder.append("]");
            return new HttpResponse(builder.toString(), message.getMetadata());
        }
    }

    public static String serialize(@Nonnull Message message) {
        return serialize(false, message);
    }

    public static String serialize(boolean preserveProtoFieldNames, @Nonnull Message message) {
        try {
            JsonFormat.Printer printer = JsonFormat.printer();
            if (preserveProtoFieldNames) {
                printer = printer.preservingProtoFieldNames();
            }
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new ParsingException(format("Failed to serialize a message: {%s}", message), e);
        }
    }
}
