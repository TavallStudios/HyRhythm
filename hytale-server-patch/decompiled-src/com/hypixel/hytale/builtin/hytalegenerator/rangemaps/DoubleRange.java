package com.hypixel.hytale.builtin.hytalegenerator.rangemaps;

import javax.annotation.Nonnull;

public class DoubleRange {
   private double min;
   private double max;
   private boolean inclusiveMin;
   private boolean inclusiveMax;

   public DoubleRange(double min, boolean inclusiveMin, double max, boolean inclusiveMax) {
      if (min > max) {
         throw new IllegalArgumentException("min greater than max");
      } else {
         this.min = min;
         this.max = max;
         this.inclusiveMin = inclusiveMin;
         this.inclusiveMax = inclusiveMax;
      }
   }

   public double getMin() {
      return this.min;
   }

   public boolean isInclusiveMin() {
      return this.inclusiveMin;
   }

   public double getMax() {
      return this.max;
   }

   public boolean isInclusiveMax() {
      return this.inclusiveMax;
   }

   public boolean includes(double v) {
      boolean var10000;
      label32: {
         label24: {
            if (this.inclusiveMin) {
               if (!(v >= this.min)) {
                  break label24;
               }
            } else if (!(v > this.min)) {
               break label24;
            }

            if (this.inclusiveMax) {
               if (v <= this.max) {
                  break label32;
               }
            } else if (v < this.max) {
               break label32;
            }
         }

         var10000 = false;
         return var10000;
      }

      var10000 = true;
      return var10000;
   }

   @Nonnull
   public static DoubleRange inclusive(double min, double max) {
      return new DoubleRange(min, true, max, true);
   }

   @Nonnull
   public static DoubleRange exclusive(double min, double max) {
      return new DoubleRange(min, false, max, false);
   }
}
