package grpcbridge.route;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.CaseFormat;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parsed out URL UrlQueryParams parameter map.
 */
final class UrlQueryParams {
    private static final Pattern VALUE_PATTERN = Pattern.compile("([\\w.]+)");
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{([\\w.]+)\\}");

    private final Map<String, String> query;
    private final Map<String, String> normalizedKeyMap;

    public UrlQueryParams(Map<String, List<String>> pattern) {
        this.query = pattern.entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<String> params = e.getValue();
                            if (params.size() != 1) {
                                throw new IllegalArgumentException(params.stream().collect(joining(
                                        ",",
                                        "Multiple bindings found for " + e.getKey() + ": ",
                                        "")));
                            }
                            return params.get(0);
                        }));
        this.normalizedKeyMap = pattern.keySet()
                .stream()
                .collect(toMap(
                        UrlQueryParams::toCamelCase,
                        k -> k));
    }

    /**
     * Returns true if this query contains all the passed in parameters.
     *
     * @param params query parameters to check
     * @return true if this query contains all the parameters
     */
    public boolean containsAll(Map<String, List<String>> params) {
        if (params.isEmpty()) {
            return true;
        }

        return params.entrySet()
                .stream()
                .allMatch(e -> {
                    String queryKey = normalizedKeyMap.get(toCamelCase(e.getKey()));
                    if (queryKey == null) {
                        return false;
                    }

                    String queryValue = query.get(queryKey);
                    Matcher matcher = VALUE_PATTERN.matcher(queryValue);
                    if (matcher.matches()) {
                        return e.getValue()
                                .stream()
                                .anyMatch(p -> p.equals(queryValue));
                    } else {
                        return true;
                    }
                });
    }

    /**
     * Extracts a list of variables out of the query parameters. The parameter
     * keys are normalized to make sure we support camel, snake and train cases.
     *
     * @param params list of parameters to extract the variables for
     * @return list of the extracted variables
     */
    public List<Variable> extractVars(Map<String, List<String>> params) {
        return params.entrySet()
                .stream()
                .flatMap(e -> {
                    String queryKey = normalizedKeyMap.get(toCamelCase(e.getKey()));
                    Matcher matcher = VAR_PATTERN.matcher(query.get(queryKey));
                    if (matcher.matches()) {
                        String protoKey = matcher.group(1);
                        return e.getValue()
                                .stream()
                                .map(v -> new Variable(protoKey, v));
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
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
