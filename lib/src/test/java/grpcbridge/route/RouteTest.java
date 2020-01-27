package grpcbridge.route;

import static grpcbridge.http.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import grpcbridge.Bridge;
import grpcbridge.Exceptions.RouteNotFoundException;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.proto.RouteTest.Empty;
import grpcbridge.test.proto.RouteTestServiceGrpc.RouteTestServiceImplBase;
import io.grpc.stub.StreamObserver;

import org.junit.Test;

public class RouteTest {
    private Bridge bridge = Bridge
            .builder()
            .addFile(grpcbridge.test.proto.RouteTest.getDescriptor())
            .addService(new RouteTestService().bindService())
            .build();

    @Test
    public void match_success() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get")
                .build();

        HttpResponse response = bridge.handle(request);
        assertThat(response).isNotNull();
    }

    @Test
    public void match_additional() {
        HttpRequest request = HttpRequest
                .builder(GET, "/v1/get")
                .build();

        HttpResponse response = bridge.handle(request);
        assertThat(response).isNotNull();
    }

    @Test
    public void match_failure() {
        HttpRequest request = HttpRequest
                .builder(GET, "/v2/get")
                .build();

        assertThatExceptionOfType(RouteNotFoundException.class)
                .isThrownBy(() -> bridge.handle(request));
    }

    @Test
    public void match_no_additional() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get2")
                .build();

        HttpResponse response = bridge.handle(request);
        assertThat(response).isNotNull();
    }

    private static final class RouteTestService extends RouteTestServiceImplBase {
        @Override
        public void get(Empty request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }

        @Override
        public void getNoAdditional(Empty request, StreamObserver<Empty> responseObserver) {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }
}
