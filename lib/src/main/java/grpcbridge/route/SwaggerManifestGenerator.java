package grpcbridge.route;

import java.util.List;

/**
 * Generates an Swagger compatible API specification from the given routes.
 */
public interface SwaggerManifestGenerator {
    String generate(List<Route> routes);
}
