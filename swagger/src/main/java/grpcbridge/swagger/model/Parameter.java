package grpcbridge.swagger.model;

import com.google.gson.annotations.SerializedName;
import com.google.protobuf.Descriptors.FieldDescriptor;

import grpcbridge.swagger.model.Property.Type;
import grpcbridge.util.ProtoVisitor.SimpleFieldType;

/**
 * Base class for all Parameter definitions that make up a {@link SwaggerRoute}. A parameter can
 * be a body which points to another swagger model, a simple field in either the path or query, or
 * a repeated simple field in the query.
 */
public abstract class Parameter {
    private final String name;
    private final Location in;
    private final boolean required;
    private final Type type;
    private final @SerializedName("default") String defaultValue;

    public static Parameter forBody(Property property) {
        return BodyParameter.create(property);
    }

    public static Parameter forSimpleField(
        String name,
        Location location,
        FieldDescriptor simpleField,
        boolean required
    ) {
        return SimpleParameter.create(
            name,
            location,
            SimpleProperty.forSimpleField(simpleField, SimpleFieldType.fromDescriptor(simpleField)),
            required
        );
    }

    public static Parameter forRepeatedQuery(String name, FieldDescriptor simpleField) {
        return RepeatedQueryParameter.create(
            name,
            SimpleProperty.forSimpleField(simpleField, SimpleFieldType.fromDescriptor(simpleField))
        );
    }

    public String getName() {
        return name;
    }

    protected Parameter(
        Type type,
        String defaultValue,
        String name,
        Location in,
        boolean required
    ) {
        this.type = type;
        this.defaultValue = defaultValue;
        this.name = name;
        this.in = in;
        this.required = required;
    }

    public enum Location { PATH, QUERY, BODY }
}
