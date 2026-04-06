package com.hypixel.hytale.server.worldgen.util.function;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;

public class RandomCoordinateDoubleSupplier implements ICoordinateDoubleSupplier {
   protected final IDoubleRange range;

   public RandomCoordinateDoubleSupplier(IDoubleRange range) {
      this.range = range;
   }

   public IDoubleRange getRange() {
      return this.range;
   }

   public double apply(int seed, int x, int y) {
      return this.range.getValue(HashUtil.random((long)seed, (long)x, (long)y));
   }

   public double apply(int seed, int x, int y, int z) {
      return this.range.getValue(HashUtil.random((long)seed, (long)x, (long)y, (long)z));
   }
}
