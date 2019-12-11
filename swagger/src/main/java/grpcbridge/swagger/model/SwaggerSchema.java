package grpcbridge.swagger.model;

import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import grpcbridge.http.HttpMethod;
import grpcbridge.swagger.gson.LowercaseEnumTypeAdapterFactory;
import grpcbridge.swagger.model.Parameter.Location;
import grpcbridge.swagger.model.Property.Type;
import grpcbridge.swagger.model.RepeatedQueryParameter.CollectionFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Full Swagger schema definition. Supports serialization.
 */
public class SwaggerSchema {
    private static final Gson gson = new GsonBuilder()
        .enableComplexMapKeySerialization()
        .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory(ImmutableSet.of(
            CollectionFormat.class,
            HttpMethod.class,
            Location.class,
            Type.class
        )))
        .create();
    private final String swagger = "2.0";
    private final List<String> schemes = singletonList("https");
    private final List<String> consumes = singletonList("application/json");
    private final List<String> produces = singletonList("application/json");
    private final Map<String, Map<HttpMethod, SwaggerRoute>> paths = new TreeMap<>();
    private final Map<String, SwaggerModel> definitions = new TreeMap<>();
    private final InfoJson info;
    private final String host;
    private final String basePath;

    private SwaggerSchema(InfoJson info, String host, String basePath) {
        this.info = info;
        this.host = host;
        this.basePath = basePath;
    }

    public static SwaggerSchema create(String title, String version) {
        return create(title, version, null, null, null);
    }

    public static SwaggerSchema create(
            String title,
            String version,
            String description,
            String host,
            String basePath) {
        return new SwaggerSchema(new InfoJson(title, version, description), host, basePath);
    }

    public void addRoute(String path, HttpMethod method, SwaggerRoute route) {
        paths.putIfAbsent(path, new TreeMap<>());
        if (paths.get(path).containsKey(method)) {
            throw new IllegalStateException(
                String.format("Duplicate definition for %s %s found", method, route.getName())
            );
        }
        paths.get(path).put(method, route);
    }

    public void putAllModels(Map<String, SwaggerModel> models) {
        this.definitions.putAll(models);
    }

    public String serialize() {
        return gson.toJson(this);
    }

    private static class InfoJson {
        private final String title;
        private final String version;
        private final String description;

        private InfoJson(
                String title,
                String version,
                String description) {
            this.title = title;
            this.version = version;
            this.description = description;
        }
    }
}

