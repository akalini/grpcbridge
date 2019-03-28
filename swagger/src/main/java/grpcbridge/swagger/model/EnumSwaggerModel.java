package grpcbridge.swagger.model;

import com.google.gson.annotations.SerializedName;
import com.google.protobuf.Descriptors.EnumDescriptor;

import grpcbridge.swagger.model.Property.Type;
import java.util.List;

public class EnumSwaggerModel extends SwaggerModel {
    private final @SerializedName("enum") List<String> enumValues;
    private final @SerializedName("default") String defaultValue;

    private EnumSwaggerModel(List<String> enumValues, String defaultValue) {
        super(Type.STRING, null);
        this.enumValues = enumValues;
        this.defaultValue = defaultValue;
    }

    public static EnumSwaggerModel create(EnumDescriptor field) {
        SimpleProperty enumProperty = SimpleProperty.forEnum(field);
        return new EnumSwaggerModel(enumProperty.enumValues, enumProperty.defaultValue);
    }

    @Override
    public Property getProperty(String name) {
        throw new UnsupportedOperationException("Cannot get property on enum model definition");
    }

    @Override
    public void putProperty(String name, Property property, boolean isRequired) {
        throw new UnsupportedOperationException("Cannot put property on enum model definition");
    }
}
