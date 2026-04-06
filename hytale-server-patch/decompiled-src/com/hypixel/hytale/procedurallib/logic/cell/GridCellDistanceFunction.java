package com.hypixel.hytale.procedurallib.logic.cell;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.CellularNoise;
import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import javax.annotation.Nonnull;

public class GridCellDistanceFunction implements CellDistanceFunction {
   public static final GridCellDistanceFunction DISTANCE_FUNCTION = new GridCellDistanceFunction();
   public static final CellPointFunction POINT_FUNCTION = new CellPointFunction() {
      public int getHash(int seed, int cellX, int cellY) {
         return GridCellDistanceFunction.getHash(seed, cellX, cellY);
      }

      public DoubleArray.Double2 getOffsets(int hash) {
         return CellularNoise.CELL_2D[hash & 255];
      }

      public double getX(double x, double y) {
         return x;
      }

      public double getY(double x, double y) {
         return y;
      }
   };

   public void nearest2D(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      for(int cy = cellY - 1; cy <= cellY + 1; ++cy) {
         for(int cx = cellX - 1; cx <= cellX + 1; ++cx) {
            this.evalPoint(seed, x, y, cx, cy, buffer, pointEvaluator);
         }
      }

   }

   public void nearest3D(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, @Nonnull PointEvaluator pointEvaluator) {
      for(int cx = cellX - 1; cx <= cellX + 1; ++cx) {
         for(int cy = cellY - 1; cy <= cellY + 1; ++cy) {
            for(int cz = cellZ - 1; cz <= cellZ + 1; ++cz) {
               this.evalPoint(seed, x, y, z, cx, cy, cz, buffer, pointEvaluator);
            }
         }
      }

   }

   public void transition2D(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      for(int cy = cellY - 1; cy <= cellY + 1; ++cy) {
         for(int cx = cellX - 1; cx <= cellX + 1; ++cx) {
            this.evalPoint2(seed, x, y, cx, cy, buffer, pointEvaluator);
         }
      }

   }

   public void transition3D(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, @Nonnull PointEvaluator pointEvaluator) {
      for(int cx = cellX - 1; cx <= cellX + 1; ++cx) {
         for(int cy = cellY - 1; cy <= cellY + 1; ++cy) {
            for(int cz = cellZ - 1; cz <= cellZ + 1; ++cz) {
               this.evalPoint2(seed, x, y, z, cx, cy, cz, buffer, pointEvaluator);
            }
         }
      }

   }

   public void evalPoint(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double2 vec = CellularNoise.CELL_2D[cellHash & 255];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellY, vec);
      pointEvaluator.evalPoint(seed, x, y, cellHash, cellX, cellY, px, py, buffer);
   }

   public void evalPoint(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double3 vec = CellularNoise.CELL_3D[cellHash & 255];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellX, vec);
      double pz = jitter.getPointZ(cellX, vec);
      pointEvaluator.evalPoint(seed, x, y, z, cellHash, cellX, cellY, cellZ, px, py, pz, buffer);
   }

   public void evalPoint2(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double2 vec = CellularNoise.CELL_2D[cellHash & 255];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellY, vec);
      pointEvaluator.evalPoint2(seed, x, y, cellHash, cellX, cellY, px, py, buffer);
   }

   public void evalPoint2(int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double3 vec = CellularNoise.CELL_3D[cellHash & 255];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellX, vec);
      double pz = jitter.getPointZ(cellX, vec);
      pointEvaluator.evalPoint2(seed, x, y, z, cellHash, cellX, cellY, cellZ, px, py, pz, buffer);
   }

   public <T> void collect(int originalSeed, int seed, int minX, int minY, int maxX, int maxY, ResultBuffer.Bounds2d bounds, T ctx, @Nonnull PointConsumer<T> collector, @Nonnull PointEvaluator pointEvaluator) {
      CellJitter jitter = pointEvaluator.getJitter();

      for(int cy = minY; cy <= maxY; ++cy) {
         for(int cx = minX; cx <= maxX; ++cx) {
            int cellHash = getHash(seed, cx, cy);
            DoubleArray.Double2 vec = CellularNoise.CELL_2D[cellHash & 255];
            double px = jitter.getPointX(cx, vec);
            double py = jitter.getPointY(cy, vec);
            pointEvaluator.collectPoint(cellHash, cx, cy, px, py, ctx, collector);
         }
      }

   }

   @Nonnull
   public String toString() {
      return "GridCellFunction{}";
   }

   public static int getHash(int seed, int cellX, int cellY) {
      return (int)HashUtil.rehash((long)seed, (long)cellX, (long)cellY);
   }
}
