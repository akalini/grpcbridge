package grpcbridge.route;

import static grpcbridge.route.ExtensionVisitor.serializeDefaultValueFields;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import grpcbridge.GrpcbridgeOptions;
import grpcbridge.http.HttpRequest;
import grpcbridge.parser.Deserializer;
import grpcbridge.rpc.RpcCall;
import grpcbridge.rpc.RpcMessage;
import io.grpc.ServerMethodDefinition;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Set;

/**
 * A route used by the {@link grpcbridge.Bridge} to match HTTP requests to the
 * available gRPC services/methods. Each route corresponds to a single gRPC
 * method.
 */
public final class Route {
    public final MethodDescriptor descriptor;
    private final ServerMethodDefinition<Message, Message> impl;

    /**
     * @param descriptor methods descriptor from the protobuf file
     * @param impl the corresponding gRPC method definition backed by the
     *             bound implementation
     */
    public Route(
            MethodDescriptor descriptor,
            ServerMethodDefinition<Message, Message> impl
    ) {
        this.descriptor = descriptor;
        this.impl = impl;
    }

    /**
     * @return gRPC service name
     */
    public String getService() {
        return descriptor.getService().getFullName();
    }

    /**
     * @return gRPC method name
     */
    public String getMethod() {
        return descriptor
                .getFullName()
                .substring(descriptor.getService().getFullName().length() + 1);
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

        boolean includeDefaultValues = descriptor
                .getService()
                .getOptions()
                .getExtension(GrpcbridgeOptions.includeDefaultValues);
        if (includeDefaultValues) {
            printer = printer.includingDefaultValueFields();
        } else {
            Set<FieldDescriptor> fields = serializeDefaultValueFields(descriptor.getOutputType());
            if (!fields.isEmpty()) {
                printer = printer.includingDefaultValueFields(fields);
            }
        }
        boolean serializeEnumAsNumber = descriptor
                .getOptions()
                .getExtension(GrpcbridgeOptions.serializeEnumAsNumber);
        if (serializeEnumAsNumber) {
            printer = printer.printingEnumsAsInts();
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
    public Optional<RpcCall> match(Deserializer deserializer, HttpRequest httpRequest) {
        DescriptorProtos.MethodOptions options = descriptor.getOptions();
        HttpRule httpRule = options.getExtension(AnnotationsProto.http);
        PathMatcher pathMatcher = new PathMatcher(httpRule);

        if (pathMatcher.matches(httpRequest)) {
            BodyParser bodyParser = new BodyParser(deserializer, httpRule, newRpcRequest());
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
