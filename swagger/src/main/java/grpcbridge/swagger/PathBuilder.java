package grpcbridge.swagger;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.util.List;

public class PathBuilder {
    private final String path;
    private final List<FieldDescriptor> pathParameters;
    private final SwaggerConfig config;

    private PathBuilder(
            String path,
            List<FieldDescriptor> pathParameters, SwaggerConfig config) {
        this.path = path;
        this.pathParameters = pathParameters;
        this.config = config;
    }

    public static PathBuilder create(
            String path,
            List<FieldDescriptor> pathParameters,
            SwaggerConfig config) {
        return new PathBuilder(path, pathParameters, config);
    }

    public String getPath() {
        String newPath = path;
        for (Descriptors.FieldDescriptor fieldDescriptor : pathParameters) {
            newPath = newPath.replace(
                    String.format("{%s}", fieldDescriptor.getName()),
                    String.format("{%s}", config.formatFieldName(fieldDescriptor)));
        }
        return newPath;
    }
}
