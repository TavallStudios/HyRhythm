package com.hypixel.hytale.procedurallib.logic.cell;

import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.DensityPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.JitterPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.NormalPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import javax.annotation.Nonnull;

public class BorderDistanceFunction implements CellDistanceFunction {
   protected final CellDistanceFunction distanceFunction;
   @Nonnull
   protected final PointEvaluator cellEvaluator;
   @Nonnull
   protected final PointEvaluator borderEvaluator;
   @Nonnull
   protected final IIntCondition density;

   public BorderDistanceFunction(CellDistanceFunction distanceFunction, @Nonnull PointEvaluator borderEvaluator, IDoubleCondition density) {
      this.distanceFunction = distanceFunction;
      this.borderEvaluator = borderEvaluator;
      this.cellEvaluator = new JitterPointEvaluator(NormalPointEvaluator.EUCLIDEAN, borderEvaluator.getJitter());
      this.density = DensityPointEvaluator.getDensityCondition(density);
   }

   public double scale(double value) {
      return this.distanceFunction.scale(value);
   }

   public double invScale(double value) {
      return this.distanceFunction.invScale(value);
   }

   public int getCellX(double x, double y) {
      return this.distanceFunction.getCellX(x, y);
   }

   public int getCellY(double x, double y) {
      return this.distanceFunction.getCellY(x, y);
   }

   public void nearest2D(int seed, double x, double y, int cellX, int cellY, @Nonnull ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
      this.transition2D(seed, x, y, cellX, cellY, buffer, pointEvaluator);
   }

   public void transition2D(int seed, double x, double y, int cellX, int cellY, @Nonnull ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
      this.distanceFunction.nearest2D(seed, x, y, cellX, cellY, buffer, this.cellEvaluator);
      if (!this.density.eval(buffer.hash)) {
         buffer.distance = 0.0;
      } else {
         cellX = buffer.ix;
         cellY = buffer.iy;
         buffer.ix2 = cellX;
         buffer.iy2 = cellY;
         buffer.x2 = buffer.x;
         buffer.y2 = buffer.y;
         buffer.distance = 1.0 / 0.0;
         buffer.distance2 = 1.0 / 0.0;
         int dx = this.borderEvaluator.getJitter().getMaxX() > 0.5 ? 2 : 1;
         int dy = this.borderEvaluator.getJitter().getMaxY() > 0.5 ? 2 : 1;

         for(int cy = cellY - dy; cy <= cellY + dy; ++cy) {
            for(int cx = cellX - dx; cx <= cellX + dx; ++cx) {
               this.distanceFunction.evalPoint2(seed, x, y, cx, cy, buffer, this.borderEvaluator);
            }
         }

      }
   }

   public void nearest3D(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public void transition3D(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public void evalPoint(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public void evalPoint2(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public void evalPoint(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public void evalPoint2(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator) {
      throw new UnsupportedOperationException();
   }

   public <T> void collect(int originalSeed, int seed, int minX, int minY, int maxX, int maxY, ResultBuffer.Bounds2d bounds, T ctx, PointConsumer<T> collector, PointEvaluator pointEvaluator) {
      this.distanceFunction.collect(originalSeed, seed, minX, minY, maxX, maxY, bounds, ctx, collector, pointEvaluator);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.distanceFunction);
      return "BorderDistanceFunction{distanceFunction=" + var10000 + ", cellEvaluator=" + String.valueOf(this.cellEvaluator) + ", borderEvaluator=" + String.valueOf(this.borderEvaluator) + ", density=" + String.valueOf(this.density) + "}";
   }
}
