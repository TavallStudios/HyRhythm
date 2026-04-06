package com.hypixel.hytale.sneakythrow;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> extends Runnable {
   default void run() {
      try {
         this.runNow();
      } catch (Throwable e) {
         throw SneakyThrow.sneakyThrow(e);
      }
   }

   void runNow() throws E;
}
