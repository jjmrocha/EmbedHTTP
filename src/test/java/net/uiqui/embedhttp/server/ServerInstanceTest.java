package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.server.io.IOServer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServerInstanceTest {
    private final ServerInstance classUnderTest = new IOServer(0, 10);

    @Test
    void testServerMessageWithoutParameters() {
        // given
        var message = "test";
        // when
        var result = classUnderTest.serverLogMessage(message);
        // then
        assertThat(result).isEqualTo("Server(0): test");
    }

    @Test
    void testServerMessageWithParameters() {
        // given
        var message = "test %s %d";
        var params1 = "param1";
        var params2 = 10;
        // when
        var result = classUnderTest.serverLogMessage(message, params1, params2);
        // then
        assertThat(result).isEqualTo("Server(0): test param1 10");
    }
}
