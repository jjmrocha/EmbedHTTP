package net.uiqui.embedhttp.server.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerStateHolderTest {
    @ParameterizedTest
    @MethodSource("statusCombinations")
    public void testIsValidStatusChange(ServerState currentServerState, ServerState newServerState, boolean expected) {
        // given
        var classUnderTest = new StateMachine(currentServerState);
        // when
        var result = classUnderTest.isValidStateChange(currentServerState, newServerState);
        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetCurrentStatus() {
        // given
        var classUnderTest = new StateMachine(ServerState.STARTING);
        // when
        var result = classUnderTest.getCurrentState();
        // then
        assertThat(result).isEqualTo(ServerState.STARTING);
    }

    @ParameterizedTest
    @MethodSource("statusCombinations")
    public void testSetStatus(ServerState initialServerState, ServerState newServerState, boolean shouldChange) {
        // given
        var classUnderTest = new StateMachine(initialServerState);
        // when
        var result = classUnderTest.setState(newServerState);
        var currentStatus = classUnderTest.getCurrentState();
        // then
        assertThat(result).isEqualTo(shouldChange);

        if (shouldChange) {
            assertThat(currentStatus).isEqualTo(newServerState);
        } else {
            assertThat(currentStatus).isEqualTo(initialServerState);
        }
    }

    @Test
    public void testWaitForStatus() throws InterruptedException {
        // given
        var classUnderTest = new StateMachine(ServerState.STARTING);
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                classUnderTest.setState(ServerState.RUNNING);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        var currentStatus = classUnderTest.waitForState(ServerState.RUNNING);
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(currentStatus).isEqualTo(ServerState.RUNNING);
        assertThat(waitTime).isGreaterThanOrEqualTo(100);
    }

    @Test
    public void testWaitForStatusWithMultipleWantedStatus() throws InterruptedException {
        // given
        var classUnderTest = new StateMachine(ServerState.STARTING);
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                classUnderTest.setState(ServerState.STOPPED);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        var currentStatus = classUnderTest.waitForState(ServerState.RUNNING, ServerState.STOPPED);
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(currentStatus).isEqualTo(ServerState.STOPPED);
        assertThat(waitTime).isGreaterThanOrEqualTo(100);
    }

    private static Stream<Arguments> statusCombinations() {
        return Stream.of(
                // currentStatus, newStatus, expected
                Arguments.of(ServerState.STARTING, ServerState.STARTING, false),
                Arguments.of(ServerState.STARTING, ServerState.RUNNING, true),
                Arguments.of(ServerState.STARTING, ServerState.STOPPING, false),
                Arguments.of(ServerState.STARTING, ServerState.STOPPED, true),
                Arguments.of(ServerState.RUNNING, ServerState.STARTING, false),
                Arguments.of(ServerState.RUNNING, ServerState.RUNNING, false),
                Arguments.of(ServerState.RUNNING, ServerState.STOPPING, true),
                Arguments.of(ServerState.RUNNING, ServerState.STOPPED, true),
                Arguments.of(ServerState.STOPPING, ServerState.STARTING, false),
                Arguments.of(ServerState.STOPPING, ServerState.RUNNING, false),
                Arguments.of(ServerState.STOPPING, ServerState.STOPPING, false),
                Arguments.of(ServerState.STOPPING, ServerState.STOPPED, true),
                Arguments.of(ServerState.STOPPED, ServerState.STARTING, true),
                Arguments.of(ServerState.STOPPED, ServerState.RUNNING, false),
                Arguments.of(ServerState.STOPPED, ServerState.STOPPING, false),
                Arguments.of(ServerState.STOPPED, ServerState.STOPPED, false)
        );
    }
}
