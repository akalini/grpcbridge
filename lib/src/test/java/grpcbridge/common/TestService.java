package grpcbridge.common;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.grpc.Status.FAILED_PRECONDITION;

import com.google.protobuf.TextFormat;
import grpcbridge.test.proto.Test.DeleteRequest;
import grpcbridge.test.proto.Test.DeleteResponse;
import grpcbridge.test.proto.Test.GetRequest;
import grpcbridge.test.proto.Test.GetResponse;
import grpcbridge.test.proto.Test.GrpcErrorRequest;
import grpcbridge.test.proto.Test.GrpcErrorResponse;
import grpcbridge.test.proto.Test.PatchRequest;
import grpcbridge.test.proto.Test.PatchResponse;
import grpcbridge.test.proto.Test.PostRequest;
import grpcbridge.test.proto.Test.PostResponse;
import grpcbridge.test.proto.Test.PutRequest;
import grpcbridge.test.proto.Test.PutResponse;
import grpcbridge.test.proto.TestServiceGrpc;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test implementation of the service.
 */
public final class TestService extends TestServiceGrpc.TestServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    @Override
    public void get(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        logger.info("Get({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(GetResponse.newBuilder()
                .setStringField(request.getStringField())
                .setIntField(request.getIntField())
                .setLongField(request.getLongField())
                .setFloatField(request.getFloatField())
                .setDoubleField(request.getDoubleField())
                .setBoolField(request.getBoolField())
                .setEnumField(request.getEnumField())
                .setBytesField(request.getBytesField())
                .setNested(request.getNested())
                .addAllRepeatedField(request.getRepeatedFieldList())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getWithSuffix(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void getWithParams(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void getStatic(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void getMultipleParams(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void getNestedParams(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void getRepeatedParams(
            GetRequest request,
            StreamObserver<GetResponse> responseObserver) {
        get(request, responseObserver);
    }

    @Override
    public void post(
            PostRequest request,
            StreamObserver<PostResponse> responseObserver) {
        logger.info("Post({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(PostResponse.newBuilder()
                .setStringField(request.getStringField())
                .setIntField(request.getIntField())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void postCustomBody(
            PostRequest request,
            StreamObserver<PostResponse> responseObserver) {
        post(request, responseObserver);
    }

    @Override
    public void put(
            PutRequest request,
            StreamObserver<PutResponse> responseObserver) {
        logger.info("Put({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(PutResponse.newBuilder()
                .setStringField(request.getStringField())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(
            DeleteRequest request,
            StreamObserver<DeleteResponse> responseObserver) {
        logger.info("Delete({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(DeleteResponse.newBuilder()
                .setStringField(request.getStringField())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void patch(
            PatchRequest request,
            StreamObserver<PatchResponse> responseObserver) {
        logger.info("Patch({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(PatchResponse.newBuilder()
                .setStringField(request.getStringField())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void grpcError(
            GrpcErrorRequest request,
            StreamObserver<GrpcErrorResponse> responseObserver) {
        Metadata trailers = null;
        if (request.getAddMetadata()) {
            trailers = new Metadata();
            trailers.put(Metadata.Key.of("error-details", ASCII_STRING_MARSHALLER), "grpc error");
        }
        throw new StatusRuntimeException(
                FAILED_PRECONDITION.withDescription("Expected GRPC error"),
                trailers);
    }
}
