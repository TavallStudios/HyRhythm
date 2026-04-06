package com.hypixel.hytale.metrics.metric;

public class SynchronizedAverageCollector extends AverageCollector {
   public synchronized double get() {
      return super.get();
   }

   public synchronized long size() {
      return super.size();
   }

   public synchronized double addAndGet(double v) {
      return super.addAndGet(v);
   }

   public synchronized void add(double v) {
      super.add(v);
   }

   public synchronized void remove(double v) {
      super.remove(v);
   }

   public synchronized void clear() {
      super.clear();
   }
}
