package grpcbridge.swagger;

import static java.util.stream.Collectors.joining;

import com.google.api.AnnotationsProto;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.MethodDescriptor;

import grpcbridge.http.BridgeHttpRule;
import grpcbridge.route.SwaggerManifestGenerator;
import grpcbridge.route.Route;
import grpcbridge.swagger.model.SwaggerRoute;
import grpcbridge.swagger.model.SwaggerSchema;
import java.util.List;

/**
 * Generates a Swagger 2.0 compatible API specification for the given routes.
 */
public final class BridgeSwaggerManifestGenerator implements SwaggerManifestGenerator {
    public String generate(List<Route> routes) {
        String serviceName = routes.stream()
            .map(Route::getService)
            .distinct()
            .collect(joining(", "));

        SwaggerSchema schema = SwaggerSchema.create(serviceName, "1.0");

        routes.forEach(route -> {
            MethodDescriptor descriptor = route.descriptor;
            DescriptorProtos.MethodOptions options = descriptor.getOptions();
            BridgeHttpRule rule = BridgeHttpRule.create(
                options.getExtension(AnnotationsProto.http)
            );

            ParametersBuilder parameters = ParametersBuilder.create(descriptor, rule);
            schema.putAllModels(parameters.getModelDefinitions());
            schema.addRoute(
                rule.getPath(),
                rule.getMethod(),
                SwaggerRoute.create(descriptor, parameters.getParameters())
            );
        });

        return schema.serialize();
    }
}
