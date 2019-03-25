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
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import grpcbridge.Exceptions.ParsingException;
import grpcbridge.Exceptions.RouteNotFoundException;
import grpcbridge.common.TestService;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.parser.ProtoJsonConverter;
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
import io.grpc.Metadata.Key;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;

import java.util.List;
import javax.annotation.Nullable;

import org.junit.Test;

public class BridgeTest implements ProtoParseTest {

    private TestService testService = new TestService();
    private Bridge bridge = Bridge
            .builder()
            .addFile(grpcbridge.test.proto.Test.getDescriptor())
            .addService(testService.bindService())
            .build();

    private ServerInterceptor authCheck = new ServerInterceptor() {
        @Override
        public <ReqT, RespT> Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                Metadata headers,
                ServerCallHandler<ReqT, RespT> next) {
            if (headers.containsKey(Key.of("auth", ASCII_STRING_MARSHALLER))) {
                return next.startCall(call, headers);
            } else {
                call.close(Status.UNAUTHENTICATED.withDescription("No auth metadata"), headers);
                return new ServerCall.Listener<ReqT>() {};
            }
        }
    };

    @Test
    public void get() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void get_withParams() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string_field=hello&int_field=987")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withParams_reordered() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?int_field=987&string_field=hello")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withParams_camelCase() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?stringField=hello&intField=987")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withParams_trainCase() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string-field=hello&int-field=987")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withParams_partial() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string_field=hello&nested.nested_field=abc")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .setNested(Nested.newBuilder()
                    .setNestedField("abc"))
                .build()));
    }

    @Test
    public void get_withParams_encoded() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get?string_field=h%3Del%2Fl%26o&int_field=987")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("h=el/l&o")
                .setIntField(987)
                .build()));
    }

    @Test
    public void get_withParams_noParams() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.getDefaultInstance()));
    }

    @Test
    public void get_withSuffix() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello/suffix")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void get_withSuffix_encoded() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get/h%3Del%2Fl%26o/suffix")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("h=el/l&o")
                .build()));
    }

    @Test
    public void get_static() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-static")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.getDefaultInstance()));
    }

    @Test
    public void get_static_withParam() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-static?string_field=value1")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("value1")
                .build()));
    }

    @Test(expected = RouteNotFoundException.class)
    public void get_static_withParam_noMatch() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-static?param1=no_match")
                .build();

        bridge.handle(request);
    }

    @Test
    public void get_multipleParams() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-multi/hello/123/321/1.0/3.0/false/bytes/INVALID/nested")
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
        HttpRequest request = HttpRequest
                .builder(GET, "/get-nested/hello")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setNested(Nested.newBuilder()
                        .setNestedField("hello")
                        .build())
                .build()));
    }

    @Test
    public void get_repeatedParam() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-repeated?repeated_field=one&repeated_field=two")
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .addRepeatedField("one")
                .addRepeatedField("two")
                .build()));
    }

    @Test(expected = RouteNotFoundException.class)
    public void get_unknownPath() {
        GetRequest rpcRequest = newGetRequest();
        HttpRequest request = HttpRequest
                .builder(GET, "/get-unknown-path/hello")
                .body(serialize(rpcRequest))
                .build();
        bridge.handle(request);
    }

    @Test(expected = RouteNotFoundException.class)
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
    public void post_noBody() {
        HttpRequest request = HttpRequest
            .builder(POST, "/post/hello?int_field=123")
            .build();

        HttpResponse response = bridge.handle(request);
        PostResponse rpcResponse = parse(response.getBody(), PostResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(PostRequest.newBuilder()
            .setStringField("hello")
            .setIntField(123)
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
                .builder(POST, "/post/hello")
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

    @Test
    public void getWithInterceptor() {
        Bridge bridge = Bridge
                .builder()
                .addFile(grpcbridge.test.proto.Test.getDescriptor())
                .addService(testService.bindService())
                .addInterceptor(authCheck)
                .build();

        /* Try without Auth header */
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello")
                .build();

        try {
            bridge.handle(request);
            fail("Did not throw expected StatusRuntimeException");
        } catch (StatusRuntimeException ex) {
            assertThat(ex.getStatus().getCode()).isEqualTo(Code.UNAUTHENTICATED);
        }

        /* Next put auth header */
        Metadata headers = new Metadata();
        headers.put(Key.of("auth", ASCII_STRING_MARSHALLER), "token");
        request = HttpRequest
                .builder(GET, "/get/hello")
                .headers(headers)
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());
        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .build()));
    }

    @Test
    public void getStream() {
        HttpRequest request = HttpRequest
                .builder(GET, "/get-stream/hello?int_field=2")
                .build();

        HttpResponse response = bridge.handle(request);

        List<GetResponse> responses = parseStream(response.getBody(), GetResponse.newBuilder());
        assertThat(responses.size()).isEqualTo(2);
        assertThat(responses.get(0).getStringField()).isEqualTo("hello");
        assertThat(responses.get(0).getIntField()).isEqualTo(0);
        assertThat(responses.get(1).getStringField()).isEqualTo("hello");
        assertThat(responses.get(1).getIntField()).isEqualTo(1);
    }

    @Test
    public void get_withTracing() {
        Metadata headers = new Metadata();
        headers.put(
                Key.of("X-Cloud-Trace-Context", ASCII_STRING_MARSHALLER),
                "105445aa7843bc8bf206b12000100000/123");
        HttpRequest request = HttpRequest
                .builder(GET, "/get/hello")
                .headers(headers)
                .build();

        HttpResponse response = bridge.handle(request);
        GetResponse rpcResponse = parse(response.getBody(), GetResponse.newBuilder());

        assertThat(rpcResponse).isEqualTo(responseFor(GetRequest.newBuilder()
                .setStringField("hello")
                .build()));
    }

    private <T extends Message> List<T> parseStream(@Nullable String body, T.Builder builder) {
        return ProtoJsonConverter.INSTANCE.parseStream(body, Charsets.UTF_8, builder);
    }
}
