package net.uiqui.embedhttp.server;

import net.uiqui.embedhttp.api.HttpResponse;

import java.io.IOException;

public class RequestPipeline<T> {
    private final T value;
    private final HttpResponse error;

    private RequestPipeline(T value, HttpResponse response) {
        this.value = value;
        this.error = response;
    }

    public T getValue() {
        return value;
    }

    public HttpResponse getError() {
        return error;
    }

    private boolean hasError() {
        return error != null;
    }

    public <R> RequestPipeline<R> map(ThrowingFunction<T, RequestPipeline<R>> mapper) throws IOException {
        if (hasError()) {
            return RequestPipeline.error(error);
        }

        return mapper.apply(value);
    }

    public HttpResponse then(ThrowingFunction<T, HttpResponse> mapper) throws IOException {
        if (hasError()) {
            return error;
        }

        return mapper.apply(value);
    }

    public static <T> RequestPipeline<T> value(T value) {
        return new RequestPipeline<>(value, null);
    }

    public static <T> RequestPipeline<T> error(HttpResponse response) {
        return new RequestPipeline<>(null, response);
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws IOException;
    }
}
