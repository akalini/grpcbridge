package grpcbridge.route;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.GrpcbridgeOptions;
import grpcbridge.http.HttpRequest;
import grpcbridge.rpc.RpcCall;
import grpcbridge.rpc.RpcMessage;
import grpcbridge.parser.Parser;
import io.grpc.ServerMethodDefinition;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

/**
 * A route used by the {@link grpcbridge.Bridge} to match HTTP requests to the
 * available gRPC services/methods. Each route corresponds to a single gRPC
 * method.
 */
public final class Route {
    private final MethodDescriptor descriptor;
    private final ServerMethodDefinition<Message, Message> impl;
    private final List<Parser> parsers;

    /**
     * @param descriptor methods descriptor from the protobuf file
     * @param impl the corresponding gRPC method definition backed by the
     *             bound implementation
     */
    public Route(List<Parser> parsers, MethodDescriptor descriptor,
                 ServerMethodDefinition<Message, Message> impl) {
        this.parsers = parsers;
        this.descriptor = descriptor;
        this.impl = impl;
    }

    /**
     * Return JSON printer for the route.
     *
     * @return JSON printer
     */
    public JsonFormat.Printer getPrinter() {
        JsonFormat.Printer printer = JsonFormat.printer();
        boolean preserveFieldNames = descriptor
                .getService()
                .getOptions()
                .getExtension(GrpcbridgeOptions.preserveFieldNames);
        if (preserveFieldNames) {
            printer = printer.preservingProtoFieldNames();
        }
        return printer;
    }

    /**
     * Matches HTTP request against the gRPC method definition. If the route
     * containsAll returns an {@link RpcCall} instance that can be used to invoke
     * the corresponding gRPC method.
     *
     * @param httpRequest HTTP request
     * @return {@link RpcCall} instance that can be used to invoke the
     *      corresponding gPRC method, {@link Optional#empty()} if the route
     *      has not matched
     */
    public Optional<RpcCall> match(HttpRequest httpRequest) {
        DescriptorProtos.MethodOptions options = descriptor.getOptions();
        HttpRule httpRule = options.getExtension(AnnotationsProto.http);
        PathMatcher pathMatcher = new PathMatcher(httpRule);

        if (pathMatcher.matches(httpRequest)) {
            BodyParser bodyParser = new BodyParser(parsers, httpRule, newRpcRequest());
            RpcMessage rpcRequest = bodyParser.extract(httpRequest);
            pathMatcher.parse(httpRequest).forEach(rpcRequest::setVar);
            return Optional.of(new RpcCall(impl, rpcRequest));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return descriptor.getFullName();
    }

    private Message newRpcRequest() {
        return impl
                .getMethodDescriptor()
                .parseRequest(new ByteArrayInputStream(new byte[] {}));
    }
}
