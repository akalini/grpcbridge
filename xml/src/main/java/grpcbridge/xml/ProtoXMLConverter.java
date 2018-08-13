package grpcbridge.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.Exceptions;
import grpcbridge.parser.ProtoConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public final class ProtoXMLConverter extends ProtoConverter {

    private static final XmlMapper MAPPER = new XmlMapper();
    private static final String TEXT_XML = "text/xml";
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();
    private static final Type LIST_TYPE = new TypeToken<List<Object>>() {
    }.getType();
    public static ProtoXMLConverter INSTANCE = new ProtoXMLConverter();
    private static List<String> SUPPORTED = Arrays.asList(
            TEXT_XML,
            "application/xml"
    );
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Map.class, new JsonDeserializer<Map<String, Object>>() {
                @Override
                public Map<String, Object> deserialize(JsonElement json,
                                                       Type typeOfT,
                                                       JsonDeserializationContext context
                ) throws JsonParseException {
                    Map<String, Object> m = new LinkedHashMap<>();
                    JsonObject jo = json.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> mx : jo.entrySet()) {
                        String key = mx.getKey();
                        JsonElement v = mx.getValue();
                        if (v.isJsonArray()) {
                            m.put(key, gson.fromJson(v, LIST_TYPE));
                        } else if (v.isJsonPrimitive()) {
                            Object value = null;
                            JsonPrimitive primitive = v.getAsJsonPrimitive();
                            if (primitive.isNumber()) {
                                try {
                                    value = NumberFormat.getInstance().parse(v.getAsString());
                                } catch (Exception ignored) {
                                }
                            }
                            if (value == null) {
                                value = gson.fromJson(v, Object.class);
                            }
                            m.put(key, value);
                        } else if (v.isJsonObject()) {
                            m.put(key, gson.fromJson(v, MAP_TYPE));
                        }

                    }
                    return m;
                }
            })
            .create();

    private ProtoXMLConverter() {
    }

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
                throw new Exceptions.ParsingException(
                        format("Failed to deserialize a message: {%s}", body), e
                );
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
            Map<String, Object> raw = gson.fromJson(json, MAP_TYPE);
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
