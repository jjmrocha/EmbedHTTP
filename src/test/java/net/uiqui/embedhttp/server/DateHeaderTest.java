package net.uiqui.embedhttp.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class DateHeaderTest {
    private MockedStatic<Now> mockedNow;

    @BeforeEach
    void setUp() {
        mockedNow = mockStatic(Now.class);
    }

    @AfterEach
    void tearDown() {
        mockedNow.close();
    }

    @Test
    void testGetDateHeaderValue() {
        // given
        var classUnderTest = new DateHeader();
        var now = ZonedDateTime.parse("2023-10-01T12:00:00.100Z");
        mockedNow.when(Now::asZonedDateTime).thenReturn(now);
        // when
        var result = classUnderTest.getDateHeaderValue();
        // then
        assertThat(result).isEqualTo("Sun, 01 Oct 2023 12:00:00 GMT");
    }

    @ParameterizedTest
    @MethodSource("formatDates")
    void testFormatDate(String input, String expected) {
        // given
        var classUnderTest = new DateHeader();
        var data = ZonedDateTime.parse(input);
        // when
        var result = classUnderTest.formatDate(data);
        // then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> formatDates() {
        return Stream.of(
                // input, expected
                Arguments.of("2023-10-01T12:00:00Z", "Sun, 01 Oct 2023 12:00:00 GMT"),
                Arguments.of("2024-05-06T03:05:07Z", "Mon, 06 May 2024 03:05:07 GMT")
        );
    }
}