package net.uiqui.embedhttp.api.impl;

import java.util.function.Supplier;

public class Lazy<T> {
    private Supplier<T> supplier;
    private T value;
    private boolean isInitialized = false;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!isInitialized) {
            value = supplier.get();
            isInitialized = true;
            supplier = null;
        }

        return value;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }
}
