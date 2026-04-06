package com.hypixel.hytale.procedurallib.logic.point;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistortedPointGenerator implements IPointGenerator {
   protected final IPointGenerator pointGenerator;
   protected final ICoordinateRandomizer coordinateRandomizer;

   public DistortedPointGenerator(IPointGenerator pointGenerator, ICoordinateRandomizer coordinateRandomizer) {
      this.pointGenerator = pointGenerator;
      this.coordinateRandomizer = coordinateRandomizer;
   }

   public ResultBuffer.ResultBuffer2d nearest2D(int seed, double x, double y) {
      return this.pointGenerator.nearest2D(seed, this.coordinateRandomizer.randomDoubleX(seed, x, y), this.coordinateRandomizer.randomDoubleY(seed, x, y));
   }

   public ResultBuffer.ResultBuffer3d nearest3D(int seed, double x, double y, double z) {
      return this.pointGenerator.nearest3D(seed, this.coordinateRandomizer.randomDoubleX(seed, x, y, z), this.coordinateRandomizer.randomDoubleY(seed, x, y, z), this.coordinateRandomizer.randomDoubleZ(seed, x, y, z));
   }

   public ResultBuffer.ResultBuffer2d transition2D(int seed, double x, double y) {
      return this.pointGenerator.transition2D(seed, this.coordinateRandomizer.randomDoubleX(seed, x, y), this.coordinateRandomizer.randomDoubleY(seed, x, y));
   }

   public ResultBuffer.ResultBuffer3d transition3D(int seed, double x, double y, double z) {
      return this.pointGenerator.transition3D(seed, this.coordinateRandomizer.randomDoubleX(seed, x, y, z), this.coordinateRandomizer.randomDoubleY(seed, x, y, z), this.coordinateRandomizer.randomDoubleZ(seed, x, y, z));
   }

   public double getInterval() {
      return this.pointGenerator.getInterval();
   }

   public void collect(int seed, double minX, double minY, double maxX, double maxY, IPointGenerator.PointConsumer2d consumer) {
      this.pointGenerator.collect(seed, minX, minY, maxX, maxY, consumer);
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DistortedPointGenerator that = (DistortedPointGenerator)o;
         return !this.pointGenerator.equals(that.pointGenerator) ? false : this.coordinateRandomizer.equals(that.coordinateRandomizer);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.pointGenerator.hashCode();
      result = 31 * result + this.coordinateRandomizer.hashCode();
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.pointGenerator);
      return "DistortedPointGenerator{pointGenerator=" + var10000 + ", coordinateRandomizer=" + String.valueOf(this.coordinateRandomizer) + "}";
   }
}
