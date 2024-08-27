package grpcbridge.util;

import com.google.protobuf.Message;
import grpcbridge.common.TestService;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileDescriptorsTest {
    @Test
    @SuppressWarnings("unchecked")
    public void noHttp() {
        FileDescriptors fileDescriptors = new FileDescriptors();
        fileDescriptors.addFile(grpcbridge.test.proto.Test.getDescriptor());

        ServerServiceDefinition service = new TestService().bindService();
        ServerMethodDefinition<?, ?> method = service
                .getMethods()
                .stream()
                .filter(m -> m.getMethodDescriptor().getBareMethodName().equals("NoHttpMethod"))
                .findFirst()
                .get();
        assertThat(fileDescriptors.routeFor(service, (ServerMethodDefinition<Message, Message>) method)).isEmpty();
    }
}
