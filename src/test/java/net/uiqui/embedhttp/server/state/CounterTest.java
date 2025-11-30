package net.uiqui.embedhttp.server.state;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {
    @Test
    void testAwaitReturnsImmediatelyWhenCounterIsZero() throws InterruptedException {
        // given
        var classUnderTest = new Counter();
        // when
        var start = System.currentTimeMillis();
        classUnderTest.await();
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(waitTime).isLessThan(50);
    }

    @Test
    void testAddOneAndDownOne() throws InterruptedException {
        // given
        var classUnderTest = new Counter();
        // when
        classUnderTest.addOne();
        classUnderTest.downOne();
        // then
        var start = System.currentTimeMillis();
        classUnderTest.await();
        var waitTime = System.currentTimeMillis() - start;
        assertThat(waitTime).isLessThan(50);
    }

    @Test
    void testAwaitBlocksUntilCounterReachesZero() throws InterruptedException {
        // given
        var classUnderTest = new Counter();
        classUnderTest.addOne();
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                classUnderTest.downOne();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        classUnderTest.await();
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(waitTime).isGreaterThanOrEqualTo(100);
    }

    @Test
    void testAwaitWithMultipleIncrements() throws InterruptedException {
        // given
        var classUnderTest = new Counter();
        classUnderTest.addOne();
        classUnderTest.addOne();
        classUnderTest.addOne();
        var parallelThread = new Thread(() -> {
            try {
                Thread.sleep(50);
                classUnderTest.downOne();
                Thread.sleep(50);
                classUnderTest.downOne();
                Thread.sleep(50);
                classUnderTest.downOne();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // when
        var start = System.currentTimeMillis();
        parallelThread.start();
        classUnderTest.await();
        var waitTime = System.currentTimeMillis() - start;
        // then
        assertThat(waitTime).isGreaterThanOrEqualTo(150);
    }
}
