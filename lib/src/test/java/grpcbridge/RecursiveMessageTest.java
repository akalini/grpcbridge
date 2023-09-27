package grpcbridge;

import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.recursive.proto.RecursiveTestServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.junit.Test;

import static grpcbridge.http.HttpMethod.POST;
import static grpcbridge.test.recursive.proto.RecursiveTest.NestedLevel1;
import static grpcbridge.test.recursive.proto.RecursiveTest.NestedLevel2;
import static grpcbridge.test.recursive.proto.RecursiveTest.NestedLevel3;
import static grpcbridge.test.recursive.proto.RecursiveTest.RecursivePostRequest;
import static grpcbridge.test.recursive.proto.RecursiveTest.RecursivePostResponse;
import static grpcbridge.test.recursive.proto.RecursiveTest.getDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

public class RecursiveMessageTest implements ProtoParseTest {
    private final Bridge bridge = Bridge
            .builder()
            .addFile(getDescriptor())
            .addService(new RecursiveTestService().bindService())
            .build();

    @Test
    public void post() {
        RecursivePostRequest rpcRequest = RecursivePostRequest.newBuilder()
            .setNested(
                NestedLevel1.newBuilder()
                    .setNext(
                        NestedLevel2.newBuilder()
                            .setNext(
                                NestedLevel3.newBuilder()
                                    .setBackTo1(
                                         NestedLevel1.newBuilder()
                                             .setNext(NestedLevel2.getDefaultInstance())
                                             .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();

        HttpRequest request = HttpRequest
                .builder(POST, "/post")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        RecursivePostResponse rpcResponse = parse(response.getBody(), RecursivePostResponse.newBuilder());

        assertThat(rpcResponse.getNested()).isEqualTo(rpcRequest.getNested());
    }
}

final class RecursiveTestService extends RecursiveTestServiceGrpc.RecursiveTestServiceImplBase {
    @Override
    public void recursivePost(
            RecursivePostRequest request,
            StreamObserver<RecursivePostResponse> responseObserver) {
        responseObserver.onNext(
                RecursivePostResponse.newBuilder()
                        .setNested(request.getNested())
                        .build()
        );
        responseObserver.onCompleted();
    }
}
