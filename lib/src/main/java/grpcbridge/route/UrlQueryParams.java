package grpcbridge.route;

import com.google.common.base.CaseFormat;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * A parsed out URL UrlQueryParams parameter map.
 */
final class UrlQueryParams {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{([\\w.]+)\\}");

    private final Map<String, String> query;
    private final Map<String, String> normalizedKeyMap;

    public UrlQueryParams(Map<String, String> query) {
        this.query = query;
        this.normalizedKeyMap = query.keySet()
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
    public boolean containsAll(Collection<String> params) {
        if (params.isEmpty()) {
            return true;
        }

        return params
                .stream()
                .allMatch(key -> normalizedKeyMap.containsKey(toCamelCase(key)));
    }

    /**
     * Extracts a list of variables out of the query parameters. The parameter
     * keys are normalized to make sure we support camel, snake and train cases.
     *
     * @param params list of parameters to extract the variables for
     * @return list of the extracted variables
     */
    public List<Variable> extractVars(Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(e -> {
                    String queryKey = normalizedKeyMap.get(toCamelCase(e.getKey()));
                    Matcher matcher = VAR_PATTERN.matcher(query.get(queryKey));
                    if (matcher.matches()) {
                        String protoKey = matcher.group(1);
                        String protoValue = e.getValue();
                        return new Variable(protoKey, protoValue);
                    } else {
                        return null;
                    }
                })
                .filter(o -> o != null)
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
