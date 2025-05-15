package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpResponse;

import java.io.IOException;

public class RequestPipeline<T> {
    private final T value;
    private final HttpResponse response;

    private RequestPipeline(T value, HttpResponse response) {
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

    public <R> RequestPipeline<R> map(ThrowingFunction<T, RequestPipeline<R>> mapper) throws IOException {
        if (hasResponse()) {
            return RequestPipeline.reply(response);
        }

        return mapper.apply(value);
    }

    public HttpResponse then(ThrowingFunction<T, HttpResponse> mapper) throws IOException {
        if (hasResponse()) {
            return response;
        }

        return mapper.apply(value);
    }

    public static <T> RequestPipeline<T> of(T value) {
        return new RequestPipeline<>(value, null);
    }

    public static <T> RequestPipeline<T> reply(HttpResponse response) {
        return new RequestPipeline<>(null, response);
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws IOException;
    }
}
