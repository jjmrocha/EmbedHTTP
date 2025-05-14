package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResponsePipelineTest {
    @Test
    void testOf() {
        // given
        var value = "test";
        // when
        var result = ResponsePipeline.of(value);
        // then
        assertThat(result.getValue()).isEqualTo(value);
        assertThat(result.getResponse()).isNull();
    }

    @Test
    void testReply() {
        // given
        var response = HttpResponse.noContent();
        // when
        var result = ResponsePipeline.reply(response);
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getResponse()).isEqualTo(response);
    }

    @Test
    void testNextWithResponse() {
        // given
        var response = HttpResponse.noContent();
        var pipeline = ResponsePipeline.reply(response);
        // when
        var result = pipeline.next(value -> ResponsePipeline.of("next"));
        // then
        assertThat(result.getValue()).isNull();
        assertThat(result.getResponse()).isEqualTo(response);
    }

    @Test
    void testNextWithValue() {
        // given
        var pipeline = ResponsePipeline.of("test");
        // when
        var result = pipeline.next(value -> ResponsePipeline.of("next"));
        // then
        assertThat(result.getValue()).isEqualTo("next");
        assertThat(result.getResponse()).isNull();
    }

    @Test
    void testThenWithResponse() {
        // given
        var response = HttpResponse.noContent();
        var pipeline = ResponsePipeline.reply(response);
        // when
        var result = pipeline.then(value -> HttpResponse.ok());
        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void testThenWithValue() {
        // given
        var pipeline = ResponsePipeline.of("test");
        var response = HttpResponse.ok();
        // when
        var result = pipeline.then(value -> response);
        // then
        assertThat(result).isEqualTo(response);
    }
}