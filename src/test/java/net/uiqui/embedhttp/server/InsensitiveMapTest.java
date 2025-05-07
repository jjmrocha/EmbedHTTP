package net.uiqui.embedhttp.server;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InsensitiveMapTest {

    @Test
    void testFrom() {
        // given
        var map = Map.of("key1", "value", "KEY1", "value");
        // when
        var result = InsensitiveMap.from(map);
        // then
        assertThat(result).hasSize(1)
                .containsKey("key1")
                .containsKey("KEY1");
    }

    @Test
    void testConstructorWithUpperCase() {
        // given
        var classUnderTest = new InsensitiveMap();
        // when
        classUnderTest.put("KEY1", "value");
        // then
        assertThat(classUnderTest).hasSize(1)
                .containsKey("key1")
                .containsKey("KEY1");
    }

    @Test
    void testConstructorWithLowerCase() {
        // given
        var classUnderTest = new InsensitiveMap();
        // when
        classUnderTest.put("key1", "value");
        // then
        assertThat(classUnderTest).hasSize(1)
                .containsKey("key1")
                .containsKey("KEY1");
    }
}