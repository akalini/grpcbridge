package grpcbridge.swagger;

import static grpcbridge.swagger.BridgeSwaggerGeneratorTestSupport.formatJson;
import static grpcbridge.swagger.BridgeSwaggerGeneratorTestSupport.load;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.protobuf.Descriptors;
import grpcbridge.Bridge;
import grpcbridge.test.proto.PetStoreServiceGrpc.PetStoreServiceImplBase;
import grpcbridge.test.proto.PetstoreOpenapiV2;

import org.junit.Before;
import org.junit.Test;

public class PetstoreOpenapiV2Test {
    private Descriptors.FileDescriptor descriptor;
    private Bridge bridge;

    @Before
    public void setup() {
        descriptor = PetstoreOpenapiV2.getDescriptor();
        bridge = Bridge
                .builder()
                .addFile(descriptor)
                .addService(new PetStoreServiceImplBase(){}.bindService())
                .build();
    }

    @Test
    public void generateManifest_petstore_v2() {
        OpenapiV2.Swagger swaggerRoot = descriptor.getServices().stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("No service defied"))
                .getOptions()
                .getExtension(OpenapiV2.root);

        String manifest = bridge.generateManifest(BridgeSwaggerManifestGenerator
                .newBuilder()
                .setSwaggerRoot(swaggerRoot)
                .build());

        String result = formatJson(manifest);
        String expected = load("petstore-openapi_v2-swagger.json");
        assertThat(result).isEqualTo(expected);
    }
}
