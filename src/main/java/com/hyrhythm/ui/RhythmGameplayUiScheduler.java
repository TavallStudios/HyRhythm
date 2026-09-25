package com.hyrhythm.ui;

import org.tavall.scheduler.CustomScheduler;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * HyRhythm-owned UI scheduling adapter backed by the canonical Tavall Scheduler.
 *
 * The adapter preserves the existing UI-facing API while removing the plugin-local
 * ScheduledExecutorService/thread-factory implementation.
 */
public final class RhythmGameplayUiScheduler implements AutoCloseable {
    private final CustomScheduler scheduler;

    public RhythmGameplayUiScheduler() {
        this(new CustomScheduler());
    }

    RhythmGameplayUiScheduler(CustomScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelayMs, long periodMs) {
        return scheduler.runTaskRepeating(
            Objects.requireNonNull(runnable, "runnable"),
            initialDelayMs,
            periodMs
        );
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delayMs) {
        return scheduler.runTaskLater(
            Objects.requireNonNull(runnable, "runnable"),
            delayMs
        );
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
