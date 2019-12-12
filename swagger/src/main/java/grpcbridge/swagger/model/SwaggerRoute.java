package grpcbridge.swagger.model;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.MethodDescriptor;
import grpcbridge.swagger.OpenapiV2;
import grpcbridge.swagger.OpenapiV2.Operation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * Defines a Swagger route. A route contains a unique name, request and response definitions, and
 * optional tags.
 */
public class SwaggerRoute {
    private final String operationId;
    private final Map<String, Response> responses;
    private final List<Parameter> parameters;
    private final List<String> tags;
    private final @Nullable String summary;
    private final @Nullable String description;
    private final @Nullable Boolean deprecated;

    private SwaggerRoute(
        String operationId,
        Map<String, Response> responses,
        List<Parameter> parameters,
        List<String> tags,
        String summary,
        String description,
        Boolean deprecated
    ) {
        this.operationId = operationId;
        this.responses = responses;
        this.parameters = parameters;
        this.tags = tags;
        this.summary = summary;
        this.description = description;
        this.deprecated = deprecated;
    }

    public static SwaggerRoute create(MethodDescriptor method, List<Parameter> parameters) {
        Optional<Operation> operation = getOperation(method);
        return new SwaggerRoute(
            format("%s.%s", method.getService().getName(), method.getName()),
            ImmutableMap.of("200", Response.create("Successful response", method)),
            parameters,
            operation.isPresent() ? operation.get().getTagsList() : emptyList(),
            operation.map(Operation::getSummary).orElse(null),
            operation.map(Operation::getDescription).orElse(null),
            operation.map(Operation::getDeprecated).orElse(null)
        );
    }

    private static Optional<Operation> getOperation(MethodDescriptor method) {
        return method.getOptions().hasExtension(OpenapiV2.operation)
                ? Optional.of(method.getOptions().getExtension(OpenapiV2.operation))
                : Optional.empty();
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
