package grpcbridge.route;

import com.google.api.HttpRule;
import grpcbridge.http.HttpMethod;
import grpcbridge.http.HttpRequest;

import java.util.List;

import static java.lang.String.format;

final class PathMatcher {
    private final HttpMethod method;
    private final VariableExtractor path;

    public PathMatcher(HttpRule httpRule) {
        String path = null;
        if (!httpRule.getGet().isEmpty()) {
            this.method = HttpMethod.GET;
            path = httpRule.getGet();
        } else if (!httpRule.getPost().isEmpty()) {
            this.method = HttpMethod.POST;
            path = httpRule.getPost();
        } else if (!httpRule.getPut().isEmpty()) {
            this.method = HttpMethod.PUT;
            path = httpRule.getPut();
        } else if (!httpRule.getDelete().isEmpty()) {
            this.method = HttpMethod.DELETE;
            path = httpRule.getDelete();
        } else if (!httpRule.getPatch().isEmpty()) {
            this.method = HttpMethod.PATCH;
            path = httpRule.getPatch();
        } else {
            // just ignore RPCs that have no valid HttpRule
            this.method = null;
        }
        this.path = path == null ? null : new VariableExtractor(path);
    }

    public boolean matches(HttpRequest httpRequest) {
        if ((method == null) || (path == null)) {
            return false;
        }
        return method == httpRequest.getMethod() && path.matches(httpRequest.getPath());
    }

    public List<Variable> parse(HttpRequest httpRequest) {
        if ((method == null) || (path == null)) {
            return null;
        }
        return path.extract(httpRequest.getPath());
    }

    @Override public String toString() {
        return format("PathMatcher(%s, %s)", method, path);
    }
}
