package grpcbridge.route;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final List<String> vars;

    /**
     * Creates new var extractor.
     *
     * @param pattern a pattern to extract the vars from
     */
    public VariableExtractor(String pattern) {
        this.vars = new ArrayList<>();

        pattern = pattern.replace("?", "\\?");

        StringBuffer pathPatternBuilder = new StringBuffer();
        Matcher matcher = VAR_PATTERN.matcher(pattern);
        int lastMatched = 0;

        while (matcher.find()) {
            String varName = matcher.group().substring(1, matcher.group().length() - 1);
            vars.add(varName);
            matcher.appendReplacement(pathPatternBuilder, VAR_SEGMENT);
            lastMatched = matcher.end();
        }

        pathPatternBuilder.append(pattern.substring(lastMatched));
        this.pattern = Pattern.compile(pathPatternBuilder.toString());
    }

    /**
     * Checks if the input matches the variable extractor pattern.
     *
     * @param input input to parse
     * @return true if the variable extractor matches the input
     */
    public boolean matches(String input) {
        return pattern.matcher(input).matches();
    }

    /**
     * Extracts the variables from the specified input.
     *
     * @param input input to parse
     * @return list of extracted variables
     */
    public List<Variable> extract(String input) {
        List<Variable> result = new ArrayList<>();

        Matcher pathMatcher = pattern.matcher(input);
        if (pathMatcher.matches()) {
            for (int i = 0; i < vars.size(); i++) {
                result.add(new Variable(vars.get(i), pathMatcher.group(1 + i)));
            }
        }

        return result;
    }

    @Override public String toString() {
        return pattern.toString();
    }
}
