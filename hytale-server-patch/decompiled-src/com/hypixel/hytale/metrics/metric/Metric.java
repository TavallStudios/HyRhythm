package com.hypixel.hytale.metrics.metric;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Metric {
   public static final Codec<Metric> CODEC;
   private long min;
   private final AverageCollector average = new AverageCollector();
   private long max;

   public Metric() {
      this.clear();
   }

   public void add(long value) {
      if (value < this.min) {
         this.min = value;
      }

      this.average.add((double)value);
      if (value > this.max) {
         this.max = value;
      }

   }

   public void remove(long value) {
      this.average.remove((double)value);
   }

   public long getMin() {
      return this.min;
   }

   public double getAverage() {
      return this.average.get();
   }

   public long getMax() {
      return this.max;
   }

   public void clear() {
      this.min = 9223372036854775807L;
      this.average.clear();
      this.max = -9223372036854775808L;
   }

   public void resetMinMax() {
      this.min = 9223372036854775807L;
      this.max = -9223372036854775808L;
   }

   public void calculateMinMax(long value) {
      if (value < this.min) {
         this.min = value;
      }

      if (value > this.max) {
         this.max = value;
      }

   }

   public void addToAverage(long value) {
      this.average.add((double)value);
   }

   public void set(@Nonnull Metric metric) {
      this.min = metric.min;
      this.average.set(metric.average.get());
      this.max = metric.max;
   }

   @Nonnull
   public String toString() {
      long var10000 = this.min;
      return "Metric{min=" + var10000 + ", average=" + this.average.get() + ", max=" + this.max + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Metric.class, Metric::new).append(new KeyedCodec("Min", Codec.LONG), (metric, s) -> metric.min = s, (metric) -> metric.min).add()).append(new KeyedCodec("Average", Codec.DOUBLE), (metric, s) -> metric.average.set(s), (metric) -> metric.average.get()).add()).append(new KeyedCodec("Max", Codec.LONG), (metric, s) -> metric.max = s, (metric) -> metric.max).add()).build();
   }
}
