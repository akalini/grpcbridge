package grpcbridge.swagger.model;

import static java.util.stream.Collectors.toList;

import com.google.gson.annotations.SerializedName;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;

import java.util.List;

class SimpleProperty extends Property {
    final String format;
    final @SerializedName("enum") List<String> enumValues;

    private SimpleProperty(
        Type type,
        String format,
        String defaultValue,
        List<String> enumValues
    ) {
        super(type, defaultValue, null);
        this.format = format;
        this.enumValues = enumValues;
    }

    static SimpleProperty create(Type type, String format) {
        return new SimpleProperty(type, format, null, null);
    }

    static SimpleProperty forEnum(EnumDescriptor enumDescriptor) {
        String defaultValue = null;
        if (!enumDescriptor.getValues().isEmpty()) {
            defaultValue = enumDescriptor.getValues().get(0).getName();
        }
        List<String> values = enumDescriptor.getValues()
            .stream()
            .map(EnumValueDescriptor::getName)
            .collect(toList());
        return new SimpleProperty(Type.STRING, null, defaultValue, values);
    }
}
