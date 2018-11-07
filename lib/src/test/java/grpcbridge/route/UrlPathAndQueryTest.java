package grpcbridge.route;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

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
        assertThat(pathAndQuery.query()).containsEntry("p1", list(""));
    }

    @Test
    public void param_multiple() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1&p1&p1");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", list("", "", ""));
    }

    @Test
    public void paramWithValue() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={pp1}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", list("{pp1}"));
    }

    @Test
    public void paramWithValue_multiple() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={pp1}&p1={pp1}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", list("{pp1}", "{pp1}"));
    }

    @Test
    public void paramWithValue_URLEncoded() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery
                .parse("/simple?p1={%20%22%25%2D%2E%3C%3E%5C%5E%5F%60%7B%7C%7D%7E}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", list("{ \"%-.<>\\^_`{|}~}"));
    }

    @Test
    public void paramWithValue_reservedCharacters() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={!*'();:@+$,/#[]}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query()).containsEntry("p1", list("{!*'();:@+$,/#[]}"));
    }

    @Test
    public void multipleParams() {
        UrlPathAndQuery pathAndQuery = UrlPathAndQuery.parse("/simple?p1={pp1}&p2={pp2}&p3={pp3}");
        assertThat(pathAndQuery.path()).isEqualTo("/simple");
        assertThat(pathAndQuery.query())
                .containsEntry("p1", list("{pp1}"))
                .containsEntry("p2", list("{pp2}"))
                .containsEntry("p3", list("{pp3}"));
    }

    @Test
    public void decode() {
        assertThat(UrlPathAndQuery.decode("%20%22%25%2D%2E%3C%3E%5C%5E%5F%60%7B%7C%7D%7E"))
                .isEqualTo(" \"%-.<>\\^_`{|}~");
    }

    private static <T> List<T> list(T t) {
        return Collections.singletonList(t);
    }

    private static <T> List<T> list(T ... t) {
        return Arrays.asList(t);
    }
}
