package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import javax.annotation.Nonnull;

public class NormalPointEvaluator implements PointEvaluator {
   public static final PointEvaluator EUCLIDEAN;
   public static final PointEvaluator MANHATTAN;
   public static final PointEvaluator NATURAL;
   public static final PointEvaluator MAX;
   protected final PointDistanceFunction distanceFunction;

   public NormalPointEvaluator(PointDistanceFunction distanceFunction) {
      this.distanceFunction = distanceFunction;
   }

   public void evalPoint(int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      buffer.register(cellHash, cellX, cellY, distance, cellPointX, cellPointY);
   }

   public void evalPoint2(int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      buffer.register2(cellHash, cellX, cellY, distance, cellPointX, cellPointY);
   }

   public void evalPoint(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, @Nonnull ResultBuffer.ResultBuffer3d buffer) {
      double distance = this.distanceFunction.distance3D(seed, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, cellPointX - x, cellPointY - y, cellPointZ - z);
      buffer.register(cellHash, cellX, cellY, cellZ, distance, cellPointX, cellPointY, cellPointZ);
   }

   public void evalPoint2(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, @Nonnull ResultBuffer.ResultBuffer3d buffer) {
      double distance = this.distanceFunction.distance3D(seed, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, cellPointX - x, cellPointY - y, cellPointZ - z);
      buffer.register2(cellHash, cellX, cellY, cellZ, distance, cellPointX, cellPointY, cellPointZ);
   }

   @Nonnull
   public String toString() {
      return "NormalPointEvaluator{distanceFunction=" + String.valueOf(this.distanceFunction) + "}";
   }

   public static PointEvaluator of(PointDistanceFunction distanceFunction) {
      DistanceCalculationMode mode = DistanceCalculationMode.from(distanceFunction);
      if (mode == null) {
         return new NormalPointEvaluator(distanceFunction);
      } else {
         PointEvaluator var10000;
         switch (mode) {
            case EUCLIDEAN -> var10000 = EUCLIDEAN;
            case MANHATTAN -> var10000 = MANHATTAN;
            case NATURAL -> var10000 = NATURAL;
            case MAX -> var10000 = MAX;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   static {
      EUCLIDEAN = new NormalPointEvaluator(DistanceCalculationMode.EUCLIDEAN.getFunction());
      MANHATTAN = new NormalPointEvaluator(DistanceCalculationMode.MANHATTAN.getFunction());
      NATURAL = new NormalPointEvaluator(DistanceCalculationMode.NATURAL.getFunction());
      MAX = new NormalPointEvaluator(DistanceCalculationMode.MAX.getFunction());
   }
}
