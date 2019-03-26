package grpcbridge.route;

import com.google.api.HttpRule;

import grpcbridge.http.BridgeHttpRule;
import grpcbridge.http.HttpMethod;
import grpcbridge.http.HttpRequest;

import java.util.List;

import static java.lang.String.format;

final class PathMatcher {
    private final HttpMethod method;
    private final VariableExtractor path;

    public PathMatcher(HttpRule httpRule) {
        BridgeHttpRule rule = BridgeHttpRule.create(httpRule);
        this.method = rule.getMethod();
        this.path = new VariableExtractor(rule.getPath());
    }

    public boolean matches(HttpRequest httpRequest) {
        return method == httpRequest.getMethod() && path.matches(httpRequest.getPath());
    }

    public List<Variable> parse(HttpRequest httpRequest) {
        return path.extract(httpRequest.getPath());
    }

    @Override public String toString() {
        return format("PathMatcher(%s, %s)", method, path);
    }
}
