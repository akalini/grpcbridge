package grpcbridge.route;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Describes a variable that can be specified in {@link com.google.api.HttpRule}
 * path or body patterns in the {path.to.protobuf.field} form.
 *
 * The variable name and path is converted to snake case since that's the protobuf
 * convention.
 */
public final class Variable {
    private final String name;
    private final String value;

    /**
     * @param name variable name, e.g path.to.protobuf.field
     * @param value variable value
     */
    public Variable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return variable path. If variable is {path.to.protobuf.field}, returns
     *      ["path", "to", "protobuf"] array
     */
    public String[] getFieldPath() {
        String[] path = name.split("\\.");
        return Arrays.copyOfRange(path, 0, path.length - 1);
    }

    /**
     * @return variable path. If variable is {path.to.protobuf.field}, returns
     *      "field" as the result
     */
    public Collection<String> getFieldNames() {
        String[] path = name.split("\\.");
        return allNamesFor(path[path.length - 1]);
    }

    /**
     * Converts the var value to the type specified by the supplied field
     * description.
     *
     * @param field field descriptor
     * @return variable value
     */
    public Object valueAs(Descriptors.FieldDescriptor field) {
        switch (field.getJavaType()) {
            case INT:
                return Integer.parseInt(value);
            case LONG:
                return Long.parseLong(value);
            case FLOAT:
                return Float.parseFloat(value);
            case DOUBLE:
                return Double.parseDouble(value);
            case BOOLEAN:
                return Boolean.parseBoolean(value);
            case STRING:
                return value;
            case BYTE_STRING:
                return ByteString.copyFrom(value.getBytes());
            case ENUM:
                return field.getEnumType().findValueByName(value);
            default:
                throw new IllegalStateException("Unsupported field type found: " + field);
        }
    }

    @Override public String toString() {
        return format("%s = %s", name, value);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Variable)) {
            return false;
        }

        Variable other = (Variable) obj;
        return name.equals(other.name) && Objects.equals(value, other.value);
    }

    private static List<String> allNamesFor(String value) {
        if (value.contains("-")) {
            return asList(
                    value,
                    CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, value));
        }

        if (!value.contains("_")) {
            return asList(
                    value,
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, value));
        }

        return ImmutableList.of(value);
    }
}
