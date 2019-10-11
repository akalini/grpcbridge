package grpcbridge.swagger;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import java.util.Set;

/**
 * Configures schema generation. Handles field name formatting and optional extensions.
 */
class SwaggerConfig {
    private final Set<GeneratedExtension<FieldOptions, Boolean>> requiredExtensions;
    private final FieldNameFormatter formatter;
    private final boolean excludeDeprecated;

    SwaggerConfig(
        Set<GeneratedExtension<FieldOptions, Boolean>> requiredExtensions,
        FieldNameFormatter formatter,
        boolean excludeDeprecated
    ) {
        this.requiredExtensions = requiredExtensions;
        this.formatter = formatter;
        this.excludeDeprecated = excludeDeprecated;
    }

    boolean isRequired(FieldDescriptor field) {
        return requiredExtensions
            .stream()
            .anyMatch(extension -> field.getOptions().getExtension(extension));
    }

    boolean isExcluded(FieldDescriptor field) {
        return field.getOptions().getDeprecated();
    }

    String formatFieldName(FieldDescriptor field) {
        return formatter.nameFor(field);
    }
}
