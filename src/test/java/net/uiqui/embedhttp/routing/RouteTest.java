package net.uiqui.embedhttp.routing;

import net.uiqui.embedhttp.api.HttpMethod;
import net.uiqui.embedhttp.api.HttpRequestHandler;
import net.uiqui.embedhttp.api.HttpResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RouteTest {
    @ParameterizedTest
    @MethodSource("pathRequests")
    void testBuilder(String path, Pattern regex) {
        // given
        HttpRequestHandler handler = (req) -> HttpResponse.noContent();
        // when
        var result = new Route(HttpMethod.GET, path, handler);
        // then
        assertThat(result.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(result.getPathPattern()).isEqualTo(path);
        assertThat(result.getPathRegexPattern().toString()).isEqualTo(regex.toString());
        assertThat(result.getHandler()).isEqualTo(handler);
    }

    private static Stream<Arguments> pathRequests() {
        return Stream.of(
                // path, regExp
                Arguments.of("/v1", Pattern.compile("/v1")),
                Arguments.of("/v1/resource", Pattern.compile("/v1/resource")),
                Arguments.of("/v1/resource/:id", Pattern.compile("/v1/resource/(?<id>[^/]+)")),
                Arguments.of("/v1/resource/:id/", Pattern.compile("/v1/resource/(?<id>[^/]+)/")),
                Arguments.of("/v1/resource/:id/section", Pattern.compile("/v1/resource/(?<id>[^/]+)/section")),
                Arguments.of("/v1/resource/:id/section/:name", Pattern.compile("/v1/resource/(?<id>[^/]+)/section/(?<name>[^/]+)"))
        );
    }
}