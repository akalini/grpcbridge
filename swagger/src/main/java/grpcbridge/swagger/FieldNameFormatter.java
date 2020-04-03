package grpcbridge.swagger;

import com.google.protobuf.Descriptors.FieldDescriptor;

public interface FieldNameFormatter {
    String nameFor(FieldDescriptor field);

    static FieldNameFormatter camelCase() {
        return FieldDescriptor::getJsonName;
    }

    static FieldNameFormatter snakeCase() {
        return FieldDescriptor::getName;
    }
}
