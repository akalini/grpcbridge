package grpcbridge.swagger;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.util.Set;
import javax.annotation.Nullable;

/**
 * Configures schema generation. Handles field name formatting and optional extensions.
 */
class SwaggerConfig {
    private final Set<GeneratedExtension<FieldOptions, Boolean>> requiredExtensions;
    private final FieldNameFormatter formatter;

    SwaggerConfig(
        Set<GeneratedExtension<FieldOptions, Boolean>> requiredExtensions,
        FieldNameFormatter formatter
    ) {
        this.requiredExtensions = requiredExtensions;
        this.formatter = formatter;
    }

    boolean isRequired(FieldDescriptor field) {
        return requiredExtensions
            .stream()
            .anyMatch(extension -> field.getOptions().getExtension(extension));
    }

    String formatFieldName(FieldDescriptor field) {
        return formatter.nameFor(field);
    }
}
