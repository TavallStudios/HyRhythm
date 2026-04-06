package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import javax.annotation.Nonnull;

public class NoiseMaskCondition implements ICoordinateCondition {
   protected final NoiseProperty noiseMask;
   protected final IDoubleCondition condition;

   public NoiseMaskCondition(NoiseProperty noiseMask, IDoubleCondition condition) {
      this.noiseMask = noiseMask;
      this.condition = condition;
   }

   public boolean eval(int seed, int x, int y) {
      return this.condition.eval(this.noiseMask.get(seed, (double)x, (double)y));
   }

   public boolean eval(int seed, int x, int y, int z) {
      return this.condition.eval(this.noiseMask.get(seed, (double)x, (double)y, (double)z));
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.noiseMask);
      return "NoiseMaskCondition{noiseMask=" + var10000 + ", condition=" + String.valueOf(this.condition) + "}";
   }
}
