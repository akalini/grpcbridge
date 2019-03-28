package grpcbridge.swagger.model;

import com.google.gson.annotations.SerializedName;

import grpcbridge.swagger.model.Property.Type;
import java.util.List;

class SimpleParameter extends Parameter {
    private final String format;
    private final @SerializedName("enum") List<String> enumValues;

    private SimpleParameter(
        Type type,
        String defaultValue,
        String name,
        Location in,
        boolean required,
        String format,
        List<String> enumValues
    ) {
        super(type, defaultValue, name, in, required);
        this.format = format;
        this.enumValues = enumValues;
    }

    static SimpleParameter create(
        String name,
        Location location,
        SimpleProperty property,
        boolean required
    ) {
        return new SimpleParameter(
            property.type,
            property.defaultValue,
            name,
            location,
            required || location == Location.PATH,
            property.format,
            property.enumValues
        );
    }
}
