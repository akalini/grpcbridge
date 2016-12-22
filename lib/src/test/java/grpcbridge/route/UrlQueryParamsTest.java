package grpcbridge.route;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class UrlQueryParamsTest {
    @Test
    public void simple() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of());
        assertThat(query.extractVars(ImmutableMap.of())).isEmpty();
    }

    @Test
    public void param() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of("p1", list("v1")));
        assertThat(query.containsAll(list("p1"))).isTrue();
        assertThat(query.extractVars(ImmutableMap.of("p1", list("")))).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void param_duplicate() {
        new UrlQueryParams(ImmutableMap.of("p1", list("v1", "v2")));
    }

    @Test
    public void paramWithValue() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of("p1", list("{pp1}")));
        assertThat(query.containsAll(list("p1"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of("p1", list(""))))
                .containsExactly(new Variable("pp1", ""));
    }

    @Test
    public void multipleParams() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of(
                "p1", list("{pp1}"),
                "p2", list("{pp2}"),
                "p3", list("{pp3}")));
        assertThat(query.containsAll(asList("p1", "p2", "p3"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "p1", list("v1"),
                        "p2", list(""),
                        "p3", list("v3"))))
                .containsExactly(
                        new Variable("pp1", "v1"),
                        new Variable("pp2", ""),
                        new Variable("pp3", "v3"));
    }

    @Test
    public void multipleParams_multipleValues() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of(
                "p1", list("{pp1}"),
                "p2", list("{pp2}"),
                "p3", list("{pp3}")));
        assertThat(query.containsAll(asList("p1", "p2", "p3"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "p1", list("v1", "v11", "v111"),
                        "p2", list(""),
                        "p3", list("v3"))))
                .containsExactly(
                        new Variable("pp1", "v1"),
                        new Variable("pp1", "v11"),
                        new Variable("pp1", "v111"),
                        new Variable("pp2", ""),
                        new Variable("pp3", "v3"));
    }

    @Test
    public void multipleParams_caseInsensitive() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of(
                "param_one", list("{p_one}"),
                "param_two", list("{p_two}")));

        assertThat(query.containsAll(asList("param_one", "param_two"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "param_one", list("one"),
                        "param_two", list("two"))))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));

        assertThat(query.containsAll(asList("param-one", "param-two"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "param-one", list("one"),
                        "param-two", list("two"))))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));

        assertThat(query.containsAll(asList("paramOne", "paramTwo"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "paramOne", list("one"),
                        "paramTwo", list("two"))))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));
    }

    private static <T> List<T> list(T t) {
        return Collections.singletonList(t);
    }

    private static <T> List<T> list(T ... t) {
        return Arrays.asList(t);
    }
}