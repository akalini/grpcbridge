package grpcbridge.sample;

import grpcbridge.Bridge;
import grpcbridge.BridgeBuilder;
import grpcbridge.Exceptions.BridgeException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import grpcbridge.http.HttpMethod;
import grpcbridge.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import static spark.Spark.*;

/**
 * A sample application that demonstrates setting up gRPC {@link Bridge} for
 * bridging HTTP RESTful API the corresponding gRPC implementation.
 *
 * <p>
 * The HTTP endpoint is implemented using <a href="http://sparkjava.com/">Spark</a>.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        // Create service implementation instance.
        Sample sample = new Sample();

        // Create gRPC server, bind the service implementation and start the server.
        Server rpcServer = ServerBuilder
                .forPort(9000)
                .addService(sample.bindService())
                .build();
        rpcServer.start();


        // Create new HTTP to gRPC bridge.
        Bridge bridge = new BridgeBuilder()
                .addFile(grpcbridge.sample.proto.Sample.getDescriptor())
                .addService(sample.bindService())
                .build();

        // Map Spark HTTP endpoints to the Bridge.
        get("/*", (req, res) -> handle(bridge, req));
        post("/*", (req, res) -> handle(bridge, req));
        put("/*", (req, res) -> handle(bridge, req));
        delete("/*", (req, res) -> handle(bridge, req));
        patch("/*", (req, res) -> handle(bridge, req));
        exception(
                BridgeException.class,
                (error, req, res) -> logger.warn("Bridge error: " + error.getMessage()));
        exception(
                RuntimeException.class,
                (error, req, res) -> logger.error("Unhandled exception", error));
    }

    private static String handle(Bridge bridge, Request req) {
        HttpRequest httpRequest = HttpRequest
                .builder(HttpMethod.valueOf(req.requestMethod()), req.pathInfo())
                .body(req.body())
                .build();
        return bridge.handle(httpRequest).getBody();
    }
}
