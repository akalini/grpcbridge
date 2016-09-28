package grpcbridge;

import com.google.protobuf.ByteString;
import grpcbridge.Exceptions.ConfigurationException;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.Exceptions.RouteNotFoundException;
import grpcbridge.common.TestService;
import grpcbridge.http.HttpMethod;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.proto.Test.*;
import org.junit.Test;

import static grpcbridge.common.TestFactory.*;
import static grpcbridge.test.proto.Test.Enum.INVALID;
import static grpcbridge.util.ProtoJson.parse;
import static grpcbridge.util.ProtoJson.serialize;
import static org.assertj.core.api.Assertions.assertThat;

public class BridgeTest {
    private TestService testService = new TestService();
    private Bridge bridge = Bridge.builder()
            .addFile(grpcbridge.test.proto.Test.getDescriptor())
            .addService(testService.bindService())
            .build();

    @Test
    public void get() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void get_withParams() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get?string_field=hello&int_field=987")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withSuffix() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get/hello/suffix")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void get_static() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get-static")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("string")
                .build()));
    }

    @Test
    public void get_multipleParams() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(HttpMethod.GET, "/get-multi/hello/123/321/1.0/3.0/false/bytes/INVALID")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(GetResponse.newBuilder()
                .setStringField("hello")
                .setIntField(123)
                .setLongField(321)
                .setFloatField(1.0f)
                .setDoubleField(3.0)
                .setBoolField(false)
                .setBytesField(ByteString.copyFrom("bytes".getBytes()))
                .setEnumField(INVALID)
                .setNested(Nested.newBuilder().setNestedField("nested"))
                .build());
    }

    @Test
    public void get_nestedMessage() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get-nested/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setNested(Nested.newBuilder()
                        .setNestedField("hello")
                        .build())
                .build()));
    }

    @Test(expected = ConfigurationException.class)
    public void get_unknownPath() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get-unknown-path/hello")
                .body(serialize(rpcRequest))
                .build();
        bridge.handle(request);
    }

    @Test(expected = ConfigurationException.class)
    public void get_unknownVariable() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get-unknown-param/hello")
                .body(serialize(rpcRequest))
                .build();
        bridge.handle(request);
    }

    @Test
    public void post() {
        PostRequest rpcRequest = newPostRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.POST, "/post/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        PostResponse rpcResponse = parse(response.getBody(), PostResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void post_customBody() {
        HttpRequest request = HttpRequest.builder(HttpMethod.POST, "/post_custom/hello")
                .body(Integer.toString(999))
                .build();

        HttpResponse response = bridge.handle(request);
        PostResponse rpcResponse = parse(response.getBody(), PostResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(PostResponse
                .newBuilder()
                .setStringField("hello")
                .setIntField(999)
                .build());
    }

    @Test
    public void put() {
        PutRequest rpcRequest = newPutRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.PUT, "/put/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        PutResponse rpcResponse = parse(response.getBody(), PutResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void delete() {
        DeleteRequest rpcRequest = newDeleteRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.DELETE, "/delete/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        DeleteResponse rpcResponse = parse(response.getBody(), DeleteResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void patch() {
        PatchRequest rpcRequest = newPatchRequest();
        HttpRequest request = HttpRequest.builder(HttpMethod.PATCH, "/patch/hello")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        PatchResponse rpcResponse = parse(response.getBody(), PatchResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test(expected = RouteNotFoundException.class)
    public void routeNotFound() {
        HttpRequest request = HttpRequest.builder(HttpMethod.DELETE, "/unknown").build();
        bridge.handle(request);
    }

    @Test(expected = ParsingException.class)
    public void invalidBody() {
        HttpRequest request = HttpRequest.builder(HttpMethod.GET, "/get/hello")
                .body("__INVALID__")
                .build();
        bridge.handle(request);
    }
}