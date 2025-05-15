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
        var result = RequestPipeline.of(value);
        // then
        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getResponse()).isNull();
    }

    @Test
    void testReply() {
        // given
        var response = HttpResponse.noContent();
        // when
        var result = RequestPipeline.reply(response);
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getResponse()).isEqualTo(response);
    }

    @Test
    void testMapWithResponse() throws IOException {
        // given
        var response = HttpResponse.noContent();
        var pipeline = RequestPipeline.reply(response);
        // when
        var result = pipeline.map(value -> RequestPipeline.of("next"));
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getResponse()).isEqualTo(response);
    }

    @Test
    void testMapWithValue() throws IOException {
        // given
        var pipeline = RequestPipeline.of("test");
        // when
        var result = pipeline.map(value -> RequestPipeline.of("next"));
        // then
        assertThat(result.getValue()).isEqualTo("next");
        assertThat(result.getResponse()).isNull();
    }

    @Test
    void testThenWithResponse() throws IOException {
        // given
        var response = HttpResponse.noContent();
        var pipeline = RequestPipeline.reply(response);
        // when
        var result = pipeline.then(value -> HttpResponse.ok());
        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void testThenWithValue() throws IOException {
        // given
        var pipeline = RequestPipeline.of("test");
        var response = HttpResponse.ok();
        // when
        var result = pipeline.then(value -> response);
        // then
        assertThat(result).isEqualTo(response);
    }
}