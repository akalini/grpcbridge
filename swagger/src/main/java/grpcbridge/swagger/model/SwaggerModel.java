package grpcbridge.swagger.model;

import grpcbridge.swagger.model.Property.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        return new SwaggerModel(Type.OBJECT, new TreeMap<>());
    }

    public static SwaggerModel empty() {
        return new SwaggerModel(Type.ARRAY, new TreeMap<>());
    }

    public Property getProperty(String name) {
        return properties.get(name);
    }

    public void putProperty(String name, Property property, boolean isRequired) {
        properties.put(name, property);
        if (isRequired) {
            required.add(name);
        }
    }

    public void remove(String name) {
        properties.remove(name);
        required.remove(name);
    }

    public boolean hasProperties() {
        return properties.size() > 0;
    }
}
