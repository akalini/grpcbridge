package grpcbridge.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts variables from the path and body definitions as described by
 * {@link com.google.api.HttpRule}. The vars are specified as
 * {path.to.protobuf.field}. The var can only point to a primitive field
 * types.
 */
final class VariableExtractor {
    private static final String VAR_SEGMENT = "([^/]+)";
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{([\\w.]+)\\}");

    private final Pattern pattern;
    private final List<String> pathVars;
    private final Map<String, String> queryVars;

    /**
     * Creates new var extractor.
     *
     * @param pattern a pattern to extract the pathVars from
     */
    public VariableExtractor(String pattern) {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse(pattern);

        this.pathVars = new ArrayList<>();

        StringBuffer pathPatternBuilder = new StringBuffer();
        Matcher matcher = VAR_PATTERN.matcher(pathAndQuery.getPath());
        int lastMatched = 0;

        while (matcher.find()) {
            String varName = matcher.group().substring(1, matcher.group().length() - 1);
            pathVars.add(varName);
            matcher.appendReplacement(pathPatternBuilder, VAR_SEGMENT);
            lastMatched = matcher.end();
        }

        pathPatternBuilder.append(pathAndQuery.getPath().substring(lastMatched));
        this.pattern = Pattern.compile(pathPatternBuilder.toString());
        this.queryVars = pathAndQuery.getQueryParams();
    }

    /**
     * Checks if the input matches the variable extractor pattern.
     *
     * @param input input to parse
     * @return true if the variable extractor matches the input
     */
    public boolean matches(String input) {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse(input);

        if (!pattern.matcher(pathAndQuery.getPath()).matches()) {
            return false;
        }

        if (!pathAndQuery.getQueryParams().isEmpty()
                && !queryVars.keySet().containsAll(pathAndQuery.getQueryParams().keySet())) {
            return false;
        }

        return true;
    }

    /**
     * Extracts the variables from the specified input.
     *
     * @param input input to parse
     * @return list of extracted variables
     */
    public List<Variable> extract(String input) {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse(input);

        List<Variable> result = new ArrayList<>();

        Matcher pathMatcher = pattern.matcher(pathAndQuery.getPath());
        if (pathMatcher.matches()) {
            for (int i = 0; i < pathVars.size(); i++) {
                result.add(new Variable(pathVars.get(i), pathMatcher.group(1 + i)));
            }
        }

        result.addAll(pathAndQuery.getQueryParams().entrySet()
                .stream()
                .map(e -> new Variable(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        return result;
    }

    @Override public String toString() {
        return pattern.toString();
    }
}
