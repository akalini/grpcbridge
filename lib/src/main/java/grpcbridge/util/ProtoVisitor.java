package grpcbridge.util;

import static java.lang.String.format;

import com.google.protobuf.Descriptors.FieldDescriptor;

public abstract class ProtoVisitor {
    public enum SimpleFieldType {
        INT, LONG, BOOL, DOUBLE, FLOAT, STRING, BYTES, ENUM;

        /**
         * Maps simple protobuf types to a {@link SimpleFieldType}.
         *
         * @param field field descriptor
         * @return simple field type
         */
        public static SimpleFieldType fromDescriptor(FieldDescriptor field) {
            switch (field.getType()) {
                case INT32:
                case SINT32:
                case SFIXED32:
                case UINT32:
                case FIXED32:
                    return SimpleFieldType.INT;

                case INT64:
                case SINT64:
                case SFIXED64:
                case UINT64:
                case FIXED64:
                    return SimpleFieldType.LONG;

                case BOOL:
                    return SimpleFieldType.BOOL;

                case DOUBLE:
                    return SimpleFieldType.DOUBLE;

                case FLOAT:
                    return SimpleFieldType.FLOAT;

                case STRING:
                    return SimpleFieldType.STRING;

                case BYTES:
                    return SimpleFieldType.BYTES;

                case ENUM:
                    return SimpleFieldType.ENUM;

                case MESSAGE:
                case GROUP:
                default:
                    throw new IllegalArgumentException(
                        format(
                            "Unsupported field type: %s (%s)",
                            field.getType(),
                            field.getFullName()
                        )
                    );
            }
        }
    }

    /**
     * Checks if the field is accepted by the visitor.
     *
     * @param field a field descriptor
     * @return true if the field is accepted; false otherwise
     */
    public boolean accept(FieldDescriptor field) {
        return true;
    }

    /**
     * Invoked at the start of a repeated field.
     *
     * @param field repeated field descriptor
     */
    public void onRepeatedFieldStart(FieldDescriptor field) {}

    /**
     * Invoked at the end of a repeated field.
     *
     * @param field repeated field descriptor
     */
    public void onRepeatedFieldEnd(FieldDescriptor field) {}

    /**
     * Invoked at the start of a nested message field.
     *
     * @param field repeated field descriptor
     */
    public void onMessageStart(FieldDescriptor field) {}

    /**
     * Invoked at the end of a nested message field.
     *
     * @param field repeated field descriptor
     */
    public void onMessageEnd(FieldDescriptor field) {}

    /**
     * Invoked before every field, complex or simple.
     *
     * @param field field descriptor
     */
    public void onBeforeField(FieldDescriptor field) {}

    /**
     * Invoked after every field, complex or simple.
     *
     * @param field field descriptor
     */
    public void onAfterField(FieldDescriptor field) {}

    /**
     * Invoked for every simple field (non nested message).
     *
     * @param field field descriptor
     */
    public void onSimpleField(FieldDescriptor field, SimpleFieldType type) {}
}
