package grpcbridge;

import static grpcbridge.common.TestFactory.newDeleteRequest;
import static grpcbridge.common.TestFactory.newGetRequest;
import static grpcbridge.common.TestFactory.newGrpcErrorRequest;
import static grpcbridge.common.TestFactory.newPatchRequest;
import static grpcbridge.common.TestFactory.newPostRequest;
import static grpcbridge.common.TestFactory.newPutRequest;
import static grpcbridge.common.TestFactory.responseFor;
import static grpcbridge.http.HttpMethod.DELETE;
import static grpcbridge.http.HttpMethod.GET;
import static grpcbridge.http.HttpMethod.PATCH;
import static grpcbridge.http.HttpMethod.POST;
import static grpcbridge.http.HttpMethod.PUT;
import static grpcbridge.test.proto.Test.Enum.INVALID;
import static grpcbridge.util.ProtoJson.parse;
import static grpcbridge.util.ProtoJson.serialize;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.protobuf.ByteString;
import grpcbridge.Exceptions.ConfigurationException;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.Exceptions.RouteNotFoundException;
import grpcbridge.common.TestService;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.test.proto.Test.DeleteRequest;
import grpcbridge.test.proto.Test.DeleteResponse;
import grpcbridge.test.proto.Test.GetRequest;
import grpcbridge.test.proto.Test.GetResponse;
import grpcbridge.test.proto.Test.GrpcErrorRequest;
import grpcbridge.test.proto.Test.Nested;
import grpcbridge.test.proto.Test.PatchRequest;
import grpcbridge.test.proto.Test.PatchResponse;
import grpcbridge.test.proto.Test.PostRequest;
import grpcbridge.test.proto.Test.PostResponse;
import grpcbridge.test.proto.Test.PutRequest;
import grpcbridge.test.proto.Test.PutResponse;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;

import org.junit.Test;

public class BridgeTest {
    private TestService testService = new TestService();
    private Bridge bridge = Bridge
            .builder()
            .addFile(grpcbridge.test.proto.Test.getDescriptor())
            .addService(testService.bindService())
            .build();

    @Test
    public void get() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello")
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
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string_field=hello&int_field=987")
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
    public void get_withParams_reordered() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get?int_field=987&string_field=hello")
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
    public void get_withParams_camelCase() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get?stringField=hello&intField=987")
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
    public void get_withParams_trainCase() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string-field=hello&int-field=987")
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
    public void get_withParams_partial() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string_field=hello")
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
    public void get_withParams_noParams() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest));
    }

    @Test
    public void get_withSuffix() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello/suffix")
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
        HttpRequest request = HttpRequest
                .builder(GET, "/get-static")
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
                .builder(GET, "/get-multi/hello/123/321/1.0/3.0/false/bytes/INVALID")
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
        HttpRequest request = HttpRequest
                .builder(GET, "/get-nested/hello")
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

    @Test
    public void get_repeatedParam() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest

                .builder(GET, "/get-repeated?repeated_field=one&repeated_field=two")
                .body(serialize(rpcRequest))
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(rpcRequest
                .toBuilder()
                .addRepeatedField("one")
                .addRepeatedField("two")
                .build()));
    }

    @Test(expected = ConfigurationException.class)
    public void get_unknownPath() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get-unknown-path/hello")
                .body(serialize(rpcRequest))
                .build();
        bridge.handle(request);
    }

    @Test(expected = ConfigurationException.class)
    public void get_unknownVariable() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get-unknown-param/hello")
                .body(serialize(rpcRequest))
                .build();
        bridge.handle(request);
    }

    @Test
    public void post() {
        PostRequest rpcRequest = newPostRequest();
        HttpRequest request = HttpRequest
                .builder(POST, "/post/hello")
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
        HttpRequest request = HttpRequest
                .builder(POST, "/post-custom/hello")
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
        HttpRequest request = HttpRequest
                .builder(PUT, "/put/hello")
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
        HttpRequest request = HttpRequest
                .builder(DELETE, "/delete/hello")
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
        HttpRequest request = HttpRequest
                .builder(PATCH, "/patch/hello")
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
        HttpRequest request = HttpRequest
                .builder(DELETE, "/unknown")
                .build();
        bridge.handle(request);
    }

    @Test(expected = ParsingException.class)
    public void invalidBody() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello")
                .body("__INVALID__")
                .build();
        bridge.handle(request);
    }

    @Test
    public void grpcErrorWithMetadata() {
        GrpcErrorRequest rpcRequest = newGrpcErrorRequest(true);
        HttpRequest request = HttpRequest
                .builder(GET, "/grpc-error?add-metadata=true")
                .body(serialize(rpcRequest))
                .build();
        try {
            bridge.handle(request);
            fail("Did not throw expected StatusRuntimeException");
        } catch (StatusRuntimeException ex) {
            assertThat(ex.getTrailers()).isNotNull();
            assertThat(ex.getTrailers()
                    .get(Metadata.Key.of("error-details", ASCII_STRING_MARSHALLER))).isNotNull();
        }
    }

    @Test
    public void grpcErrorWithoutMetadata() {
        GrpcErrorRequest rpcRequest = newGrpcErrorRequest(true);
        HttpRequest request = HttpRequest
                .builder(GET, "/grpc-error?add-metadata=false")
                .body(serialize(rpcRequest))
                .build();
        try {
            bridge.handle(request);
            fail("Did not throw expected StatusRuntimeException");
        } catch (StatusRuntimeException ex) {
            assertThat(ex.getTrailers()).isNull();
        }
    }
}