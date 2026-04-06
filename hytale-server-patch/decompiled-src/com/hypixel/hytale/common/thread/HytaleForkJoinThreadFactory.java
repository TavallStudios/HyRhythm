package com.hypixel.hytale.common.thread;

import com.hypixel.hytale.metrics.InitStackThread;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import javax.annotation.Nonnull;

public class HytaleForkJoinThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
   @Nonnull
   public ForkJoinWorkerThread newThread(@Nonnull ForkJoinPool pool) {
      return new WorkerThread(pool);
   }

   public static class WorkerThread extends ForkJoinWorkerThread implements InitStackThread {
      @Nonnull
      private final StackTraceElement[] initStack = Thread.currentThread().getStackTrace();

      protected WorkerThread(@Nonnull ForkJoinPool pool) {
         super(pool);
      }

      @Nonnull
      public StackTraceElement[] getInitStack() {
         return this.initStack;
      }
   }
}
