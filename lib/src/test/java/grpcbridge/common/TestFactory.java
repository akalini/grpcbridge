package grpcbridge.common;

import com.google.protobuf.ByteString;
import grpcbridge.test.proto.Test.*;

import static grpcbridge.test.proto.Test.Enum.VALID;

public final class TestFactory {
    public static GetRequest newGetRequest() {
        return GetRequest.newBuilder()
                .setStringField("string")
                .setIntField(123)
                .setLongField(321)
                .setFloatField(45.67f)
                .setDoubleField(89.999)
                .setBoolField(true)
                .setEnumField(VALID)
                .setBytesField(ByteString.copyFrom("bytes".getBytes()))
                .setNested(Nested.newBuilder()
                        .setNestedField("nested"))
                .build();

    }

    public static GetResponse responseFor(GetRequest request) {
        return GetResponse.newBuilder()
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
                .setDefault(request.getDefault())
                .build();
    }

    public static PostWrappersResponse responseFor(PostWrappersRequest request) {
        PostWrappersResponse.Builder builder = PostWrappersResponse.newBuilder();
        if (request.hasBoolValueField()) {
            builder.setBoolValueField(request.getBoolValueField());
        }
        if (request.hasBytesValueField()) {
            builder.setBytesValueField(request.getBytesValueField());
        }
        if (request.hasDoubleValueField()) {
            builder.setDoubleValueField(request.getDoubleValueField());
        }
        if (request.hasFloatValueField()) {
            builder.setFloatValueField(request.getFloatValueField());
        }
        if (request.hasStringValueField()) {
            builder.setStringValueField(request.getStringValueField());
        }
        if (request.hasInt64ValueField()) {
            builder.setInt64ValueField(request.getInt64ValueField());
        }
        if (request.hasUint64ValueField()) {
            builder.setUint64ValueField(request.getUint64ValueField());
        }
        if (request.hasInt32ValueField()) {
            builder.setInt32ValueField(request.getInt32ValueField());
        }
        if (request.hasUint32ValueField()) {
            builder.setUint32ValueField(request.getUint32ValueField());
        }
        return builder.build();
    }

    public static PostRequest newPostRequest() {
        return PostRequest.newBuilder()
                .setIntField(123)
                .build();

    }

    public static PostResponse responseFor(PostRequest request) {
        return PostResponse.newBuilder()
                .setStringField(request.getStringField())
                .setIntField(request.getIntField())
                .build();
    }

    public static PutRequest newPutRequest() {
        return PutRequest.newBuilder()
                .build();

    }

    public static PutResponse responseFor(PutRequest request) {
        return PutResponse.newBuilder()
                .setStringField(request.getStringField())
                .build();
    }

    public static DeleteRequest newDeleteRequest() {
        return DeleteRequest.newBuilder()
                .build();

    }

    public static DeleteResponse responseFor(DeleteRequest request) {
        return DeleteResponse.newBuilder()
                .setStringField(request.getStringField())
                .build();
    }

    public static PatchRequest newPatchRequest() {
        return PatchRequest.newBuilder()
                .build();

    }

    public static PatchResponse responseFor(PatchRequest request) {
        return PatchResponse.newBuilder()
                .setStringField(request.getStringField())
                .build();
    }

    public static GrpcErrorRequest newGrpcErrorRequest(boolean addMetadata) {
        return GrpcErrorRequest.newBuilder()
                .setAddMetadata(addMetadata)
                .build();
    }
}
