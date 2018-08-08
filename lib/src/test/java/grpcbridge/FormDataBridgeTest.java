package grpcbridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import grpcbridge.common.TestSnakeService;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.parser.ProtoFormDataConverter;
import grpcbridge.parser.ProtoJsonConverter;
import grpcbridge.test.proto.TestSnakeCase;
import io.grpc.Metadata;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static grpcbridge.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

public class FormDataBridgeTest implements ProtoParseTest {

    private TestSnakeService testService = new TestSnakeService();
    private Bridge bridge = Bridge
        .builder()
            .addFile(grpcbridge.test.proto.TestSnakeCase.getDescriptor())
            .addService(testService.bindService())
            .addSerializer(ProtoJsonConverter.INSTANCE)
            .addDeserializer(ProtoFormDataConverter.INSTANCE)
            .build();

    @Test
    public void handleFormDataRequest() {
        final Type type = new TypeToken<Map<String, String>>() {}.getType();
        final Gson gson = new Gson();

        Metadata headers = new Metadata();
        headers.put(
                Metadata.Key.of("content-type", Metadata.ASCII_STRING_MARSHALLER),
                "application/x-www-form-urlencoded"
        );

        String rawBody = "first_name=John&last_name=Doe";

        HttpRequest request = HttpRequest
            .builder(POST, "/snake/")
            .body(rawBody)
            .headers(headers)
            .build();

        HttpResponse response = bridge.handle(request);
        String raw = response.getBody();

        assertThat(response.getTrailers().get(Metadata.Key.of("content-type",
            Metadata.ASCII_STRING_MARSHALLER))).isEqualTo("application/json");

        Map<String, String> json = gson.fromJson(raw, type);
        assertThat(json.get("status_code")).isEqualTo("OK");

        TestSnakeCase.SnakeResponse rpcResponse = parse(raw,
            TestSnakeCase.SnakeResponse.newBuilder());
        assertThat(rpcResponse.getStatusCode()).isEqualTo("OK");
    }
}
