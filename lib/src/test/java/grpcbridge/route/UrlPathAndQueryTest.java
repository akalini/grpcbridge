package grpcbridge.route;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlPathAndQueryTest {
    @Test
    public void root() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/");
        assertThat(pathAndQuery.path()).isEqualTo("/");
        assertThat(pathAndQuery.query()).isEmpty();
    }

    @Test
    public void simple() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).isEmpty();
    }

    @Test
    public void param() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", "");
    }

    @Test
    public void paramWithValue() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={pp1}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", "{pp1}");
    }

    @Test
    public void multipleParams() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={pp1}&p2={pp2}&p3={pp3}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query())
                .containsEntry("p1", "{pp1}")
                .containsEntry("p2", "{pp2}")
                .containsEntry("p3", "{pp3}");
    }
}