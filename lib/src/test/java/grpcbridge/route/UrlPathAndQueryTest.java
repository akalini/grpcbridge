package grpcbridge.route;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlPathAndQueryTest {
    @Test
    public void root() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/");
        assertThat(pathAndQuery.getPath()).isEqualTo("/");
        assertThat(pathAndQuery.getQueryParams()).isEmpty();
    }

    @Test
    public void simple() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams()).isEmpty();
    }

    @Test
    public void param() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams())
                .containsOnlyKeys("p1")
                .containsEntry("p1", "");
    }

    @Test
    public void params() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1=v1&p2&p3=v3");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams())
                .containsEntry("p1", "v1")
                .containsEntry("p2", "")
                .containsEntry("p3", "v3");
    }

    @Test
    public void params_camelCase() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?paramOne=value1&paramTwo=value2");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams())
                .containsEntry("paramOne", "value1")
                .containsEntry("paramTwo", "value2");
    }

    @Test
    public void params_snakeCase() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?param_one=value1&param_two=value2");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams())
                .containsEntry("paramOne", "value1")
                .containsEntry("paramTwo", "value2");
    }

    @Test
    public void params_trainCase() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?param-one=value1&param-two=value2");
        assertThat(pathAndQuery.getPath()).isEqualTo("/simple");
        assertThat(pathAndQuery.getQueryParams())
                .containsEntry("paramOne", "value1")
                .containsEntry("paramTwo", "value2");
    }
}