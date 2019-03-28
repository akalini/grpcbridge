package grpcbridge.swagger;

import static java.util.stream.Collectors.joining;

import com.google.api.AnnotationsProto;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;

import grpcbridge.http.BridgeHttpRule;
import grpcbridge.route.SwaggerManifestGenerator;
import grpcbridge.route.Route;
import grpcbridge.swagger.model.SwaggerRoute;
import grpcbridge.swagger.model.SwaggerSchema;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Generates a Swagger 2.0 compatible API specification for the given routes.
 */
public final class BridgeSwaggerManifestGenerator implements SwaggerManifestGenerator {
    private final SwaggerConfig config;

    private BridgeSwaggerManifestGenerator(SwaggerConfig config) {
        this.config = config;
    }

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

            ParametersBuilder parameters = ParametersBuilder.create(descriptor, rule, config);
            schema.putAllModels(parameters.getModelDefinitions());
            schema.addRoute(
                rule.getPath(),
                rule.getMethod(),
                SwaggerRoute.create(descriptor, parameters.getParameters())
            );
        });

        return schema.serialize();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private @Nullable GeneratedExtension<FieldOptions, Boolean> requiredExtension;
        private @Nullable FieldNameFormatter formatter;

        /**
         * Set extension to use to mark fields as required. Optional.
         *
         * @param extension extension
         * @return this
         */
        public Builder setRequiredExtension(GeneratedExtension<FieldOptions, Boolean> extension) {
            this.requiredExtension = extension;
            return this;
        }

        /**
         * Set field formatter.
         *
         * @param formatter field formatter
         * @return this
         */
        public Builder setFormatter(FieldNameFormatter formatter) {
            this.formatter = formatter;
            return this;
        }

        /**
         * Creates a {@link BridgeSwaggerManifestGenerator} instance.
         *
         * @return a {@link BridgeSwaggerManifestGenerator} instance
         */
        public BridgeSwaggerManifestGenerator build() {
            SwaggerConfig config = new SwaggerConfig(
                requiredExtension,
                formatter != null ? formatter : FieldNameFormatter.snakeCase()
            );
            return new BridgeSwaggerManifestGenerator(config);
        }
    }

}
