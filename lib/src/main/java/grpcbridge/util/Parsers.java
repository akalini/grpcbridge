package grpcbridge.util;

import com.google.common.base.Strings;
import grpcbridge.http.HttpRequest;
import grpcbridge.parser.Parser;
import grpcbridge.parser.ProtoJsonParser;
import io.grpc.Metadata;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Parsers {

    private Parsers() {}

    public static @Nonnull Parser findBestRequestParser(
            List<Parser> parsers,
            HttpRequest httpRequest
    ) {
        return findBestParserForHeader(parsers, httpRequest, "content-type", null);
    }

    public static @Nonnull Parser findBestResponse(
        List<Parser> parsers,
        HttpRequest httpRequest,
        String preferredType
    ) {
        return findBestParserForHeader(parsers, httpRequest, "accept", preferredType);
    }

    private static @Nonnull Parser findBestParserForHeader(
            List<Parser> parsers,
            HttpRequest httpRequest,
            String header,
            String preferredType
    ) {
        List<String> supportedTypes = new ArrayList<>();
        if (!Strings.isNullOrEmpty(preferredType)) supportedTypes.add(preferredType);

        Iterable<String> acceptedTypes = httpRequest.getHeaders().getAll(Metadata.Key.of(
                header,
                Metadata.ASCII_STRING_MARSHALLER));
        if (acceptedTypes != null) {
            acceptedTypes.forEach(supportedTypes::add);
        }
        if (supportedTypes.isEmpty()) {
            return ProtoJsonParser.INSTANCE;
        }
        return parsers.stream()
                .filter( it -> it.accept(supportedTypes))
                .findFirst()
                .orElse(ProtoJsonParser.INSTANCE);
    }
}
