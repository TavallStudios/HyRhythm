package com.hyrhythm.ui;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class RhythmGameplayUiScheduler implements AutoCloseable {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "hyrhythm-ui-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelayMs, long periodMs) {
        Objects.requireNonNull(runnable, "runnable");
        return executor.scheduleAtFixedRate(runnable, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delayMs) {
        Objects.requireNonNull(runnable, "runnable");
        return executor.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
