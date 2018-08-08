package grpcbridge.parser;

import com.google.protobuf.Message;
import grpcbridge.http.HttpRequest;
import grpcbridge.rpc.RpcMessage;

public interface Deserializer {
    RpcMessage deserialize(HttpRequest httpRequest, Message.Builder builder);

    boolean supported(String contentType);
}
