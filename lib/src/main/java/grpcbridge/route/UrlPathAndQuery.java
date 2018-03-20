package grpcbridge.route;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses URL path and query parameters. Normalizes parameters to be camelCase.
 */
final class UrlPathAndQuery {
    public static UrlPathAndQuery parse(String pattern) {
        return new UrlPathAndQuery(pattern);
    }

    private final String path;
    private final Map<String, List<String>> query;

    UrlPathAndQuery(String urlPath) {
        String[] pathAndQuery = urlPath.split("\\?");
        switch (pathAndQuery.length) {
            case 1:
                this.path = pathAndQuery[0];
                this.query = new HashMap<>();
                break;
            case 2:
                this.path = pathAndQuery[0];
                this.query = Arrays.stream(pathAndQuery[1].split("&"))
                        .map(UrlPathAndQuery::toMapEntry)
                        .collect(toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (one, two) -> {
                                    List<String> result = new ArrayList<>(one);
                                    result.addAll(two);
                                    return result;
                                }));
                break;
            default:
                throw new IllegalArgumentException("Invalid URL path: " + urlPath);
        }
    }

    public String path() {
        return path;
    }

    public Map<String, List<String>> query() {
        return query;
    }

    private static Map.Entry<String, List<String>> toMapEntry(String value) {
        String[] keyAndValue = value.split("=");
        try {
            return keyAndValue.length == 1
                    ? new SimpleImmutableEntry<>(keyAndValue[0], singletonList(""))
                    : new SimpleImmutableEntry<>(
                            keyAndValue[0],
                            singletonList(URLDecoder.decode(keyAndValue[1], UTF_8.name())));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
