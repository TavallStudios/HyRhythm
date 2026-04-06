package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import javax.annotation.Nonnull;

public class JitterPointEvaluator implements PointEvaluator {
   protected final PointEvaluator pointEvaluator;
   protected final CellJitter jitter;

   public JitterPointEvaluator(PointEvaluator pointEvaluator, CellJitter jitter) {
      this.pointEvaluator = pointEvaluator;
      this.jitter = jitter;
   }

   public CellJitter getJitter() {
      return this.jitter;
   }

   public void evalPoint(int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer) {
      this.pointEvaluator.evalPoint(seed, x, y, cellHash, cellX, cellY, cellPointX, cellPointY, buffer);
   }

   public void evalPoint2(int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer) {
      this.pointEvaluator.evalPoint2(seed, x, y, cellHash, cellX, cellY, cellPointX, cellPointY, buffer);
   }

   public void evalPoint(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, ResultBuffer.ResultBuffer3d buffer) {
      this.pointEvaluator.evalPoint(seed, x, y, z, cellHash, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, buffer);
   }

   public void evalPoint2(int seed, double x, double y, double z, int cellHash, int cellX, int cellY, int cellZ, double cellPointX, double cellPointY, double cellPointZ, ResultBuffer.ResultBuffer3d buffer) {
      this.pointEvaluator.evalPoint2(seed, x, y, z, cellHash, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, buffer);
   }

   public <T> void collectPoint(int cellHash, int cellX, int cellY, double cellCentreX, double cellCentreY, T ctx, @Nonnull PointConsumer<T> consumer) {
      this.pointEvaluator.collectPoint(cellHash, cellX, cellY, cellCentreX, cellCentreY, ctx, consumer);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.pointEvaluator);
      return "JitterPointEvaluator{pointEvaluator=" + var10000 + ", jitter=" + String.valueOf(this.jitter) + "}";
   }
}
