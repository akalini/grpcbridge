package grpcbridge.util;


import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import grpcbridge.util.ProtoVisitor.SimpleFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static grpcbridge.util.ProtoVisitor.SimpleFieldType.BOOL;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.BYTES;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.DOUBLE;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.FLOAT;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.INT;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.LONG;
import static grpcbridge.util.ProtoVisitor.SimpleFieldType.STRING;

/**
 * Contains mapping for protobuf wrappers, with matching {@link SimpleFieldType} and a value parser function.
 */
public class SimpleFieldMapper {
    private final SimpleFieldType type;
    private final Function<String, Object> parser;

    private SimpleFieldMapper(SimpleFieldType type, Function<String, Object> parser) {
        this.type = type;
        this.parser = parser;
    }

    /**
     * Maps supported wrapper types to a {@link SimpleFieldMapper}.
     *
     * There's no simple way to identify whether the message is a wrapper, we have to use
     * explicit descriptors matching.
     */
    private static final Map<Descriptor, SimpleFieldMapper> mappers = new HashMap<Descriptor, SimpleFieldMapper>() {
        {
            put(DoubleValue.getDescriptor(), new SimpleFieldMapper(DOUBLE, (s) -> DoubleValue.of(Double.parseDouble(s))));
            put(FloatValue.getDescriptor(), new SimpleFieldMapper(FLOAT, (s) -> FloatValue.of(Float.parseFloat(s))));
            put(Int64Value.getDescriptor(), new SimpleFieldMapper(LONG, (s) -> Int64Value.of(Long.parseLong(s))));
            put(UInt64Value.getDescriptor(), new SimpleFieldMapper(LONG, (s) -> UInt64Value.of(Long.parseLong(s))));
            put(Int32Value.getDescriptor(), new SimpleFieldMapper(INT, (s) -> Int32Value.of(Integer.parseInt(s))));
            put(UInt32Value.getDescriptor(), new SimpleFieldMapper(INT, (s) -> UInt32Value.of(Integer.parseInt(s))));
            put(BoolValue.getDescriptor(), new SimpleFieldMapper(BOOL, (s) -> BoolValue.of(Boolean.parseBoolean(s))));
            put(StringValue.getDescriptor(), new SimpleFieldMapper(STRING, StringValue::of));
            put(BytesValue.getDescriptor(), new SimpleFieldMapper(BYTES, (s) -> BytesValue.of(ByteString.copyFrom(s.getBytes()))));
        }
    };

    /**
     * {@link SimpleFieldType} assigned to the mapper.
     */
    public SimpleFieldType getType() {
        return type;
    }

    /**
     * Checks whether the supplied {@link FieldDescriptor} represents a supported wrapper type.
     *
     * @param field field descriptor
     * @return whether the field is a supported wrapper type
     */
    public static boolean isSupported(FieldDescriptor field) {
        return mappers.containsKey(field.getMessageType());
    }

    /**
     * Returns a {@link SimpleFieldMapper} for a given ${@link FieldDescriptor}.
     * @param field field descriptor
     * @return matching field mapper or null if not found
     */
    public static SimpleFieldMapper forDescriptor(FieldDescriptor field) {
        return mappers.get(field.getMessageType());
    }

    /**
     * Applies value parser function to an argument.
     * @param value input value to map
     * @return field value
     */
    public Object parse(String value) {
        return parser.apply(value);
    }
}
