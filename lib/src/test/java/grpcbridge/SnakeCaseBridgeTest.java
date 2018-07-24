package grpcbridge;

import static grpcbridge.http.HttpMethod.POST;
import static grpcbridge.util.ProtoJson.parse;
import static grpcbridge.util.ProtoJson.serialize;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.common.TestSnakeService;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.proto.TestSnakeCase.SnakeRequest;
import grpcbridge.test.proto.TestSnakeCase.SnakeResponse;

import java.lang.reflect.Type;
import java.util.Map;

import org.junit.Test;

public class SnakeCaseBridgeTest {
    private TestSnakeService testService = new TestSnakeService();
    private final JsonFormat.Printer printer = JsonFormat.printer().preservingProtoFieldNames();
    private Bridge bridge = Bridge
            .builder()
            .addFile(grpcbridge.test.proto.TestSnakeCase.getDescriptor())
            .addService(testService.bindService())
            .build();

    @Test
    public void preserveSnakeCaseTest() {
        final Type type = new TypeToken<Map<String, String>>() {}.getType();
        final Gson gson = new Gson();

        SnakeRequest rpcRequest = SnakeRequest.newBuilder()
                .setFirstName("John")
                .setLastName("Doe").build();

        String rawBody = serialize(printer, rpcRequest);
        Map<String, String> bodyJson = gson.fromJson(rawBody, type);
        assertThat(bodyJson.get("first_name")).isEqualTo("John");
        assertThat(bodyJson.get("last_name")).isEqualTo("Doe");

        HttpRequest request = HttpRequest
                .builder(POST, "/snake/")
                .body(rawBody)
                .build();

        HttpResponse response = bridge.handle(request);
        String raw = response.getBody();

        Map<String, String> json = new Gson().fromJson(raw, type);
        assertThat(json.get("status_code")).isEqualTo("OK");

        SnakeResponse rpcResponse = parse(raw, SnakeResponse.newBuilder());
        assertThat(rpcResponse.getStatusCode()).isEqualTo("OK");
    }
}
