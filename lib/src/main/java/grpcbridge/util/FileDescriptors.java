package grpcbridge.util;

import com.google.api.AnnotationsProto;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import grpcbridge.Exceptions.ConfigurationException;
import grpcbridge.route.Route;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Helper methods to simplify dealing with {@link com.google.protobuf.Descriptors.FileDescriptor}s.
 */
public final class FileDescriptors {
    private final List<Descriptors.FileDescriptor> files = new ArrayList<>();

    private static Optional<Descriptors.ServiceDescriptor> serviceFor(
            Descriptors.FileDescriptor file,
            ServerServiceDefinition service) {
        String[] serviceName = service.getServiceDescriptor().getName().split("\\.");
        return Optional.ofNullable(file.findServiceByName(serviceName[serviceName.length - 1]));
    }

    private static Optional<Descriptors.MethodDescriptor> methodFor(
            Descriptors.ServiceDescriptor service,
            ServerMethodDefinition method) {
        String[] methodName = method.getMethodDescriptor().getFullMethodName().split("/");
        return Optional.ofNullable(service.findMethodByName(methodName[methodName.length - 1]));
    }

    /**
     * Adds another protobuf file descriptor.
     *
     * @param file protobuf file descriptor
     */
    public void addFile(Descriptors.FileDescriptor file) {
        files.add(file);
    }

    /**
     * Returns {@link Route} instance for the specified service/method, if
     * annotated as an HTTP method.
     *
     * @param service service definition
     * @param method method definition
     * @return route for the given service/method combination
     */
    public Optional<Route> routeFor(
            ServerServiceDefinition service,
            ServerMethodDefinition<Message, Message> method) {
        for (Descriptors.FileDescriptor file: files) {
            Optional<Descriptors.MethodDescriptor> protoMethod = serviceFor(file, service)
                    .flatMap(s -> methodFor(s, method));
            if (protoMethod.isPresent()) {
                if (protoMethod.get().getOptions().hasExtension(AnnotationsProto.http)) {
                    return Optional.of(new Route(protoMethod.get(), method));
                } else {
                    return Optional.empty();
                }
            }
        }

        throw new ConfigurationException(String.format(
                "Proto definition for %s is not found, did you forget to add the proto file?",
                method.getMethodDescriptor().getFullMethodName()));
    }
}
