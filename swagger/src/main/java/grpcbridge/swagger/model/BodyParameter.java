package grpcbridge.swagger.model;

import com.google.protobuf.Descriptors.MethodDescriptor;

class BodyParameter extends Parameter {
    private final Property schema;

    private BodyParameter(Property schema) {
        super(null, null, "body", Location.BODY, true);
        this.schema = schema;
    }

    static BodyParameter referenceTo(MethodDescriptor methodDescriptor) {
        return new BodyParameter(ReferenceProperty.create(methodDescriptor.getInputType()));
    }

    static BodyParameter create(Property property) {
        return new BodyParameter(property);
    }
}
