package grpcbridge.swagger.model;

import static java.util.Collections.emptyMap;

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
    private final Property additionalProperties;

    SwaggerModel(Type type, Map<String, Property> properties, Property additionalProperties) {
        this.type = type;
        this.properties = properties;
        this.additionalProperties = additionalProperties;
    }

    public static SwaggerModel forMessage() {
        return new SwaggerModel(Type.OBJECT, new TreeMap<>(), null);
    }

    public static SwaggerModel empty() {
        return new SwaggerModel(Type.ARRAY, new TreeMap<>(), null);
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

    public SwaggerModel asMapDefinition() {
        if (type != Type.OBJECT) {
            throw new IllegalArgumentException("Non-object model is not a map: " + type);
        }

        Property key = properties.get("key");
        Property value = properties.get("value");
        if (key == null || value == null) {
            throw new IllegalArgumentException("Model is not a map entry, must have key/value");
        }
        if (key.type != Type.STRING) {
            throw new IllegalArgumentException("Non-string map key not supported: " + key.type);
        }
        if (properties.size() != 2) {
            throw new IllegalArgumentException(
                "Model is not a map entry, must have exactly two fields"
            );
        }

        return new SwaggerModel(Type.OBJECT, emptyMap(), value);
    }
}
