package net.uiqui.embedhttp.server.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HttpVersionTest {
    @ParameterizedTest
    @MethodSource("httpVersions")
    void testFromString(String value, HttpVersion expected) {
        // when
        var result = HttpVersion.fromString(value);
        // then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> httpVersions() {
        return Stream.of(
                // string, value
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of("invalid", null),
                Arguments.of("http/1.0", null),  // lowercase should be rejected
                Arguments.of("HTTP/1.0", HttpVersion.VERSION_1_0),
                Arguments.of("http/1.1", null),  // lowercase should be rejected
                Arguments.of("HTTP/1.1", HttpVersion.VERSION_1_1)
        );
    }
}