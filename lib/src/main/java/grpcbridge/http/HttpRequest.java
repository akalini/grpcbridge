package grpcbridge.http;

import com.google.common.base.Strings;
import io.grpc.Metadata;

import java.util.Optional;

import static java.lang.String.format;

/**
 * Describes an HTTP request. The library is HTTP framework independent, so
 * the caller is responsible for setting up an HTTP endpoint and translating
 * HTTP requests into {@link HttpRequest} instances.
 */
public final class HttpRequest {
    /**
     * Used to build an HTTP request.
     */
    public static class Builder {
        private final HttpMethod method;
        private final String path;
        private Metadata headers;
        private Optional<String> body;

        /**
         * Creates new builder instance for a given HTTP method and path.
         *
         * @param method HTTP method
         * @param path URI path, e.g. /some/path?param=value
         */
        Builder(HttpMethod method, String path) {
            this.method = method;
            this.path = path;
            this.headers = new Metadata();
            this.body = Optional.empty();
        }

        /**
         * Sets HTTP request body. The body is parsed as either a protobuf
         * request or as a protobuf field depending on the
         * {@link com.google.api.HttpRule} annotation for the matching route.
         *
         * @param body HTTP request body
         * @return this builder instance
         */
        public Builder body(String body) {
            this.body = Optional.ofNullable(Strings.emptyToNull(body));
            return this;
        }

        /**
         * Sets up request headers {@link Metadata}.
         *
         * @param metadata gRPC headers metadata
         * @return this builder instance
         */
        public Builder headers(Metadata metadata) {
            this.headers = metadata;
            return this;
        }

        /**
         * Builds new instance of the HTTP request.
         *
         * @return built HTTP request instance
         */
        public HttpRequest build() {
            return new HttpRequest(method, path, headers, body);
        }
    }

    private final HttpMethod method;
    private final String path;
    private final Metadata headers;
    private final Optional<String> body;

    public static Builder builder(HttpMethod method, String path) {
        return new Builder(method, path);
    }

    /**
     * @param method HTTP request method
     * @param path HTTP URI path, e.g. /some/path?param=value
     * @param headers  gRPC headers metadata
     * @param body optional request body
     */
    private HttpRequest(HttpMethod method, String path, Metadata headers, Optional<String> body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    /**
     * @return HTTP request method
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * @return HTTP request path, e.g. /some/path?param=value
     */
    public String getPath() {
        return path;
    }

    /**
     * @return gRPC headers metadata
     */
    public Metadata getHeaders() {
        return headers;
    }

    /**
     * @return optional request body
     */
    public Optional<String> getBody() {
        return body;
    }

    @Override public String toString() {
        return format("HttRequest(%s %s %s)", method, path, body.orElse(null));
    }
}
