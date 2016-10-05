package grpcbridge.route;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlQueryParamsTest {
    @Test
    public void simple() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of());
        assertThat(query.extractVars(ImmutableMap.of())).isEmpty();
    }

    @Test
    public void param() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of("p1", "v1"));
        assertThat(query.containsAll(singletonList("p1"))).isTrue();
        assertThat(query.extractVars(ImmutableMap.of("p1", ""))).isEmpty();
    }

    @Test
    public void paramWithValue() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of("p1", "{pp1}"));
        assertThat(query.containsAll(singletonList("p1"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of("p1", "")))
                .containsExactly(new Variable("pp1", ""));
    }

    @Test
    public void multipleParams() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of(
                "p1", "{pp1}",
                "p2", "{pp2}",
                "p3", "{pp3}"));
        assertThat(query.containsAll(asList("p1", "p2", "p3"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "p1", "v1",
                        "p2", "",
                        "p3", "v3")))
                .containsExactly(
                        new Variable("pp1", "v1"),
                        new Variable("pp2", ""),
                        new Variable("pp3", "v3"));
    }

    @Test
    public void multipleParams_caseInsensitive() {
        UrlQueryParams query = new UrlQueryParams(ImmutableMap.of(
                "param_one", "{p_one}",
                "param_two", "{p_two}"));

        assertThat(query.containsAll(asList("param_one", "param_two"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "param_one", "one",
                        "param_two", "two")))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));

        assertThat(query.containsAll(asList("param-one", "param-two"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "param-one", "one",
                        "param-two", "two")))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));

        assertThat(query.containsAll(asList("paramOne", "paramTwo"))).isTrue();
        Assertions
                .assertThat(query.extractVars(ImmutableMap.of(
                        "paramOne", "one",
                        "paramTwo", "two")))
                .containsOnly(
                        new Variable("p_one", "one"),
                        new Variable("p_two", "two"));
    }
}