package grpcbridge.util;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

import grpcbridge.util.ProtoVisitor.SimpleFieldType;
import java.util.List;

/**
 * Traverses a protobuf message descriptor. Implements a visitor pattern, using
 * {@link ProtoVisitor} as the callback interface.
 */
public class ProtoDescriptorTraverser {
    private final ProtoVisitor visitor;

    public ProtoDescriptorTraverser(ProtoVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * Traverses the given descriptor.
     *
     * @param messageDescriptor message descriptor to traverse
     */
    public void traverse(Descriptor messageDescriptor) {
        traverse(messageDescriptor.getFields());
    }

    private void traverse(List<FieldDescriptor> fields) {
        fields
                .stream()
                .filter(visitor::accept)
                .forEach(field -> {
                    visitor.onBeforeField(field);
                    onField(field);
                    visitor.onAfterField(field);
                });
    }

    /**
     * Invoked for a protobuf message field.
     *
     * @param field field descriptor
     */
    private void onField(FieldDescriptor field) {
        if (field.isRepeated()) {
            onRepeatedField(field);
        } else {
            onSingleField(field);
        }
    }

    /**
     * Invoked for a protobuf repeated message field. The contained type will also be traversed.
     *
     * @param field field descriptor
     */
    private void onRepeatedField(FieldDescriptor field) {
        visitor.onRepeatedFieldStart(field);
        onSingleField(field);
        visitor.onRepeatedFieldEnd(field);
    }

    /**
     * Invoked for a protobuf single message field.
     *
     * @param field field descriptor
     */
    private void onSingleField(FieldDescriptor field) {
        if (field.isExtension()) {
            throw new IllegalArgumentException("Extensions are not supported");
        } else if (field.getType() == Type.GROUP) {
            throw new IllegalArgumentException("Groups are not supported");
        }

        if (field.getJavaType() == JavaType.MESSAGE && !SimpleFieldMapper.isSupported(field)) {
            onMessageField(field);
        } else {
            visitor.onSimpleField(field, SimpleFieldType.fromDescriptor(field));
        }
    }

    /**
     * Invoked for a protobuf nested message field.
     *
     * @param field field descriptor
     */
    private void onMessageField(FieldDescriptor field) {
        visitor.onMessageStart(field);
        field.getMessageType().getFields().forEach(fieldDescriptor -> {
            visitor.onBeforeField(fieldDescriptor);
            onField(fieldDescriptor);
            visitor.onAfterField(fieldDescriptor);
        });
        visitor.onMessageEnd(field);
    }
}
