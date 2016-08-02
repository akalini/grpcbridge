package grpcbridge.http;

import io.grpc.Metadata;

public final class HttpResponse {
    private final String body;
    private final Metadata trailers;

    public HttpResponse(String body, Metadata trailers) {
        this.body = body;
        this.trailers = trailers;
    }

    public String getBody() {
        return body;
    }

    public Metadata getTrailers() {
        return trailers;
    }
}
