package grpcbridge.xml.common;

import grpcbridge.test.proto.TestXml;
import grpcbridge.test.proto.XmlTestServiceGrpc;
import io.grpc.stub.StreamObserver;

/**
 * Test Implementation for TestSnakeService
 */
public class TestXMLService extends XmlTestServiceGrpc.XmlTestServiceImplBase {
    @Override
    public void accountEvent(
        TestXml.XmlRequest request,
        StreamObserver<TestXml.AccountEvent> responseObserver
    ) {
        responseObserver.onNext(
                TestXml.AccountEvent
                        .newBuilder()
                        .setStatusCode(TestXml.Status.SUCCESS)
                    .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void tx(
        TestXml.XmlRequest request,
        StreamObserver<TestXml.Transaction> responseObserver
    ) {
        responseObserver.onNext(
            TestXml.Transaction
                .newBuilder()
                    .setStatusCode(TestXml.Status.SUCCESS)
                    .setDescription("test transaction")
                    .setHasMoney(true)
                    .setAmount(25.50)
                .build()
        );
        responseObserver.onCompleted();
    }
}
