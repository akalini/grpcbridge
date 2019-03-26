package grpcbridge.swagger.model;

import grpcbridge.swagger.model.Property.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines a Swagger model.
 */
public class SwaggerModel {
    private final Type type;
    private final Map<String, Property> properties;
    private final List<String> required = new LinkedList<>();

    SwaggerModel(Type type, Map<String, Property> properties) {
        this.type = type;
        this.properties = properties;
    }

    public static SwaggerModel forMessage() {
        return new SwaggerModel(Type.OBJECT, new HashMap<>());
    }

    public static SwaggerModel empty() {
        return new SwaggerModel(Type.ARRAY, new HashMap<>());
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }

    public void putProperty(String name, Property property) {
        properties.put(name, property);
    }
}
