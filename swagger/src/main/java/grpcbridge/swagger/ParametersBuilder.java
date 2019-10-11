package grpcbridge.swagger;

import static java.lang.String.format;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Descriptors.MethodDescriptor;

import grpcbridge.http.BridgeHttpRule;
import grpcbridge.route.VariableExtractor;
import grpcbridge.swagger.model.Parameter;
import grpcbridge.swagger.model.Parameter.Location;
import grpcbridge.swagger.model.Property;
import grpcbridge.swagger.model.SwaggerModel;
import grpcbridge.util.ProtoDescriptorTraverser;
import grpcbridge.util.ProtoVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Proto Visitor which extracts {@link Parameter} and {@link SwaggerModel} definitions for a given
 * method.
 */
class ParametersBuilder extends ProtoVisitor {
    private final Stack<String> jsonPath = new Stack<>();
    private final List<Parameter> parameters = new LinkedList<>();
    private final Map<String, SwaggerModel> modelDefinitions = new TreeMap<>();
    private final MethodDescriptor method;
    private final SwaggerConfig config;
    private final FieldLocator locator;
    private boolean visitingRepeated = false;

    static ParametersBuilder create(
        MethodDescriptor method,
        BridgeHttpRule rule,
        SwaggerConfig config
    ) {
        ParametersBuilder builder = new ParametersBuilder(method, rule, config);
        builder.traverse();
        return builder;
    }

    private ParametersBuilder(
        MethodDescriptor method,
        BridgeHttpRule rule,
        SwaggerConfig config
    ) {
        this.method = method;
        this.config = config;
        this.locator = new FieldLocator(rule);
    }

    private void traverse() {
        new ProtoDescriptorTraverser(this).traverse(method.getInputType());
        if (locator.bodyIsWildCard) {
            // TODO: support custom body
            modelDefinitions.putAll(ModelBuilder.define(method.getInputType(), config));

            // Removes any top-level fields from request body that are included in path or query.
            // Nested fields defined in the path or query will not be removed
            SwaggerModel requestType = modelDefinitions.get(method.getInputType().getFullName());
            parameters.forEach(parameter -> requestType.remove(parameter.getName()));
            if (requestType.hasProperties()) {
                parameters.add(Parameter.forBody(Property.forReferenceTo(method.getInputType())));
            } else {
                modelDefinitions.remove(method.getInputType().getFullName());
            }
        }
        modelDefinitions.putAll(ModelBuilder.define(method.getOutputType(), config));
    }

    List<Parameter> getParameters() {
        return parameters;
    }

    Map<String, SwaggerModel> getModelDefinitions() {
        return modelDefinitions;
    }

    @Override
    public boolean accept(FieldDescriptor field) {
        return !config.isExcluded(field);
    }

    @Override
    public void onRepeatedFieldStart(FieldDescriptor field) {
        visitingRepeated = true;
        String name = fullPathName(field);
        Location location = locator.getLocation(name);

        // Repeated fields must not be in the path and for queries must be simple type or enum.
        if (location == Location.PATH) {
            throw new IllegalArgumentException(
                "Cannot put repeated field in path: " + field.getFullName()
            );
        } else if (location == Location.QUERY && field.getJavaType() == JavaType.MESSAGE) {
            throw new IllegalArgumentException(
                "Cannot put message in query: " + field.getFullName()
            );
        } else if (location == Location.BODY) {
            return;
        }

        parameters.add(Parameter.forRepeatedQuery(name, field));
    }

    @Override
    public void onRepeatedFieldEnd(FieldDescriptor field) {
        visitingRepeated = false;
    }

    @Override
    public void onMessageStart(FieldDescriptor field) {
        jsonPath.push(field.getName());
    }

    @Override
    public void onMessageEnd(FieldDescriptor field) {
        jsonPath.pop();
    }

    @Override
    public void onSimpleField(FieldDescriptor field, SimpleFieldType type) {
        if (visitingRepeated) {
            // Ignore definition of repeated field.
            return;
        }
        String name = fullPathName(field);
        Location location = locator.getLocation(name);
        if (location == Location.BODY) {
            return;
        }
        parameters.add(Parameter.forSimpleField(name, location, field, config.isRequired(field)));
    }

    private String fullPathName(FieldDescriptor field) {
        List<String> path = new ArrayList<>(jsonPath);
        Collections.reverse(path);
        path.add(config.formatFieldName(field));
        return String.join(".", path);
    }

    private static class FieldLocator {
        private static final String WILD_CARD = "*";
        private final Set<String> pathParameters;
        private final String bodyParameter;
        private final boolean bodyIsWildCard;

        private FieldLocator(BridgeHttpRule rule) {
            pathParameters = new TreeSet<>(new VariableExtractor(rule.getPath()).getPathVars());
            bodyIsWildCard = rule.getBody().equals(WILD_CARD);
            if (rule.getBody().isEmpty() || bodyIsWildCard) {
                bodyParameter = null;
            } else {
                List<String> bodyParameters = new VariableExtractor(rule.getBody()).getPathVars();
                if (bodyParameters.size() != 1) {
                    throw new IllegalArgumentException(
                        format(
                            "Body must match exactly one field (%s matched: %s)",
                            rule,
                            bodyParameters
                        )
                    );
                } else {
                    bodyParameter = bodyParameters.get(0);
                }
            }
        }

        private Location getLocation(String fullPathName) {
            if (pathParameters.contains(fullPathName)) {
                return Location.PATH;
            } else if (bodyIsWildCard) {
                return Location.BODY;
            } else if (inBodyParameter(fullPathName)) {
                // Covers selected body field and any sub fields
                return Location.BODY;
            } else {
                return Location.QUERY;
            }
        }

        private boolean inBodyParameter(String fullPathName) {
            if (bodyParameter == null) {
                return false;
            }
            return fullPathName.equals(bodyParameter)
                || fullPathName.startsWith(bodyParameter + ".");
        }
    }
}
