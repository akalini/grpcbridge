package grpcbridge.swagger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import grpcbridge.Bridge;
import grpcbridge.test.proto.TestServiceGrpc.TestServiceImplBase;
import java.io.IOException;
import org.junit.Test;

public class SwaggerManifestGeneratorTest {
    private Bridge bridge = Bridge
        .builder()
        .addFile(grpcbridge.test.proto.Test.getDescriptor())
        .addService(new TestServiceImplBase(){}.bindService())
        .build();

    @Test
    public void generateManifest() {
        String manifest = bridge.generateManifest(new SwaggerManifestGenerator());
        String expected = load("test-proto-swagger.json")
            .replaceAll("\n", "")
            .replaceAll(" ", "");
        assertThat(manifest).isEqualTo(expected);
    }

    private String load(String fileName) {
        try {
            return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
