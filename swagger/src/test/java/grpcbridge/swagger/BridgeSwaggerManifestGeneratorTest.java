package grpcbridge.swagger;

import static grpcbridge.test.proto.Test.testRequired;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import grpcbridge.Bridge;
import grpcbridge.test.proto.TestServiceGrpc.TestServiceImplBase;
import java.io.IOException;
import org.junit.Test;

public class BridgeSwaggerManifestGeneratorTest {
    private Bridge bridge = Bridge
        .builder()
        .addFile(grpcbridge.test.proto.Test.getDescriptor())
        .addService(new TestServiceImplBase(){}.bindService())
        .build();

    @Test
    public void generateManifest() {
        String manifest = bridge.generateManifest(
            BridgeSwaggerManifestGenerator.newBuilder()
                .addRequiredExtension(testRequired)
                .excludeDeprecated()
                .build()
        );
        String expected = load("test-proto-swagger.json");
        assertThat(formatJson(manifest)).isEqualTo(expected);
    }

    private String load(String fileName) {
        try {
            return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatJson(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }
}
