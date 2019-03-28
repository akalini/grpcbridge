package grpcbridge.swagger;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import javax.annotation.Nullable;

/**
 * Configures schema generation. Handles field name formatting and optional extensions.
 */
class SwaggerConfig {
    private final @Nullable GeneratedExtension<FieldOptions, Boolean> requiredExtension;
    private final FieldNameFormatter formatter;

    SwaggerConfig(
        @Nullable GeneratedExtension<FieldOptions, Boolean> requiredExtension,
        FieldNameFormatter formatter
    ) {
        this.requiredExtension = requiredExtension;
        this.formatter = formatter;
    }

    boolean isRequired(FieldDescriptor field) {
        return requiredExtension != null && field.getOptions().getExtension(requiredExtension);
    }

    String formatFieldName(FieldDescriptor field) {
        return formatter.nameFor(field);
    }
}
