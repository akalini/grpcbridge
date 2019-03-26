package grpcbridge.swagger.model;

import com.google.gson.annotations.SerializedName;
import com.google.protobuf.Descriptors.GenericDescriptor;

class ReferenceProperty extends Property {
    private final @SerializedName("$ref") String ref;

    private ReferenceProperty(String ref) {
        super(null, null, null);
        this.ref = "#/definitions/" + ref;
    }

    static ReferenceProperty create(GenericDescriptor field) {
        return new ReferenceProperty(field.getFullName());
    }
}
