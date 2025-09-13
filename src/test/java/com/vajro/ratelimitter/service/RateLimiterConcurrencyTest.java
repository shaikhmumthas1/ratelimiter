package com.vajro.ratelimitter.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RateLimiterConcurrencyTest {

    @Test
    public void concurrentIncrementsShouldBeCountedCorrectly() throws Exception {
        final int threads = 50;
        final AtomicInteger counter = new AtomicInteger(0);
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        Callable<Integer> task = () -> counter.incrementAndGet();
        int calls = 200;
        CompletionService<Integer> cs = new ExecutorCompletionService<>(exec);
        for (int i = 0; i < calls; i++) cs.submit(task);

        int highest = 0;
        for (int i = 0; i < calls; i++) {
            Future<Integer> f = cs.take();
            int v = f.get();
            highest = Math.max(highest, v);
        }
        exec.shutdown();
        // final counter should match calls
        assertEquals(calls, counter.get());
    }
}
