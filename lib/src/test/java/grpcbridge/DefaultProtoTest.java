package grpcbridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.util.JsonFormat;
import org.junit.Test;
import grpcbridge.test.proto.Test.DefaultMessage;

import java.lang.reflect.Type;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultProtoTest implements ProtoParseTest {
    final Type type = new TypeToken<Map<String, String>>() {
    }.getType();
    final Gson gson = new Gson();

    @Override
    public JsonFormat.Printer printer() {
        return JsonFormat.printer().preservingProtoFieldNames();
    }

    @Test
    public void optionalBoolNotSetTest() {
        var message = DefaultMessage.newBuilder()
                .build();

        var rawBody = serialize(message);
        Map<String, String> bodyJson = gson.fromJson(rawBody, type);
        assertThat(bodyJson.get("optional_bool")).isNull();
        DefaultMessage parsed = parse(rawBody, DefaultMessage.newBuilder());
        assertThat(parsed.hasOptionalBool()).isFalse();
    }

    @Test
    public void optionalBoolFalseTest() {
        var message = DefaultMessage.newBuilder()
                .setOptionalBool(false)
                .build();

        var rawBody = serialize(message);
        Map<String, String> bodyJson = gson.fromJson(rawBody, type);
        assertThat(bodyJson.get("optional_bool")).isEqualTo("false");
        DefaultMessage parsed = parse(rawBody, DefaultMessage.newBuilder());
        assertThat(parsed.hasOptionalBool()).isTrue();
    }

    @Test
    public void optionalBoolTrueTest() {
        var message = DefaultMessage.newBuilder()
                .setOptionalBool(true)
                .build();

        var rawBody = serialize(message);
        Map<String, String> bodyJson = gson.fromJson(rawBody, type);
        assertThat(bodyJson.get("optional_bool")).isEqualTo("true");
        DefaultMessage parsed = parse(rawBody, DefaultMessage.newBuilder());
        assertThat(parsed.hasOptionalBool()).isTrue();
    }
}
