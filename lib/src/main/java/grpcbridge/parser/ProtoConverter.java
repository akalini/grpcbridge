package grpcbridge.parser;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.rpc.RpcMessage;
import io.grpc.Metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public abstract class ProtoConverter implements Serializer, Deserializer {

    private static final String ANY = "*/*";

    protected abstract MediaType contentType();

    protected abstract String packMultiple(List<String> serializedItems);

    public abstract <T extends Message> T parse(
            @Nullable String body,
            Charset charset,
            T.Builder builder
    );

    protected abstract String serialize(
            @Nullable Integer index,
            @Nonnull JsonFormat.Printer printer,
            @Nonnull Message message
    );

    @Override
    public boolean supportsAny(Collection<MediaType> accepted) {
        return accepted.stream().anyMatch(it -> supported(it));
    }

    @Override
    public boolean supported(MediaType contentType) {
        for (MediaType mediaType : supportedTypes()) {
            boolean supported = mediaType.is(contentType);
            if (supported) return true;
        }
        return false;
    }

    protected List<MediaType> supportedTypes() {
        return Collections.singletonList(contentType());
    }

    @Override
    public RpcMessage deserialize(
            HttpRequest httpRequest,
            Message.Builder builder
    ) {
        String contentType = httpRequest
                .getHeaders()
                .get(Metadata.Key.of("content-type", ASCII_STRING_MARSHALLER));
        Charset charset = Charsets.UTF_8;
        if (contentType != null) {
            MediaType type = MediaType.parse(contentType);
            charset = type.charset().or(Charsets.UTF_8);
        }
        return new RpcMessage(
                parse(httpRequest.getBody().orElse(null), charset, builder),
            httpRequest.getHeaders());
    }

    @Override
    public HttpResponse serialize(
            @Nonnull JsonFormat.Printer printer,
            @Nonnull RpcMessage message
    ) {
        if (message.getMethodType().serverSendsOneMessage()) {
            String httpBody = !message.getBody().isEmpty() ? serialize(printer,
                message.getBody().get(0))
                : "";
            return new HttpResponse(httpBody, withContentType(message.getMetadata()));
        } else {
            List<String> serialized = new ArrayList<>();
            for (int i = 0; i < message.getBody().size(); i++) {
                serialized.add(serialize(i, printer, message.getBody().get(i)));
            }
            return new HttpResponse(packMultiple(serialized), withContentType(message.getMetadata()));
        }
    }

    @Override
    public Function<RpcMessage, HttpResponse> serializeAsync(JsonFormat.Printer printer) {
        return new Function<RpcMessage, HttpResponse>() {
            @Nullable
            @Override
            public HttpResponse apply(@Nullable RpcMessage response) {
                return serialize(printer, response);
            }
        };
    }

    public String serialize(@Nonnull JsonFormat.Printer printer, @Nonnull Message message) {
        return serialize(null, printer, message);
    }

    private Metadata withContentType(Metadata headers) {
        headers.put(
                Metadata.Key.of("content-type", ASCII_STRING_MARSHALLER),
                contentType().toString()
        );
        return headers;
    }
}
