package grpcbridge;

import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import grpcbridge.route.Route;
import grpcbridge.util.FileDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to build {@link Bridge} instances. When a protobuf file is compiled
 * it generates all the service and request/responses definitions. Each
 * service is described as {@link ServerServiceDefinition}. The compiler also
 * generates a protobuf file descriptor in the {@link FileDescriptor} format.
 * The builder uses both to setup a bridge. The file descriptor is used to
 * extract {@link com.google.api.HttpRule} annotations and the service
 * definition is used to enumerate the available methods and to find the
 * available handlers.
 */
public final class BridgeBuilder {
    private final FileDescriptors files = new FileDescriptors();
    private final List<ServerServiceDefinition> services = new ArrayList<>();

    /**
     * Adds protobuf file descriptor. Call this method for each of the protobuf
     * files that you use to define your services.
     *
     * @param file protobuf file descriptor
     * @return this builder instance
     */
    public BridgeBuilder addFile(FileDescriptor file) {
        files.addFile(file);
        return this;
    }

    /**
     * Adds protobuf service definition. This is what the generated
     * <c>bindService</c> method returns.
     *
     * @param service service definition returned by <c>bindService</c>
     * @return this builder instance
     */
    public BridgeBuilder addService(ServerServiceDefinition service) {
        services.add(service);
        return this;
    }

    /**
     * Creates new instance of the {@link Bridge}.
     *
     * @return built bride instance
     */
    @SuppressWarnings("unchecked")
    public Bridge build() {
        List<Route> routes = new ArrayList<>();

        for (ServerServiceDefinition service : services) {
            for (ServerMethodDefinition<?, ?> method : service.getMethods()) {
                Route route = files.routeFor(service, (ServerMethodDefinition<Message, Message>) method);
                routes.add(route);
            }
        }

        return new Bridge(routes);
    }
}
