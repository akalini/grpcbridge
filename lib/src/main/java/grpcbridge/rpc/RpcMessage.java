package grpcbridge.rpc;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import grpcbridge.Exceptions.RouteNotFoundException;
import grpcbridge.route.Variable;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor.MethodType;

import java.util.Objects;

/**
 * gRPC message (request or response) abstraction. Each message is the
 * request or response protobuf and the headers or trailers metadata.
 */
public class RpcMessage {
    private List<Message> body;
    private final Metadata metadata;
    private MethodType methodType;

    /**
     * @param body request or response protobuf message
     * @param metadata headers or trailers metadata
     */
    public RpcMessage(Message body, Metadata metadata) {
        this(Arrays.asList(body), metadata, MethodType.UNARY);
    }
    
    public RpcMessage(List<Message> body, Metadata metadata, MethodType methodType) {
        this.body = body;
        this.metadata = metadata;
        this.methodType = methodType;
    }

    /**
     * @return request or response protobuf message
     */
    public List<Message> getBody() {
        return body;
    }

    /**
     * @return headers or trailers metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    /**
     * Applies the specified variable to the underlying protobuf message.
     * If this is a stream RpcMessage, then this will set the var on every message.
     * To set on a single message use {@link RpcMessage#setVar(int, Variable)}
     *
     * @param var variable to set
     */
    public void setVar(Variable var) {
        for (int i = 0; i < this.body.size(); i++) {
            setVar(i, var);
        }
    }

    /**
     * Applies the specified variable to the underlying protobuf message.
     *
     * @param messageIndex The index of the message in the stream to set the variable on
     * @param var variable to set
     * 
     * throws {@link IllegalArgumentException} when an invalid index is given
     */
    public void setVar(int messageIndex, Variable var) {
        if ((messageIndex < 0) || (messageIndex >= body.size())) {
            throw new IllegalArgumentException("No message found for index " + messageIndex);
        }
        
        Message message = this.body.get(messageIndex);
        Message.Builder start = message.toBuilder();

        Message.Builder current = start;

        for (String segment : var.getFieldPath()) {
            FieldDescriptor field = current.getDescriptorForType().findFieldByName(segment);
            if (field == null) {
                throw new RouteNotFoundException(format(
                        "Invalid variable path: %s, looking for: %s",
                        var,
                        segment));
            }
            current = current.getFieldBuilder(field);
        }

        FieldDescriptor field = var.getFieldNames()
                .stream()
                .map(current.getDescriptorForType()::findFieldByName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new RouteNotFoundException("Invalid variable path: " + var));

        if (field.isRepeated()) {
            current.addRepeatedField(field, var.valueAs(field));
        } else {
            current.setField(field, var.valueAs(field));
        }
        this.body.set(messageIndex, start.build());
    }

    @Override
    public String toString() {
        return format("{%s} %s", body, metadata);
    }
}
