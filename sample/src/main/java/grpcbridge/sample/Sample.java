package grpcbridge.sample;

import com.google.protobuf.TextFormat;
import grpcbridge.sample.proto.SampleServiceGrpc.SampleServiceImplBase;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static grpcbridge.sample.proto.Sample.*;

/**
 * A sample gRPC service implementation. Echos requests back.
 */
final class Sample extends SampleServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(Sample.class);

    @Override
    public void post(PostRequest request, StreamObserver<PostResponse> responseObserver) {
        logger.info("Post({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(PostResponse.newBuilder()
                .setStringField(request.getStringField())
                .setIntField(request.getIntField())
                .setDoubleField(request.getDoubleField())
                .setBoolField(request.getBoolField())
                .setEnumField(request.getEnumField())
                .setNested(request.getNested())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        logger.info("Get({})", TextFormat.shortDebugString(request));
        responseObserver.onNext(GetResponse.newBuilder()
                .setStringField(request.getStringField())
                .setIntField(request.getIntField())
                .setDoubleField(request.getDoubleField())
                .setBoolField(request.getBoolField())
                .setEnumField(request.getEnumField())
                .setNested(request.getNested())
                .build());
        responseObserver.onCompleted();
    }
}

