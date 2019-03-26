package grpcbridge.swagger.model;

import static java.lang.String.format;

import com.google.gson.annotations.SerializedName;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;

import grpcbridge.util.ProtoVisitor.SimpleFieldType;

/**
 * Base class for all Property definitions which make up a {@link SwaggerModel}. A property can be
 * a simple field, a reference to another model, or an array containing another property.
 */
public abstract class Property {
    protected final Type type;
    protected final @SerializedName("default") String defaultValue;
    protected final String description;

    public static SimpleProperty forSimpleField(FieldDescriptor field, SimpleFieldType type) {
        switch (type) {
            case INT:
                return SimpleProperty.create(Type.INTEGER, "int32");
            case LONG:
                return SimpleProperty.create(Type.INTEGER, "int64");
            case BOOL:
                return SimpleProperty.create(Type.BOOLEAN, "boolean");
            case DOUBLE:
                return SimpleProperty.create(Type.NUMBER, "double");
            case FLOAT:
                return SimpleProperty.create(Type.NUMBER, "float");
            case STRING:
                return SimpleProperty.create(Type.STRING, null);
            case BYTES:
                return SimpleProperty.create(Type.STRING, "byte");
            case ENUM:
                return SimpleProperty.forEnum(field.getEnumType());
            default:
                throw new IllegalArgumentException(
                    format("Unexpected field type: %s (%s)", type, field.getFullName())
                );
        }
    }

    public static Property forRepeated(Property nestedType) {
        return new RepeatedProperty(nestedType);
    }

    public static Property forReferenceTo(GenericDescriptor field) {
        return ReferenceProperty.create(field);
    }

    protected Property(Type type, String defaultValue, String description) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    enum Type {
        // https://tools.ietf.org/html/draft-zyp-json-schema-04#section-3.5
        ARRAY, BOOLEAN, INTEGER, NUMBER, NULL, OBJECT, STRING
    }
}
