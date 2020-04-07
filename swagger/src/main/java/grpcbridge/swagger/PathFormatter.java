package grpcbridge.swagger;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;

import java.util.List;

/**
 * For formatting path according to config.formatter
 */
class PathFormatter {
    /**
     * Formats path according to path parameters and config.formatter
     * @param path initial path
     * @param pathParameters path parameters
     * @param config SwaggerConfig instance
     * @return formatted path
     */
    static String getPath(String path,
                          List<FieldDescriptor> pathParameters,
                          SwaggerConfig config) {
        for (Descriptors.FieldDescriptor fieldDescriptor : pathParameters) {
            path = path.replace(
                    String.format("{%s}", fieldDescriptor.getName()),
                    String.format("{%s}", config.formatFieldName(fieldDescriptor)));
        }
        return path;
    }
}
