package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import javax.annotation.Nonnull;

public class SingleNoiseProperty implements NoiseProperty {
   protected final int seedOffset;
   protected final NoiseFunction function;

   public SingleNoiseProperty(NoiseFunction function) {
      this(0, function);
   }

   public SingleNoiseProperty(int seedOffset, NoiseFunction function) {
      this.seedOffset = seedOffset;
      this.function = function;
   }

   public int getSeedOffset() {
      return this.seedOffset;
   }

   public NoiseFunction getFunction() {
      return this.function;
   }

   public double get(int seed, double x, double y) {
      return GeneralNoise.limit(this.function.get(seed, seed + this.seedOffset, x, y) * 0.5 + 0.5);
   }

   public double get(int seed, double x, double y, double z) {
      return GeneralNoise.limit(this.function.get(seed, seed + this.seedOffset, x, y, z) * 0.5 + 0.5);
   }

   @Nonnull
   public String toString() {
      int var10000 = this.seedOffset;
      return "SingleNoiseProperty{seedOffset=" + var10000 + ", function=" + String.valueOf(this.function) + "}";
   }
}
