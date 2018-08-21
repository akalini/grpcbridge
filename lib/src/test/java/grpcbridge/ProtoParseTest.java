package grpcbridge;

import com.google.common.base.Charsets;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.parser.ProtoConverter;
import grpcbridge.parser.ProtoJsonConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ProtoParseTest {

    default ProtoConverter parser() {
        return ProtoJsonConverter.INSTANCE;
    }

    default JsonFormat.Printer printer() {
        return JsonFormat.printer();
    }

    default String serialize(@Nonnull Message message) {
        return parser().serialize(printer(), message);
    }

    default <T extends Message> T parse(@Nullable String body, T.Builder builder) {
        return parser().parse(body, Charsets.UTF_8, builder);
    }
}
