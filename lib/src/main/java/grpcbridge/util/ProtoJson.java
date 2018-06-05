package grpcbridge.util;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import grpcbridge.Exceptions.ParsingException;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;

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

    public static HttpResponse serialize(RpcMessage message) {
        String httpBody;
        if (message.getMethodType().serverSendsOneMessage()) {
            httpBody = !message.getBody().isEmpty() ? serialize(message.getBody().get(0)) : "";
        } else {
            List<String> messagesBody = new ArrayList<String>();
            for (Message msg : message.getBody()) {
                messagesBody.add(serialize(msg));
            }
            httpBody = "[" + Joiner.on(",").join(messagesBody) + "]";
        }
        return new HttpResponse(httpBody, message.getMetadata());
    }

    public static String serialize(Message message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new ParsingException(format("Failed to serialize a message: {%s}", message), e);
        }
    }
}
