package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpMethod;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTest {
    @ParameterizedTest
    @MethodSource("requestUrls")
    void testBuilder(String url, String path, String query) {
        // given
        var method = HttpMethod.GET;
        var headers = Map.of("header1", "value1", "header2", "value2");
        var body = "request body";
        // when
        var result = new Request(method, url, headers, body);
        // then
        assertThat(result.getMethod()).isEqualTo(method);
        assertThat(result.getUrl()).isEqualTo(url);
        assertThat(result.getHeaders()).isEqualTo(headers);
        assertThat(result.getBody()).isEqualTo(body);
        assertThat(result.getPath()).isEqualTo(path);
        assertThat(result.getQuery()).isEqualTo(query);
    }

    private static Stream<Arguments> requestUrls() {
        return Stream.of(
                // url, path, query
                Arguments.of("/", "/", ""),
                Arguments.of("/resource/123", "/resource/123", ""),
                Arguments.of("/resource/123?", "/resource/123", ""),
                Arguments.of("/resource?name=value", "/resource", "name=value"),
                Arguments.of("/resource?name1=value1&name2=value2", "/resource", "name1=value1&name2=value2")
        );
    }
}