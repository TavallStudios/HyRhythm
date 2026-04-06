package com.hypixel.hytale.procedurallib.supplier;

import com.hypixel.hytale.math.util.HashUtil;
import javax.annotation.Nonnull;

public class DoubleRangeCoordinateHashSupplier implements IDoubleCoordinateHashSupplier {
   protected final IDoubleRange range;

   public DoubleRangeCoordinateHashSupplier(IDoubleRange range) {
      this.range = range;
   }

   public double get(int seed, int x, int y, long hash) {
      return this.range.getValue(HashUtil.random((long)seed, (long)x, (long)y, hash));
   }

   @Nonnull
   public String toString() {
      return "DoubleRangeCoordinateHashSupplier{range=" + String.valueOf(this.range) + "}";
   }
}
