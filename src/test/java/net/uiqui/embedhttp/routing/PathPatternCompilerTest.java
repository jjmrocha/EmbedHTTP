package net.uiqui.embedhttp.routing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class PathPatternCompilerTest {
    @ParameterizedTest
    @MethodSource("pathRequests")
    void testPathToRegex(String path, String regex) {
        // when
        var result = PathPatternCompiler.pathToRegex(path);
        // then
        assertThat(result).isEqualTo(regex);
    }

    @ParameterizedTest
    @MethodSource("pathRequests")
    void testCompile(String path, String regex, String example, Map<String, String> parms) {
        // when
        var compilePath = PathPatternCompiler.compile(path);
        var matcher = compilePath.matcher(example);
        // then
        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.groupCount()).isEqualTo(parms.size());

        for (var entry : parms.entrySet()) {
            assertThat(matcher.group(entry.getKey())).isEqualTo(entry.getValue());
        }
    }

    private static Stream<Arguments> pathRequests() {
        return Stream.of(
                // path, regExp, example, parms
                Arguments.of("/v1", "/v1", "/v1", emptyMap()),
                Arguments.of("/v1/resource", "/v1/resource", "/v1/resource", emptyMap()),
                Arguments.of("/v1/resource/:id", "/v1/resource/(?<id>[^/]+)", "/v1/resource/123", Map.of("id", "123")),
                Arguments.of("/v1/resource/:a1", "/v1/resource/(?<a1>[^/]+)", "/v1/resource/123", Map.of("a1", "123")),
                Arguments.of("/v1/resource/:aZ", "/v1/resource/(?<aZ>[^/]+)", "/v1/resource/123", Map.of("aZ", "123")),
                Arguments.of("/v1/resource/:Z2", "/v1/resource/(?<Z2>[^/]+)", "/v1/resource/123", Map.of("Z2", "123")),
                Arguments.of("/v1/resource/:Za", "/v1/resource/(?<Za>[^/]+)", "/v1/resource/123", Map.of("Za", "123")),
                Arguments.of("/v1/resource/:id/section", "/v1/resource/(?<id>[^/]+)/section", "/v1/resource/123/section", Map.of("id", "123")),
                Arguments.of("/v1/resource/:id/section/:name", "/v1/resource/(?<id>[^/]+)/section/(?<name>[^/]+)",
                        "/v1/resource/123/section/abc", Map.of("id", "123", "name", "abc"))
        );
    }
}