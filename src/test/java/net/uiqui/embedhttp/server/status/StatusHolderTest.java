package net.uiqui.embedhttp.server.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusHolderTest {
    @ParameterizedTest
    @MethodSource("statusCombinations")
    public void testIsValidStatusChange(Status currentStatus, Status newStatus, boolean expected) {
        // given
        var classUnderTest = new StatusHolder(currentStatus);
        // when
        var result = classUnderTest.isValidStatusChange(currentStatus, newStatus);
        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetCurrentStatus() {
        // given
        var classUnderTest = new StatusHolder(Status.STARTING);
        // when
        var result = classUnderTest.getCurrentStatus();
        // then
        assertThat(result).isEqualTo(Status.STARTING);
    }

    @ParameterizedTest
    @MethodSource("statusCombinations")
    public void testSetStatus(Status initialStatus, Status newStatus, boolean shouldChange) {
        // given
        var classUnderTest = new StatusHolder(initialStatus);
        // when
        var result = classUnderTest.setStatus(newStatus);
        var currentStatus = classUnderTest.getCurrentStatus();
        // then
        assertThat(result).isEqualTo(shouldChange);

        if (shouldChange) {
            assertThat(currentStatus).isEqualTo(newStatus);
        } else {
            assertThat(currentStatus).isEqualTo(initialStatus);
        }
    }

    @Test
    public void testWaitForStatus() throws InterruptedException {
        // given
        var classUnderTest = new StatusHolder(Status.STARTING);
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                classUnderTest.setStatus(Status.RUNNING);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        var currentStatus = classUnderTest.waitForStatus(Status.RUNNING);
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(currentStatus).isEqualTo(Status.RUNNING);
        assertThat(waitTime).isGreaterThanOrEqualTo(100);
    }

    @Test
    public void testWaitForStatusWithMultipleWantedStatus() throws InterruptedException {
        // given
        var classUnderTest = new StatusHolder(Status.STARTING);
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                classUnderTest.setStatus(Status.STOPPED);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        var currentStatus = classUnderTest.waitForStatus(Status.RUNNING, Status.STOPPED);
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(currentStatus).isEqualTo(Status.STOPPED);
        assertThat(waitTime).isGreaterThanOrEqualTo(100);
    }

    private static Stream<Arguments> statusCombinations() {
        return Stream.of(
                // currentStatus, newStatus, expected
                Arguments.of(Status.STARTING, Status.STARTING, false),
                Arguments.of(Status.STARTING, Status.RUNNING, true),
                Arguments.of(Status.STARTING, Status.STOPPING, false),
                Arguments.of(Status.STARTING, Status.STOPPED, true),
                Arguments.of(Status.RUNNING, Status.STARTING, false),
                Arguments.of(Status.RUNNING, Status.RUNNING, false),
                Arguments.of(Status.RUNNING, Status.STOPPING, true),
                Arguments.of(Status.RUNNING, Status.STOPPED, true),
                Arguments.of(Status.STOPPING, Status.STARTING, false),
                Arguments.of(Status.STOPPING, Status.RUNNING, false),
                Arguments.of(Status.STOPPING, Status.STOPPING, false),
                Arguments.of(Status.STOPPING, Status.STOPPED, true),
                Arguments.of(Status.STOPPED, Status.STARTING, true),
                Arguments.of(Status.STOPPED, Status.RUNNING, false),
                Arguments.of(Status.STOPPED, Status.STOPPING, false),
                Arguments.of(Status.STOPPED, Status.STOPPED, false)
        );
    }
}
