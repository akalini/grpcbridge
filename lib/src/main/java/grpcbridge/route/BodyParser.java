package grpcbridge.route;

import com.google.api.HttpRule;
import com.google.common.base.Strings;
import com.google.protobuf.Message;
import grpcbridge.http.HttpRequest;
import grpcbridge.rpc.RpcMessage;
import grpcbridge.util.ProtoJson;

import javax.annotation.Nullable;

/**
 * Parses HTTP request body as a protobuf message. The parsing is done based
 * on the {@link HttpRule} annotation.
 *
 * <p>
 * There are several cases that we need to handle:
 *
 * <ul>
 *     <li>
 *         <c>body</c> is '*' - the whole HTTP body is parsed as the
 *         corresponding protobuf request message.
 *     </li>
 *     <li>
 *         <c>body</c> is missing. This is equivalent to the case above.
 *     </li>
 *     <li>
 *         <c>body</c> is in the '{var}' form - an empty protobuf message
 *         is created and the HTTP request body is used to set <c>var</c>
 *         field in the created protobuf.
 *     </li>
 * </ul>
 */
final class BodyParser {
    private static final String BODY_WILDCARD = "*";

    private final @Nullable VariableExtractor bodyExtractor;
    private final Message blank;

    /**
     * Creates new parser.
     *
     * @param httpRule an {@link HttpRule} annotation describing how request
     *                 body should be parsed
     * @param blank an empty protobuf instance that is used to create new
     *              request instances
     */
    public BodyParser(HttpRule httpRule, Message blank) {
        String bodyPattern = Strings.emptyToNull(httpRule.getBody());
        if (bodyPattern == null) {
            this.bodyExtractor = null;
        } else {
            if (bodyPattern.equals(BODY_WILDCARD)) {
                this.bodyExtractor = null;
            } else {
                this.bodyExtractor = new VariableExtractor(bodyPattern);
            }
        }
        this.blank = blank;
    }

    /**
     * Extracts gPRC message from the given request.
     *
     * @param request HTTP request to parse
     * @return parsed out gRPC message
     */
    public RpcMessage extract(HttpRequest request) {
        return request.getBody()
                .map(requestBody -> {
                    if (bodyExtractor == null) {
                        return ProtoJson.parse(request, blank.toBuilder());
                    } else {
                        RpcMessage result = new RpcMessage(blank, request.getHeaders());
                        bodyExtractor.extract(requestBody).forEach(result::setVar);
                        return result;
                    }
                })
                .orElse(new RpcMessage(blank, request.getHeaders()));
    }
}
