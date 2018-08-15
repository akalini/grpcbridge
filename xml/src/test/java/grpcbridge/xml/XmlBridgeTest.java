package grpcbridge.xml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import grpcbridge.Bridge;
import grpcbridge.http.HttpRequest;
import grpcbridge.http.HttpResponse;
import grpcbridge.parser.ProtoFormDataConverter;
import grpcbridge.test.proto.TestXml;
import grpcbridge.xml.common.TestXMLService;
import io.grpc.Metadata;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static grpcbridge.http.HttpMethod.POST;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlBridgeTest {

    private TestXMLService testService = new TestXMLService();
    private Bridge bridge = Bridge
        .builder()
            .addFile(TestXml.getDescriptor())
            .addService(testService.bindService())
            .addDeserializer(ProtoFormDataConverter.INSTANCE)
            .addSerializer(ProtoXMLConverter.INSTANCE)
            .build();

    @Test
    public void handleXmlRequest() throws IOException {
        Metadata headers = new Metadata();
        headers.put(
            Metadata.Key.of("content-type", Metadata.ASCII_STRING_MARSHALLER),
            "application/x-www-form-urlencoded"
        );
        headers.put(Metadata.Key.of("accept", Metadata.ASCII_STRING_MARSHALLER), "text/xml");

        String rawBody = "first_name=John&last_name=Doe";

        HttpRequest request = HttpRequest
            .builder(POST, "/account-event/")
            .body(rawBody)
            .headers(headers)
            .build();

        HttpResponse response = bridge.handle(request);
        String raw = response.getBody();

        assertThat(response.getTrailers().get(Metadata.Key.of("content-type",
                Metadata.ASCII_STRING_MARSHALLER))).isEqualTo("text/xml; charset=utf-8");
        Map<String, Object> xml = new XmlMapper().readValue(raw,
            new TypeReference<Map<String, Object>>() {});

        assertThat(raw).startsWith("<AccountEvent>");
        assertThat(raw).endsWith("</AccountEvent>");

        assertThat(xml.get("status_code")).isEqualTo("0");
    }

    @Test
    public void handleXmlWithoutAcceptRequest() throws IOException {
        Metadata headers = new Metadata();
        headers.put(
            Metadata.Key.of("content-type", Metadata.ASCII_STRING_MARSHALLER),
            "application/x-www-form-urlencoded"
        );

        String rawBody = "first_name=John&last_name=Doe";

        HttpRequest request = HttpRequest
            .builder(POST, "/tx/")
            .body(rawBody)
            .headers(headers)
            .build();

        HttpResponse response = bridge.handle(request);
        String raw = response.getBody();
        assertThat(raw).startsWith("<Transaction>");
        assertThat(raw).endsWith("</Transaction>");

        assertThat(response.getTrailers().get(Metadata.Key.of("content-type",
                Metadata.ASCII_STRING_MARSHALLER))).isEqualTo("text/xml; charset=utf-8");
        Map<String, String> xml = new XmlMapper().readValue(raw,
                new TypeReference<Map<String, String>>() {
                });

        assertThat(xml.get("status_code")).isEqualTo("0");
        assertThat(xml.get("has_money")).isEqualTo("true");
        assertThat(xml.get("description")).isEqualTo("test transaction");
        assertThat(new BigDecimal(xml.get("amount"))).isEqualByComparingTo(new BigDecimal("25.50"));
    }
}
