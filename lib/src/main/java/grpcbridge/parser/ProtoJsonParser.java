package grpcbridge.parser;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.lang.String.format;

/**
 * Helper methods to convert from JSON to protobuf messages and back.
 */
public final class ProtoJsonParser extends ProtoParser {

    public static final String JSON = "application/json";

    public static final ProtoJsonParser INSTANCE = new ProtoJsonParser();

    private ProtoJsonParser() {}

    @Override
    public RpcMessage parse(HttpRequest httpRequest, Message.Builder builder) {
        return new RpcMessage(
                parse(httpRequest.getBody().orElse(null), builder),
                httpRequest.getHeaders());
    }

    @SuppressWarnings("rawtypes")
    public <T extends Message> List<T> parseStream(
            @Nullable String body,
            T.Builder builder
    ) {
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
    @Override
    public <T extends Message> T parse(@Nullable String body, T.Builder builder) {
        if (!Strings.isNullOrEmpty(body)) {
            try {
                JsonFormat.parser().merge(body, builder);
            } catch (InvalidProtocolBufferException e) {
                throw new ParsingException(format("Failed to parse a message: {%s}", body), e);
            }
        }
        return (T) builder.build();
    }

    @Override
    public String serialize(@Nullable Integer index, @Nonnull JsonFormat.Printer printer,
                            @Nonnull Message message) {
        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new Exceptions.ParsingException(format("Failed to serialize a message: {%s}", message), e);
        }
    }

    @Override
    public Function<RpcMessage, HttpResponse> rpcToHttpTransformer(Route route) {
        return new RpcToJsonHttpMessage(route);
    }

    @Override
    protected String contentType() { return JSON; }

    @Override
    protected String packMultiple(List<String> serializedItems) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < serializedItems.size(); i++) {
            builder.append((i > 0 ? "," : ""));
            builder.append(serializedItems.get(i));
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Translates gRPC responses to the HTTP responses.
     */
    private class RpcToJsonHttpMessage implements Function<RpcMessage, HttpResponse> {
        private final Route route;

        RpcToJsonHttpMessage(Route route) {
            this.route = route;
        }

        @Override public HttpResponse apply(RpcMessage response) {
            return serialize(route.getPrinter(), response);
        }

        @Override public boolean equals(Object object) {
            return false;
        }
    }
}
