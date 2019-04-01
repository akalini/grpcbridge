package grpcbridge.swagger;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import grpcbridge.swagger.model.Property;
import grpcbridge.swagger.model.EnumSwaggerModel;
import grpcbridge.swagger.model.SwaggerModel;
import grpcbridge.util.ProtoDescriptorTraverser;
import grpcbridge.util.ProtoVisitor;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Proto Visitor which builds a {@link SwaggerModel} for the given input type and all included
 * message types.
 */
class ModelBuilder extends ProtoVisitor {
    private final Stack<SwaggerModel> models = new Stack<>();
    private final Map<String, SwaggerModel> completeDefinitions = new HashMap<>();
    private final Descriptor rootDescriptor;
    private final SwaggerConfig config;

    static Map<String, SwaggerModel> define(Descriptor messageDescriptor, SwaggerConfig config) {
        ModelBuilder builder = new ModelBuilder(messageDescriptor, config);
        builder.traverse();
        return builder.completeDefinitions;
    }

    private ModelBuilder(Descriptor rootDescriptor, SwaggerConfig config) {
        this.rootDescriptor = rootDescriptor;
        this.config = config;
    }

    private void traverse() {
        models.push(SwaggerModel.forMessage());
        new ProtoDescriptorTraverser(this).traverse(rootDescriptor);
        completeDefinitions.put(rootDescriptor.getFullName(), models.pop());
    }

    @Override
    public void onMessageStart(FieldDescriptor field) {
        models.peek().putProperty(
            config.formatFieldName(field),
            Property.forReferenceTo(field.getMessageType()),
            config.isRequired(field)
        );

        // Open scope with new message definition
        models.push(SwaggerModel.forMessage());
    }

    @Override
    public void onMessageEnd(FieldDescriptor field) {
        // Close current message definition scope and save
        completeDefinitions.put(field.getMessageType().getFullName(), models.pop());
    }

    @Override
    public void onRepeatedFieldStart(FieldDescriptor field) {
        // Open container scope that will hold field property
        models.push(SwaggerModel.empty());
    }

    @Override
    public void onRepeatedFieldEnd(FieldDescriptor field) {
        Property repeatedType = models.pop().getProperty(config.formatFieldName(field));
        models.peek().putProperty(
            config.formatFieldName(field),
            Property.forRepeated(repeatedType),
            config.isRequired(field)
        );
    }

    @Override
    public void onSimpleField(FieldDescriptor field, SimpleFieldType type) {
        if (type == SimpleFieldType.ENUM) {
            completeDefinitions.put(
                field.getEnumType().getFullName(),
                EnumSwaggerModel.create(field.getEnumType())
            );
            models.peek().putProperty(
                config.formatFieldName(field),
                Property.forReferenceTo(field.getEnumType()),
                config.isRequired(field)
            );
        } else {
            models.peek().putProperty(
                config.formatFieldName(field),
                Property.forSimpleField(field, type),
                config.isRequired(field)
            );
        }
    }
}
