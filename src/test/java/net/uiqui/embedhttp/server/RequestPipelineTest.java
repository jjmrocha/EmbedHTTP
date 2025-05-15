package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class RequestPipelineTest {
    @Test
    void testOf() {
        // given
        var value = "test";
        // when
        var result = RequestPipeline.value(value);
        // then
        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getError()).isNull();
    }

    @Test
    void testError() {
        // given
        var response = HttpResponse.noContent();
        // when
        var result = RequestPipeline.error(response);
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getError()).isEqualTo(response);
    }

    @Test
    void testMapWithError() throws IOException {
        // given
        var response = HttpResponse.noContent();
        var pipeline = RequestPipeline.error(response);
        // when
        var result = pipeline.map(value -> RequestPipeline.value("next"));
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getError()).isEqualTo(response);
    }

    @Test
    void testMapWithValue() throws IOException {
        // given
        var pipeline = RequestPipeline.value("test");
        // when
        var result = pipeline.map(value -> RequestPipeline.value("next"));
        // then
        assertThat(result.getValue()).isEqualTo("next");
        assertThat(result.getError()).isNull();
    }

    @Test
    void testThenWithError() throws IOException {
        // given
        var response = HttpResponse.noContent();
        var pipeline = RequestPipeline.error(response);
        // when
        var result = pipeline.then(value -> HttpResponse.ok());
        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void testThenWithValue() throws IOException {
        // given
        var pipeline = RequestPipeline.value("test");
        var response = HttpResponse.ok();
        // when
        var result = pipeline.then(value -> response);
        // then
        assertThat(result).isEqualTo(response);
    }
}