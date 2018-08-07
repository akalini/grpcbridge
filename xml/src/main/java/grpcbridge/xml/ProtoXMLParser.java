package grpcbridge.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions;
import grpcbridge.http.HttpResponse;
import grpcbridge.parser.ProtoParser;
import grpcbridge.route.Route;
import grpcbridge.rpc.RpcMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public final class ProtoXMLParser extends ProtoParser {
    private static Type type = new TypeToken<Map<String, Object>>() {}.getType();

    private static final XmlMapper MAPPER = new XmlMapper();

    public static ProtoXMLParser INSTANCE = new ProtoXMLParser();

    private static final String TEXT_XML = "text/xml";

    private static List<String> SUPPORTED = Arrays.asList(
        TEXT_XML,
        "application/xml"
    );

    private ProtoXMLParser() {}

    @Override
    protected List<String> supportedTypes() {
        return SUPPORTED;
    }

    @Override
    protected String contentType() {
        return TEXT_XML;
    }

    @Override
    protected String packMultiple(List<String> serializedItems) {
        StringBuilder builder = new StringBuilder("<Array>");
        serializedItems.forEach(builder::append);
        builder.append("</Array>");
        return builder.toString();
    }

    @Override
    public Function<RpcMessage, HttpResponse> rpcToHttpTransformer(Route route) {
        return new RpcToXMLHttpMessage(route);
    }

    /**
     * Translates gRPC responses to the HTTP responses.
     */
    private class RpcToXMLHttpMessage
        implements Function<RpcMessage, HttpResponse> {
        private final Route route;

        RpcToXMLHttpMessage(Route route) {
            this.route = route;
        }

        @Override public HttpResponse apply(RpcMessage response) {
            return serialize(route.getPrinter(), response);
        }

        @Override public boolean equals(Object object) {
            return false;
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public   <T extends Message> T parse(@Nullable String body, T.Builder builder) {
        if (!Strings.isNullOrEmpty(body)) {
            try {
                XmlMapper mapper = new XmlMapper();
                Map msg = mapper.readValue(body, Map.class);
                Gson gson = new Gson();
                JsonFormat.parser().merge(gson.toJson(msg), builder);
            } catch (IOException e) {
                throw new Exceptions.ParsingException(format("Failed to parse a message: {%s}", body), e);
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
            String json = printer.print(message);
            Map<String, Object> raw = new Gson().fromJson(json, type);
            return MAPPER
                .writer()
                .withRootName(message.getDescriptorForType().getName())
                .writeValueAsString(raw);
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            throw new Exceptions.ParsingException(
                format("Failed to serialize a message: {%s}", message), e
            );
        }
    }
}
