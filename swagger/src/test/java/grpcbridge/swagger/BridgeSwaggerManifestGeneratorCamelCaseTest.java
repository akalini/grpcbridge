package grpcbridge.swagger;

import static grpcbridge.swagger.BridgeSwaggerGeneratorTestSupport.formatJson;
import static grpcbridge.swagger.BridgeSwaggerGeneratorTestSupport.load;
import static grpcbridge.test.proto.Test.testRequired;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import grpcbridge.Bridge;
import grpcbridge.test.proto.TestServiceGrpc;

import org.junit.Before;
import org.junit.Test;

public class BridgeSwaggerManifestGeneratorCamelCaseTest {
    private Bridge bridge;

    @Before
    public void setup() {
        bridge = Bridge
                .builder()
                .addFile(grpcbridge.test.proto.Test.getDescriptor())
                .addService(new TestServiceGrpc.TestServiceImplBase(){}.bindService())
                .build();
    }

    @Test
    public void generateManifest() {
        String manifest = bridge.generateManifest(
                BridgeSwaggerManifestGenerator.newBuilder()
                        .addRequiredExtension(testRequired)
                        .setFormatter(FieldNameFormatter.camelCase())
                        .excludeDeprecated()
                        .build()
        );
        String expected = load("test-proto-swagger-camel.json");
        assertThat(formatJson(manifest)).isEqualTo(expected);
    }
}
