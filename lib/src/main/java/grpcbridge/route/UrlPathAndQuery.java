package grpcbridge.route;

import com.google.common.base.CaseFormat;

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

    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParams() {
        return query;
    }

    private static Map.Entry<String, String> toMapEntry(String value) {
        String[] keyAndValue = value.split("=");
        return keyAndValue.length == 1
                ? new SimpleImmutableEntry<>(toCamelCase(keyAndValue[0]), "")
                : new SimpleImmutableEntry<>(toCamelCase(keyAndValue[0]), keyAndValue[1]);
    }

    private static String toCamelCase(String value) {
        if (value.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, value);
        }

        if (value.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, value);
        }

        return value;
    }
}
