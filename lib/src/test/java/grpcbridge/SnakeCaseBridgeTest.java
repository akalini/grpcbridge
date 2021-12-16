package grpcbridge;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.common.TestSnakeService;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.proto.TestSnakeCase;
import grpcbridge.test.proto.TestSnakeCase.SnakeRequest;
import grpcbridge.test.proto.TestSnakeCase.SnakeResponse;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static grpcbridge.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

public class SnakeCaseBridgeTest implements ProtoParseTest {
    private TestSnakeService testService = new TestSnakeService();
    private Bridge bridge = Bridge
            .builder()
            .addFile(grpcbridge.test.proto.TestSnakeCase.getDescriptor())
            .addService(testService.bindService())
            .build();

    @Override
    public JsonFormat.Printer printer() {
        return JsonFormat.printer().preservingProtoFieldNames();
    }

    @Test
    public void preserveSnakeCaseTest() {
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        final Gson gson = new Gson();

        SnakeRequest rpcRequest = SnakeRequest.newBuilder()
                .setFirstName("John")
                .setLastName("Doe")
                .build();

        String rawBody = serialize(rpcRequest);
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
        assertThat(json.get("status_code")).isEqualTo("0");

        SnakeResponse rpcResponse = parse(raw, SnakeResponse.newBuilder());
        assertThat(rpcResponse.getStatusCode()).isEqualTo(TestSnakeCase.Status.SUCCESS);
    }
}
