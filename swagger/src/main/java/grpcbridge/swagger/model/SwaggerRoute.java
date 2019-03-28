package grpcbridge.swagger.model;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.MethodDescriptor;

import java.util.List;
import java.util.Map;

/**
 * Defines a Swagger route. A route contains a unique name, request and response definitions, and
 * optional tags.
 */
public class SwaggerRoute {
    private final String operationId;
    private final Map<String, Response> responses;
    private final List<Parameter> parameters;
    private final List<String> tags;

    private SwaggerRoute(
        String operationId,
        Map<String, Response> responses,
        List<Parameter> parameters,
        List<String> tags
    ) {
        this.operationId = operationId;
        this.responses = responses;
        this.parameters = parameters;
        this.tags = tags;
    }

    public static SwaggerRoute create(MethodDescriptor method, List<Parameter> parameters) {
        return new SwaggerRoute(
            format("%s.%s", method.getService().getName(), method.getName()),
            ImmutableMap.of("200", Response.create("Successful response", method)),
            parameters,
            emptyList()
        );
    }

    String getName() {
        return operationId;
    }

    private static class Response {
        private final String description;
        private final ReferenceProperty schema;

        private Response(String description, ReferenceProperty schema) {
            this.description = description;
            this.schema = schema;
        }

        private static Response create(String description, MethodDescriptor methodDescriptor) {
            return new Response(
                description,
                ReferenceProperty.create(methodDescriptor.getOutputType())
            );
        }
    }
}
