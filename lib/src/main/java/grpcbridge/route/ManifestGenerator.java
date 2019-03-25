package grpcbridge.route;

import java.util.List;

public interface ManifestGenerator {
    String generate(List<Route> routes);
}
