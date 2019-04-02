package grpcbridge.route;

import static grpcbridge.GrpcbridgeOptions.serializeDefaultValue;

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Extension;

import grpcbridge.util.ProtoDescriptorTraverser;
import grpcbridge.util.ProtoVisitor;
import java.util.HashSet;
import java.util.Set;

/**
 * Proto visitor which retrieves all fields (including nested) in a message that have an extension
 * matching the given extension.
 */
final class ExtensionVisitor<T> extends ProtoVisitor {
    private final Extension<FieldOptions, T> extension;
    private final T value;
    private final Set<FieldDescriptor> matchingFields = new HashSet<>();

    private ExtensionVisitor(Extension<FieldOptions, T> extension, T value) {
        this.extension = extension;
        this.value = value;
    }

    /**
     * Returns all fields that have {@code serializeDefaultValue} set.
     *
     * @param message message to traverse
     * @return matched fields
     */
    static Set<FieldDescriptor> serializeDefaultValueFields(Descriptor message) {
        return fieldsWithExtension(message, serializeDefaultValue, true);
    }

    private static <T> Set<FieldDescriptor> fieldsWithExtension(
        Descriptor message,
        Extension<FieldOptions, T> extension,
        T value
    ) {
        ExtensionVisitor<T> visitor = new ExtensionVisitor<>(extension, value);
        new ProtoDescriptorTraverser(visitor).traverse(message);
        return visitor.matchingFields;
    }

    @Override
    public void onBeforeField(FieldDescriptor field) {
        if (field.getOptions().getExtension(extension).equals(value)) {
            matchingFields.add(field);
        }
    }
}
