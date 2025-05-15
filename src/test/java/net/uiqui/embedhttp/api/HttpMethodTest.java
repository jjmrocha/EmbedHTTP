package net.uiqui.embedhttp.api;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpMethodTest {
    @ParameterizedTest
    @MethodSource("httpMethods")
    void testFromString(String value, HttpMethod expected) {
        // when
        var result = HttpMethod.fromString(value);
        // then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> httpMethods() {
        return Stream.of(
                // string, value
                Arguments.of("invalid", null),
                Arguments.of("get", HttpMethod.GET),
                Arguments.of("GET", HttpMethod.GET),
                Arguments.of("post", HttpMethod.POST),
                Arguments.of("POST", HttpMethod.POST),
                Arguments.of("put", HttpMethod.PUT),
                Arguments.of("PUT", HttpMethod.PUT),
                Arguments.of("delete", HttpMethod.DELETE),
                Arguments.of("DELETE", HttpMethod.DELETE),
                Arguments.of("patch", HttpMethod.PATCH),
                Arguments.of("PATCH", HttpMethod.PATCH),
                Arguments.of("options", HttpMethod.OPTIONS),
                Arguments.of("OPTIONS", HttpMethod.OPTIONS),
                Arguments.of("head", HttpMethod.HEAD),
                Arguments.of("HEAD", HttpMethod.HEAD)
        );
    }
}