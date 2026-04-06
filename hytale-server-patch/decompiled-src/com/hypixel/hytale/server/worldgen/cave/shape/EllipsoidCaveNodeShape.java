package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

public class EllipsoidCaveNodeShape extends AbstractCaveNodeShape implements IWorldBounds {
   private final CaveType caveType;
   @Nonnull
   private final Vector3d o;
   private final double rx;
   private final double ry;
   private final double rz;
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;

   public EllipsoidCaveNodeShape(CaveType caveType, @Nonnull Vector3d o, double rx, double ry, double rz) {
      this.caveType = caveType;
      this.o = o;
      this.rx = rx;
      this.ry = ry;
      this.rz = rz;
      this.lowBoundX = MathUtil.floor(Math.min(o.x - rx, o.x + rx));
      this.lowBoundY = MathUtil.floor(Math.min(o.y - ry, o.y + ry));
      this.lowBoundZ = MathUtil.floor(Math.min(o.z - rz, o.z + rz));
      this.highBoundX = MathUtil.ceil(Math.max(o.x - rx, o.x + rx));
      this.highBoundY = MathUtil.ceil(Math.max(o.y - ry, o.y + ry));
      this.highBoundZ = MathUtil.ceil(Math.max(o.z - rz, o.z + rz));
   }

   @Nonnull
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   public Vector3d getEnd() {
      return new Vector3d(this.o.x, (double)this.lowBoundY, this.o.z);
   }

   @Nonnull
   public Vector3d getAnchor(@Nonnull Vector3d vector, double tx, double ty, double tz) {
      return CaveNodeShapeUtils.getSphereAnchor(vector, this.o, this.rx, this.ry, this.rz, tx, ty, tz);
   }

   @Nonnull
   public IWorldBounds getBounds() {
      return this;
   }

   public int getLowBoundX() {
      return this.lowBoundX;
   }

   public int getLowBoundZ() {
      return this.lowBoundZ;
   }

   public int getHighBoundX() {
      return this.highBoundX;
   }

   public int getHighBoundZ() {
      return this.highBoundZ;
   }

   public int getLowBoundY() {
      return this.lowBoundY;
   }

   public int getHighBoundY() {
      return this.highBoundY;
   }

   public boolean shouldReplace(int seed, double x, double z, int y) {
      double fy = (double)y;
      double fx = x - this.o.x;
      fy -= this.o.y;
      double fz = z - this.o.z;
      fx /= this.rx;
      fy /= this.ry;
      fz /= this.rz;
      double t = (double)this.caveType.getHeightRadiusFactor(seed, x, z, y);
      return fx * fx + fy * fy + fz * fz <= t * t;
   }

   public double getFloorPosition(int seed, double x, double z) {
      for(int y = this.getLowBoundY(); (double)y < this.o.y; ++y) {
         if (this.shouldReplace(seed, x, z, y)) {
            return (double)(y - 1);
         }
      }

      return -1.0;
   }

   public double getCeilingPosition(int seed, double x, double z) {
      for(int y = this.getHighBoundY(); (double)y > this.o.y; --y) {
         if (this.shouldReplace(seed, x, z, y)) {
            return (double)(y + 1);
         }
      }

      return -1.0;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.caveType);
      return "EllipsoidCaveNodeShape{caveType=" + var10000 + ", o=" + String.valueOf(this.o) + ", rx=" + this.rx + ", ry=" + this.ry + ", rz=" + this.rz + ", lowBoundX=" + this.lowBoundX + ", lowBoundY=" + this.lowBoundY + ", lowBoundZ=" + this.lowBoundZ + ", highBoundX=" + this.highBoundX + ", highBoundY=" + this.highBoundY + ", highBoundZ=" + this.highBoundZ + "}";
   }

   public static class EllipsoidCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final IDoubleRange radiusX;
      private final IDoubleRange radiusY;
      private final IDoubleRange radiusZ;

      public EllipsoidCaveNodeShapeGenerator(IDoubleRange radiusX, IDoubleRange radiusY, IDoubleRange radiusZ) {
         this.radiusX = radiusX;
         this.radiusY = radiusY;
         this.radiusZ = radiusZ;
      }

      @Nonnull
      public CaveNodeShape generateCaveNodeShape(Random random, CaveType caveType, CaveNode parentNode, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry, @Nonnull Vector3d origin, float yaw, float pitch) {
         double rx = this.radiusX.getValue(random);
         double ry = this.radiusY.getValue(random);
         double rz = this.radiusZ.getValue(random);
         Vector3d offset = CaveNodeShapeUtils.getOffset(parentNode, childEntry);
         origin.add(offset);
         return new EllipsoidCaveNodeShape(caveType, origin, rx, ry, rz);
      }
   }
}
