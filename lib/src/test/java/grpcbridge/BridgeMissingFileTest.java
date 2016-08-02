package grpcbridge;

import grpcbridge.Exceptions.ConfigurationException;
import grpcbridge.common.TestService;
import org.junit.Test;

public class BridgeMissingFileTest {
    private TestService testService = new TestService();

    @Test(expected = ConfigurationException.class)
    public void create() {
        // Note that the proto definition file has not been added!
        Bridge.builder()
                .addService(testService.bindService())
                .build();
    }
}