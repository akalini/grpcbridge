package grpcbridge.parser;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions;
import grpcbridge.http.HttpResponse;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

public final class ProtoFormDataParser extends ProtoParser {

    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, String>>() {}.getType();
    private static final String URL_ENCODED_FORM = "application/x-www-form-urlencoded";
    public static final ProtoFormDataParser INSTANCE = new ProtoFormDataParser();

    private ProtoFormDataParser() {}

    @Override
    public String serialize(
        Integer index,
        JsonFormat.Printer printer,
        @Nonnull Message message
    ) {
        try {
            String json = printer.print(message);
            Map<String, String> msg = new Gson().fromJson(json, type);
            msg = msg.entrySet().parallelStream().map(it -> {
                try {
                    String key = index == null ? it.getKey()
                        : format("{%s}[%s]", it.getKey(), index);
                    return new AbstractMap.SimpleEntry<>(
                        key,
                        URLEncoder.encode(it.getValue(), "UTF-8")
                    );
                } catch (UnsupportedEncodingException e) {
                    throw new Exceptions.ParsingException(
                        format("Failed to serialize a message: {%s}", message), e
                    );
                }
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return Joiner.on('&').withKeyValueSeparator('=').join(msg);
        } catch (InvalidProtocolBufferException e) {
            throw new Exceptions.ParsingException(
                format("Failed to serialize a message: {%s}", message), e
            );
        }
    }

    @Override
    public Function<RpcMessage, HttpResponse> rpcToHttpTransformer(Route route) {
        return new RpcToUrlEncodedFormHttpMessage(route);
    }

    @Override
    protected String contentType() { return URL_ENCODED_FORM; }

    @Override
    protected String packMultiple(List<String> serializedItems) {
        return Joiner.on('&').join(serializedItems);
    }

    @Override
    public <T extends Message> T parse(@Nullable String body, Message.Builder builder) {
        if (Strings.isNullOrEmpty(body)) {
            return (T) builder.build();
        }
        Map<String, String> pairs =
            Splitter.on('&').withKeyValueSeparator('=').split(body).entrySet()
                .parallelStream()
                .map(it -> {
                    try {
                        return new AbstractMap.SimpleEntry<>(
                            it.getKey(),
                            URLDecoder.decode(it.getValue(), "UTF-8")
                        );
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        String json = gson.toJson(pairs);
        return ProtoJsonParser.INSTANCE.parse(json, builder);
    }

    /**
     * Translates gRPC responses to the HTTP responses.
     */
    private class RpcToUrlEncodedFormHttpMessage
        implements Function<RpcMessage, HttpResponse> {
        private final Route route;

        RpcToUrlEncodedFormHttpMessage(Route route) {
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
