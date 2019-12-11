package grpcbridge.swagger;

import static grpcbridge.GrpcbridgeOptions.serializeDefaultValue;
import static java.util.stream.Collectors.joining;

import com.google.api.AnnotationsProto;
import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import grpcbridge.http.BridgeHttpRule;
import grpcbridge.route.Route;
import grpcbridge.route.SwaggerManifestGenerator;
import grpcbridge.swagger.OpenapiV2.Swagger;
import grpcbridge.swagger.model.SwaggerRoute;
import grpcbridge.swagger.model.SwaggerSchema;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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

        SwaggerSchema schema = SwaggerSchema.create(
                getSwaggerInfo(OpenapiV2.Info::getTitle, serviceName),
                getSwaggerInfo(OpenapiV2.Info::getVersion, "1.0"),
                getSwaggerInfo(OpenapiV2.Info::getDescription, null),
                getSwaggerField(OpenapiV2.Swagger::getHost),
                getSwaggerField(OpenapiV2.Swagger::getBasePath));

        routes
                .stream()
                .map(route -> route.descriptor)
                .filter(descriptor -> descriptor.getOptions().hasExtension(AnnotationsProto.http))
                .filter(descriptor -> !descriptor.getOptions().hasExtension(OpenapiV2.exclude)
                        || !descriptor.getOptions().getExtension(OpenapiV2.exclude))
                .forEach(descriptor -> applyMethodDescriptor(schema, descriptor));

        return schema.serialize();
    }

    private <T> T getSwaggerField(Function<OpenapiV2.Swagger,T> extractor) {
        return config.getSwaggerRoot()
                .flatMap(root -> Optional.ofNullable(extractor.apply(root)))
                .orElse(null);
    }

    private <T> T getSwaggerInfo(
            Function<OpenapiV2.Info,T> extractor,
            T defaultValue) {
        return config.getSwaggerRoot()
                .flatMap(root -> Optional.ofNullable(root.getInfo()))
                .flatMap(info -> Optional.ofNullable(extractor.apply(info)))
                .orElse(defaultValue);
    }

    private void applyMethodDescriptor(SwaggerSchema schema, MethodDescriptor descriptor) {
        BridgeHttpRule rule = BridgeHttpRule.create(
                descriptor.getOptions().getExtension(AnnotationsProto.http)
        );

        ParametersBuilder parameters = ParametersBuilder.create(descriptor, rule, config);
        schema.putAllModels(parameters.getModelDefinitions());
        schema.addRoute(
                rule.getPath(),
                rule.getMethod(),
                SwaggerRoute.create(descriptor, parameters.getParameters())
        );
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Set<GeneratedExtension<FieldOptions, Boolean>> requiredExtensions = new HashSet<>();
        private @Nullable FieldNameFormatter formatter;
        private @Nullable Swagger swaggerRoot;
        private boolean excludeDeprecated;

        /**
         * Set extension to use to mark fields as required. Optional.
         *
         * @param extension extension
         * @return this
         */
        public Builder addRequiredExtension(GeneratedExtension<FieldOptions, Boolean> extension) {
            this.requiredExtensions.add(extension);
            return this;
        }

        /**
         * Sets a flag to exclude deprecated fields. Optional.
         *
         * @return this
         */
        public Builder excludeDeprecated() {
            return setExcludeDeprecated(true);
        }

        /**
         * Sets a flag to exclude deprecated fields. Optional.
         *
         * @return this
         */
        public Builder setExcludeDeprecated(boolean excludeDeprecated) {
            this.excludeDeprecated = excludeDeprecated;
            return this;
        }

        /**
         * Sets root details to be used for swagger generation. Optional.
         *
         * @return this
         */
        public Builder setSwaggerRoot(Swagger swaggerRoot) {
            this.swaggerRoot = swaggerRoot;
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
            // Fields that print default values will always be present
            requiredExtensions.add(serializeDefaultValue);

            SwaggerConfig config = new SwaggerConfig(
                requiredExtensions,
                formatter != null ? formatter : FieldNameFormatter.snakeCase(),
                Optional.ofNullable(swaggerRoot),
                excludeDeprecated
            );
            return new BridgeSwaggerManifestGenerator(config);
        }
    }

}
