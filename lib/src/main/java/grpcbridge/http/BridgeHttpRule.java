package grpcbridge.http;

import com.google.api.HttpRule;

public class BridgeHttpRule {
    private final String path;
    private final HttpMethod method;
    private final String body;

    private BridgeHttpRule(String path, HttpMethod method, String body) {
        this.path = path;
        this.method = method;
        this.body = body;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public static BridgeHttpRule create(HttpRule httpRule) {
        if (!httpRule.getGet().isEmpty()) {
            return new BridgeHttpRule(httpRule.getGet(), HttpMethod.GET, httpRule.getBody());
        } else if (!httpRule.getPost().isEmpty()) {
            return new BridgeHttpRule(httpRule.getPost(), HttpMethod.POST, httpRule.getBody());
        } else if (!httpRule.getPut().isEmpty()) {
            return new BridgeHttpRule(httpRule.getPut(), HttpMethod.PUT, httpRule.getBody());
        } else if (!httpRule.getDelete().isEmpty()) {
            return new BridgeHttpRule(httpRule.getDelete(), HttpMethod.DELETE, httpRule.getBody());
        } else if (!httpRule.getPatch().isEmpty()) {
            return new BridgeHttpRule(httpRule.getPatch(), HttpMethod.PATCH, httpRule.getBody());
        } else if (httpRule.getPatternCase() == HttpRule.PatternCase.PATTERN_NOT_SET) {
            throw new UnsupportedOperationException("Pattern is not set. Make sure you have the HTTP binding defined.");
        } else {
            throw new UnsupportedOperationException("Unsupported method: " + httpRule);
        }
    }
}
