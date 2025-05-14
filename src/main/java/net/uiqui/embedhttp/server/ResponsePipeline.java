package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpResponse;

import java.util.function.Function;

public class ResponsePipeline<T> {
    private final T value;
    private final HttpResponse response;

    private ResponsePipeline(T value, HttpResponse response) {
        this.value = value;
        this.response = response;
    }

    public T getValue() {
        return value;
    }

    public HttpResponse getResponse() {
        return response;
    }

    private boolean hasResponse() {
        return response != null;
    }

    public <R> ResponsePipeline<R> next(Function<T, ResponsePipeline<R>> mapper) {
        if (hasResponse()) {
            return ResponsePipeline.reply(response);
        }

        return mapper.apply(value);
    }

    public HttpResponse then(Function<T, HttpResponse> mapper) {
        if (hasResponse()) {
            return response;
        }

        return mapper.apply(value);
    }

    public static <T> ResponsePipeline<T> of(T value) {
        return new ResponsePipeline<>(value, null);
    }

    public static <T> ResponsePipeline<T> reply(HttpResponse response) {
        return new ResponsePipeline<>(null, response);
    }
}
