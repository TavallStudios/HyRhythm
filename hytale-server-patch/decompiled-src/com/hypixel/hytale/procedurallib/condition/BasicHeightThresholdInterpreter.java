package com.hypixel.hytale.procedurallib.condition;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class BasicHeightThresholdInterpreter implements IHeightThresholdInterpreter {
   @Nonnull
   protected final float[] interpolatedThresholds;
   protected final int lowestNonOne;
   protected final int highestNonZero;

   public BasicHeightThresholdInterpreter(@Nonnull int[] positions, @Nonnull float[] thresholds, int length) {
      if (positions.length != thresholds.length) {
         throw new IllegalArgumentException(String.format("Mismatching array lengths! positions: %s, thresholds: %s", positions.length, thresholds.length));
      } else {
         this.interpolatedThresholds = new float[length];

         for(int y = 0; y < this.interpolatedThresholds.length; ++y) {
            float threshold = thresholds[thresholds.length - 1];

            for(int i = 0; i < positions.length; ++i) {
               if (y < positions[i]) {
                  if (i == 0) {
                     threshold = thresholds[i];
                  } else {
                     float distance = (float)(y - positions[i - 1]) / (float)(positions[i] - positions[i - 1]);
                     threshold = IHeightThresholdInterpreter.lerp(thresholds[i - 1], thresholds[i], distance);
                  }
                  break;
               }
            }

            this.interpolatedThresholds[y] = threshold;
         }

         int lowestNonOne;
         for(lowestNonOne = 0; lowestNonOne < length && !(this.interpolatedThresholds[lowestNonOne] < 1.0F); ++lowestNonOne) {
         }

         this.lowestNonOne = lowestNonOne;

         int highestNonZero;
         for(highestNonZero = length - 1; highestNonZero >= 0 && !(this.interpolatedThresholds[highestNonZero] > 0.0F); --highestNonZero) {
         }

         this.highestNonZero = highestNonZero;
      }
   }

   public int getLowestNonOne() {
      return this.lowestNonOne;
   }

   public int getHighestNonZero() {
      return this.highestNonZero;
   }

   public double getContext(int seed, double x, double y) {
      return 0.0;
   }

   public int getLength() {
      return this.interpolatedThresholds.length;
   }

   public float getThreshold(int seed, double x, double y, int height) {
      return this.getThreshold(seed, x, y, height, this.getContext(seed, x, y));
   }

   public float getThreshold(int seed, double x, double y, int height, double context) {
      if (height > this.highestNonZero) {
         return 0.0F;
      } else {
         if (height < 0) {
            height = 0;
         }

         if (height >= this.interpolatedThresholds.length) {
            height = this.interpolatedThresholds.length - 1;
         }

         return this.interpolatedThresholds[height];
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.interpolatedThresholds);
      return "BasicHeightThresholdInterpreter{interpolatedThresholds=" + var10000 + ", lowestNonOne=" + this.lowestNonOne + ", highestNonZero=" + this.highestNonZero + "}";
   }

   public interface Constants {
      String ERROR_ARRAY_LENGTH = "Mismatching array lengths! positions: %s, thresholds: %s";
   }
}
