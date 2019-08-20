package grpcbridge.swagger;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

import grpcbridge.swagger.model.Property;
import grpcbridge.swagger.model.EnumSwaggerModel;
import grpcbridge.swagger.model.SwaggerModel;
import grpcbridge.util.ProtoDescriptorTraverser;
import grpcbridge.util.ProtoVisitor;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Proto Visitor which builds a {@link SwaggerModel} for the given input type and all included
 * message types.
 */
class ModelBuilder extends ProtoVisitor {
    private final Stack<SwaggerModel> models = new Stack<>();
    private final Map<String, SwaggerModel> completeDefinitions = new TreeMap<>();
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
        // Close current message definition scope and save. If this model is a definition of a
        // map entry, convert the model to a map definition.
        SwaggerModel model = field.isMapField() ? models.pop().asMapDefinition() : models.pop();
        completeDefinitions.put(field.getMessageType().getFullName(), model);
    }

    @Override
    public void onRepeatedFieldStart(FieldDescriptor field) {
        // Open container scope that will hold field property
        models.push(SwaggerModel.empty());
    }

    @Override
    public void onRepeatedFieldEnd(FieldDescriptor field) {
        SwaggerModel repeatedType = models.pop();

        // If this repeated field is a entry set of a map, convert to reference of the map
        // definition.
        Property containerType = field.isMapField()
            ? Property.forReferenceTo(field.getMessageType())
            : Property.forRepeated(repeatedType.getProperty(config.formatFieldName(field)));

        models.peek().putProperty(
            config.formatFieldName(field),
            containerType,
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
