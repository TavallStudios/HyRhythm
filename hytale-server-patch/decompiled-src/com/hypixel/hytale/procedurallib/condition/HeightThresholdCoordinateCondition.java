package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import javax.annotation.Nonnull;

public class HeightThresholdCoordinateCondition implements ICoordinateCondition {
   private final IHeightThresholdInterpreter interpreter;

   public HeightThresholdCoordinateCondition(IHeightThresholdInterpreter interpreter) {
      this.interpreter = interpreter;
   }

   public boolean eval(int seed, int x, int y) {
      throw new UnsupportedOperationException("This needs a height to operate.");
   }

   public boolean eval(int seed, int x, int y, int z) {
      return (double)this.interpreter.getThreshold(seed, (double)x, (double)z, y) >= HashUtil.random((long)seed, (long)GeneralNoise.fastFloor((double)x), (long)GeneralNoise.fastFloor((double)y), (long)GeneralNoise.fastFloor((double)z));
   }

   @Nonnull
   public String toString() {
      return "HeightThresholdCoordinateCondition{interpreter=" + String.valueOf(this.interpreter) + "}";
   }
}
