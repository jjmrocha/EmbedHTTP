package net.uiqui.embedhttp;

import net.uiqui.embedhttp.server.ServerInstance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HttpServerTest {

    @Test
    void newInstance() {
        // given
        int port = 8080;
        // when
        var result = HttpServer.newInstance(port);
        // then
        assertThat(result).isInstanceOf(ServerInstance.class);
        assertThat(result.isRunning()).isFalse();
    }
}