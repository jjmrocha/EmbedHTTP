package net.uiqui.embedhttp.server.io;

import net.uiqui.embedhttp.Router;
import net.uiqui.embedhttp.api.ContentType;
import net.uiqui.embedhttp.api.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class IOServerTest {
    @Test
    void testStartWithRandomPort() throws Exception {
        // given
        var classUnderTest = new IOServer(0, 10);
        var router = Router.newRouter()
                .get("/", req -> HttpResponse.ok()
                        .setBody(ContentType.TEXT_PLAIN, "Hello World")
                );
        // when
        var result = classUnderTest.start(router);
        var url = "http://localhost:" + classUnderTest.getInstancePort() + "/";
        var response = callEndpoint(url);
        // then
        assertThat(result).isTrue();
        assertThat(classUnderTest.isRunning()).isTrue();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World");
    }

    @Test
    void testStartWithDefinedPort() throws Exception {
        // given
        var classUnderTest = new IOServer(9876, 10);
        var router = Router.newRouter()
                .get("/", req -> HttpResponse.ok()
                        .setBody(ContentType.TEXT_PLAIN, "Hello World")
                );
        // when
        var result = classUnderTest.start(router);
        var url = "http://localhost:9876/";
        var response = callEndpoint(url);
        // then
        assertThat(result).isTrue();
        assertThat(classUnderTest.isRunning()).isTrue();
        assertThat(classUnderTest.getInstancePort()).isEqualTo(9876);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World");
    }

    @Test
    void testStop() throws Exception {
        // given
        var classUnderTest = new IOServer(0, 10);
        var router = Router.newRouter()
                .get("/", req -> HttpResponse.ok()
                        .setBody(ContentType.TEXT_PLAIN, "Hello World")
                );
        classUnderTest.start(router);
        var url = "http://localhost:" + classUnderTest.getInstancePort() + "/";
        // when
        var result = classUnderTest.stop();
        var response = catchThrowable(() ->
                callEndpoint(url)
        );
        // then
        assertThat(result).isTrue();
        assertThat(classUnderTest.isRunning()).isFalse();
        assertThat(response).isInstanceOf(ConnectException.class);
        assertThat(classUnderTest.getInstancePort()).isEqualTo(-1);
    }

    private java.net.http.HttpResponse<String> callEndpoint(String url) throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .build();
            return client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        }
    }
}