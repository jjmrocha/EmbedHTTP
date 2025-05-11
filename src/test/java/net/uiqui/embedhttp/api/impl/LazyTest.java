package net.uiqui.embedhttp.api.impl;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class LazyTest {

    @Test
    void testGetExecutesSupplier() {
        // given
        var lazy = Lazy.of(() -> "Hello, World!");
        // when
        var result = lazy.get();
        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void testGetSupplierIsCalledOnlyOnce() {
        // given
        var counter = new AtomicInteger();
        var lazy = Lazy.of(counter::incrementAndGet);
        // then
        var result = lazy.get();
        assertThat(result).isEqualTo(1);
        // then again
        result = lazy.get();
        assertThat(result).isEqualTo(1);
    }
}