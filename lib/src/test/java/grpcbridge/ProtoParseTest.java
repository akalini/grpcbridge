package grpcbridge;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.parser.ProtoJsonParser;
import grpcbridge.parser.ProtoParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ProtoParseTest {

    default ProtoParser parser() {
        return ProtoJsonParser.INSTANCE;
    }

    default JsonFormat.Printer printer() {
        return JsonFormat.printer();
    }

    default String serialize(@Nonnull Message message) {
        return parser().serialize(printer(), message);
    }

    default <T extends Message> T parse(@Nullable String body, T.Builder builder) {
        return parser().parse(body, builder);
    }
}
