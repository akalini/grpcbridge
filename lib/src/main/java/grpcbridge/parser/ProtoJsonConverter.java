package grpcbridge.parser;

import com.google.common.base.Strings;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions;
import grpcbridge.Exceptions.ParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Helper methods to convert from JSON to protobuf messages and back.
 */
public final class ProtoJsonConverter extends ProtoConverter {

    public static final ProtoJsonConverter INSTANCE = new ProtoJsonConverter();
    private static final MediaType contentType = MediaType.JSON_UTF_8;

    private ProtoJsonConverter() {
    }

    @SuppressWarnings("rawtypes")
    public <T extends Message> List<T> parseStream(
            @Nullable String body,
            Charset charset,
            T.Builder builder) {
        List<T> messages = new ArrayList<>();
        if (!Strings.isNullOrEmpty(body)) {
            /** Parses the containing json which is not gRPC parsable
             * then splits them into individual json strings for each message so JsonFormat
             * can be used to deserialize them */
            Gson gson = new Gson();
            Map[] jsonMsgs = gson.fromJson(body, Map[].class);
            for (Map message : jsonMsgs) {
                messages.add(parse(gson.toJson(message), charset, builder));
                builder.clear();
            }
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Message> T parse(@Nullable String body, Charset charset, T.Builder builder) {
        if (!Strings.isNullOrEmpty(body)) {
            try {
                JsonFormat.parser().merge(body, builder);
            } catch (InvalidProtocolBufferException e) {
                throw new ParsingException(format("Failed to deserialize a message: {%s}", body), e);
            }
        }
        return (T) builder.build();
    }

    @Override
    public String serialize(
            @Nullable Integer index,
            @Nonnull JsonFormat.Printer printer,
            @Nonnull Message message) {
        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new Exceptions.ParsingException(
                    format("Failed to serialize a message: {%s}", message), e
            );
        }
    }

    @Override
    protected MediaType contentType() {
        return contentType;
    }

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
}
