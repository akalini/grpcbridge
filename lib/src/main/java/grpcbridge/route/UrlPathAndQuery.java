package grpcbridge.route;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Parses URL path and query parameters. Normalizes parameters to be camelCase.
 */
final class UrlPathAndQuery {
    public static UrlPathAndQuery parse(String pattern) {
        return new UrlPathAndQuery(pattern);
    }

    private final String path;
    private final Map<String, String> query;

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
                                Map.Entry::getValue));
                break;
            default:
                throw new IllegalArgumentException("Invalid URL path: " + urlPath);
        }
    }

    public String path() {
        return path;
    }

    public Map<String, String> query() {
        return query;
    }

    private static Map.Entry<String, String> toMapEntry(String value) {
        String[] keyAndValue = value.split("=");
        return keyAndValue.length == 1
                ? new SimpleImmutableEntry<>(keyAndValue[0], "")
                : new SimpleImmutableEntry<>(keyAndValue[0], keyAndValue[1]);
    }
}
