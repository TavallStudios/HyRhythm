package com.hypixel.hytale.procedurallib.supplier;

import java.util.Random;
import javax.annotation.Nonnull;

public class FloatRange {
   public static final Constant ZERO = new Constant(0.0F);
   public static final Constant ONE = new Constant(1.0F);

   public static class Constant implements IFloatRange {
      protected final float result;

      public Constant(float result) {
         this.result = result;
      }

      public float getResult() {
         return this.result;
      }

      public float getValue(float v) {
         return this.result;
      }

      public float getValue(FloatSupplier supplier) {
         return this.result;
      }

      public float getValue(Random random) {
         return this.result;
      }

      public float getValue(int seed, double x, double y, IDoubleCoordinateSupplier2d supplier) {
         return this.result;
      }

      public float getValue(int seed, double x, double y, double z, IDoubleCoordinateSupplier3d supplier) {
         return this.result;
      }

      @Nonnull
      public String toString() {
         return "FloatRange.Constant{result=" + this.result + "}";
      }
   }

   public static class Normal implements IFloatRange {
      protected final float min;
      protected final float range;

      public Normal(float min, float max) {
         this.min = min;
         this.range = max - min;
      }

      public float getMin() {
         return this.min;
      }

      public float getRange() {
         return this.range;
      }

      public float getValue(float v) {
         return this.min + this.range * v;
      }

      public float getValue(@Nonnull FloatSupplier supplier) {
         return this.min + this.range * supplier.getAsFloat();
      }

      public float getValue(@Nonnull Random random) {
         return this.getValue(random.nextFloat());
      }

      public float getValue(int seed, double x, double y, @Nonnull IDoubleCoordinateSupplier2d supplier) {
         return this.min + this.range * (float)supplier.get(seed, x, y);
      }

      public float getValue(int seed, double x, double y, double z, @Nonnull IDoubleCoordinateSupplier3d supplier) {
         return this.min + this.range * (float)supplier.get(seed, x, y, z);
      }

      @Nonnull
      public String toString() {
         return "FloatRange.Normal{min=" + this.min + ", range=" + this.range + "}";
      }
   }
}
