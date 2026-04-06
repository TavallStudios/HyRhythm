package com.hypixel.hytale.procedurallib.condition;

import java.util.Random;
import javax.annotation.Nonnull;

public class HeightCondition implements ICoordinateRndCondition {
   protected final IHeightThresholdInterpreter interpreter;

   public HeightCondition(IHeightThresholdInterpreter interpreter) {
      this.interpreter = interpreter;
   }

   public boolean eval(int seed, int x, int z, int y, @Nonnull Random random) {
      double threshold = (double)this.interpreter.getThreshold(seed, (double)x, (double)z, y);
      return threshold > 0.0 && (threshold >= 1.0 || threshold > random.nextDouble());
   }

   @Nonnull
   public String toString() {
      return "HeightCondition{interpreter=" + String.valueOf(this.interpreter) + "}";
   }
}
