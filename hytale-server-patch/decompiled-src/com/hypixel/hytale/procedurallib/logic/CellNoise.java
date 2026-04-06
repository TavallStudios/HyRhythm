package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CellNoise implements NoiseFunction {
   protected final CellDistanceFunction distanceFunction;
   protected final PointEvaluator pointEvaluator;
   protected final CellFunction cellFunction;
   @Nullable
   protected final NoiseProperty noiseLookup;

   public CellNoise(CellDistanceFunction distanceFunction, PointEvaluator pointEvaluator, CellFunction cellFunction, @Nullable NoiseProperty noiseLookup) {
      this.distanceFunction = distanceFunction;
      this.pointEvaluator = pointEvaluator;
      this.cellFunction = cellFunction;
      this.noiseLookup = noiseLookup;
   }

   public CellDistanceFunction getDistanceFunction() {
      return this.distanceFunction;
   }

   public CellFunction getCellFunction() {
      return this.cellFunction;
   }

   @Nullable
   public NoiseProperty getNoiseLookup() {
      return this.noiseLookup;
   }

   public double get(int seed, int offsetSeed, double x, double y) {
      x = this.distanceFunction.scale(x);
      y = this.distanceFunction.scale(y);
      int xr = this.distanceFunction.getCellX(x, y);
      int yr = this.distanceFunction.getCellY(x, y);
      ResultBuffer.ResultBuffer2d buffer = this.localBuffer2d();
      buffer.distance = 1.0 / 0.0;
      this.distanceFunction.nearest2D(offsetSeed, x, y, xr, yr, buffer, this.pointEvaluator);
      return GeneralNoise.limit(this.cellFunction.eval(seed, offsetSeed, x, y, buffer, this.distanceFunction, this.noiseLookup)) * 2.0 - 1.0;
   }

   public double get(int seed, int offsetSeed, double x, double y, double z) {
      int xr = this.distanceFunction.getCellX(x, y, z);
      int yr = this.distanceFunction.getCellY(x, y, z);
      int zr = this.distanceFunction.getCellZ(x, y, z);
      ResultBuffer.ResultBuffer3d buffer = this.localBuffer3d();
      buffer.distance = 1.0 / 0.0;
      this.distanceFunction.nearest3D(offsetSeed, x, y, z, xr, yr, zr, buffer, this.pointEvaluator);
      return GeneralNoise.limit(this.cellFunction.eval(seed, offsetSeed, x, y, z, buffer, this.distanceFunction, this.noiseLookup)) * 2.0 - 1.0;
   }

   protected ResultBuffer.ResultBuffer2d localBuffer2d() {
      return ResultBuffer.buffer2d;
   }

   protected ResultBuffer.ResultBuffer3d localBuffer3d() {
      return ResultBuffer.buffer3d;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.distanceFunction);
      return "CellNoise{distanceFunction=" + var10000 + ", pointEvaluator=" + String.valueOf(this.pointEvaluator) + ", cellFunction=" + String.valueOf(this.cellFunction) + ", noiseLookup=" + String.valueOf(this.noiseLookup) + "}";
   }

   public static enum CellMode {
      CELL_VALUE(new CellFunction() {
         public double eval(int seed, int offsetSeed, double x, double y, @Nonnull ResultBuffer.ResultBuffer2d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            return HashUtil.random((long)offsetSeed, (long)buffer.ix, (long)buffer.iy);
         }

         public double eval(int seed, int offsetSeed, double x, double y, double z, @Nonnull ResultBuffer.ResultBuffer3d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            return HashUtil.random((long)offsetSeed, (long)buffer.ix, (long)buffer.iy, (long)buffer.iz);
         }

         @Nonnull
         public String toString() {
            return "CellValueCellFunction{}";
         }
      }),
      NOISE_LOOKUP(new CellFunction() {
         public double eval(int seed, int offsetSeed, double x, double y, @Nonnull ResultBuffer.ResultBuffer2d buffer, @Nonnull CellDistanceFunction cellFunction, @Nonnull NoiseProperty noiseLookup) {
            double px = cellFunction.invScale(buffer.x);
            double py = cellFunction.invScale(buffer.y);
            return noiseLookup.get(seed, px, py);
         }

         public double eval(int seed, int offsetSeed, double x, double y, double z, @Nonnull ResultBuffer.ResultBuffer3d buffer, @Nonnull CellDistanceFunction cellFunction, @Nonnull NoiseProperty noiseLookup) {
            double px = cellFunction.invScale(buffer.x);
            double py = cellFunction.invScale(buffer.y);
            double pz = cellFunction.invScale(buffer.z);
            return noiseLookup.get(seed, px, py, pz);
         }

         @Nonnull
         public String toString() {
            return "NoiseLookupCellFunction{}";
         }
      }),
      DISTANCE(new CellFunction() {
         public double eval(int seed, int offsetSeed, double x, double y, @Nonnull ResultBuffer.ResultBuffer2d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            return Math.sqrt(buffer.distance);
         }

         public double eval(int seed, int offsetSeed, double x, double y, double z, @Nonnull ResultBuffer.ResultBuffer3d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            return Math.sqrt(buffer.distance);
         }

         @Nonnull
         public String toString() {
            return "DistanceCellFunction{}";
         }
      }),
      DIRECTION(new CellFunction() {
         public double eval(int seed, int offsetSeed, double x, double y, @Nonnull ResultBuffer.ResultBuffer2d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            float angle = (float)this.getAngleNoise(seed, offsetSeed, buffer, noiseLookup) * 6.2831855F;
            float dx = TrigMathUtil.sin(angle);
            float dy = TrigMathUtil.cos(angle);
            double ax = buffer.x;
            double ay = buffer.y;
            double bx = ax + (double)dx;
            double by = ay + (double)dy;
            double distance2 = MathUtil.distanceToInfLineSq(x, y, ax, ay, bx, by);
            double distance = MathUtil.clamp(Math.sqrt(distance2), 0.0, 1.0);
            int side = MathUtil.sideOfLine(x, y, ax, ay, bx, by);
            return 0.5 + (double)side * distance * 0.5;
         }

         public double eval(int seed, int offsetSeed, double x, double y, double z, ResultBuffer.ResultBuffer3d buffer, CellDistanceFunction cellFunction, NoiseProperty noiseLookup) {
            throw new UnsupportedOperationException();
         }

         @Nonnull
         public String toString() {
            return "DirectionCellFunction{}";
         }

         private double getAngleNoise(int seed, int offsetSeed, @Nonnull ResultBuffer.ResultBuffer2d buffer, @Nullable NoiseProperty noiseProperty) {
            return noiseProperty != null ? noiseProperty.get(seed, buffer.x, buffer.y) : HashUtil.random((long)offsetSeed, (long)buffer.ix, (long)buffer.iy);
         }
      });

      private final CellFunction function;

      private CellMode(CellFunction function) {
         this.function = function;
      }

      public CellFunction getFunction() {
         return this.function;
      }
   }

   public interface CellFunction {
      double eval(int var1, int var2, double var3, double var5, ResultBuffer.ResultBuffer2d var7, CellDistanceFunction var8, NoiseProperty var9);

      double eval(int var1, int var2, double var3, double var5, double var7, ResultBuffer.ResultBuffer3d var9, CellDistanceFunction var10, NoiseProperty var11);
   }
}
