package com.hypixel.hytale.procedurallib.logic.cell.jitter;

import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import javax.annotation.Nonnull;

public class ConstantCellJitter implements CellJitter {
   protected final double jitterX;
   protected final double jitterY;
   protected final double jitterZ;

   public ConstantCellJitter(double jitterX, double jitterY, double jitterZ) {
      this.jitterX = jitterX;
      this.jitterY = jitterY;
      this.jitterZ = jitterZ;
   }

   public double getMaxX() {
      return this.jitterX;
   }

   public double getMaxY() {
      return this.jitterY;
   }

   public double getMaxZ() {
      return this.jitterZ;
   }

   public double getPointX(int cx, @Nonnull DoubleArray.Double2 vec) {
      return (double)cx + vec.x * this.jitterX;
   }

   public double getPointY(int cy, @Nonnull DoubleArray.Double2 vec) {
      return (double)cy + vec.y * this.jitterY;
   }

   public double getPointX(int cx, @Nonnull DoubleArray.Double3 vec) {
      return (double)cx + vec.x * this.jitterX;
   }

   public double getPointY(int cy, @Nonnull DoubleArray.Double3 vec) {
      return (double)cy + vec.y * this.jitterY;
   }

   public double getPointZ(int cz, @Nonnull DoubleArray.Double3 vec) {
      return (double)cz + vec.z * this.jitterZ;
   }

   @Nonnull
   public String toString() {
      return "ConstantCellJitter{jitterX=" + this.jitterX + ", jitterY=" + this.jitterY + ", jitterZ=" + this.jitterZ + "}";
   }
}
