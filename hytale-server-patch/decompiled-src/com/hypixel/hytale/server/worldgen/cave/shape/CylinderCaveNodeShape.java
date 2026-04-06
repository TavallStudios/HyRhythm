package com.hypixel.hytale.server.worldgen.cave.shape;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.util.bounds.IWorldBounds;
import java.util.Random;
import javax.annotation.Nonnull;

public class CylinderCaveNodeShape extends AbstractCaveNodeShape implements IWorldBounds {
   private final CaveType caveType;
   @Nonnull
   private final Vector3d o;
   @Nonnull
   private final Vector3d v;
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;
   private final double radius1;
   private final double radius2;
   private final double middleRadius;

   public CylinderCaveNodeShape(CaveType caveType, @Nonnull Vector3d o, @Nonnull Vector3d v, double radius1, double radius2, double middleRadius) {
      this.caveType = caveType;
      this.o = o;
      this.v = v;
      this.radius1 = radius1;
      this.radius2 = radius2;
      this.middleRadius = middleRadius;
      this.lowBoundX = MathUtil.floor(Math.min(o.x, o.x + v.x) - Math.max(radius1, radius2));
      this.lowBoundY = MathUtil.floor(Math.min(o.y, o.y + v.y) - Math.max(radius1, radius2));
      this.lowBoundZ = MathUtil.floor(Math.min(o.z, o.z + v.z) - Math.max(radius1, radius2));
      this.highBoundX = MathUtil.ceil(Math.max(o.x, o.x + v.x) + Math.max(radius1, radius2));
      this.highBoundY = MathUtil.ceil(Math.max(o.y, o.y + v.y) + Math.max(radius1, radius2));
      this.highBoundZ = MathUtil.ceil(Math.max(o.z, o.z + v.z) + Math.max(radius1, radius2));
   }

   @Nonnull
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   public Vector3d getEnd() {
      double x = this.o.x + this.v.x;
      double y = this.o.y + this.v.y;
      double z = this.o.z + this.v.z;
      return new Vector3d(x, y, z);
   }

   @Nonnull
   public Vector3d getAnchor(@Nonnull Vector3d vector, double t, double tv, double th) {
      double radius = this.getRadiusAt(t);
      return CaveNodeShapeUtils.getPipeAnchor(vector, this.o, this.v, radius, radius, radius, t, tv, th);
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

   public double getRadius1() {
      return this.radius1;
   }

   public double getRadius2() {
      return this.radius2;
   }

   public boolean shouldReplace(int seed, double x, double z, int y) {
      double t = this.projectPointOnNode(x, (double)y, z);
      if (!(t < 0.0) && !(t > 1.0)) {
         double dx = this.o.x + this.v.x * t;
         double dy = this.o.y + this.v.y * t;
         double dz = this.o.z + this.v.z * t;
         double r = this.getRadiusAt(t) * (double)this.caveType.getHeightRadiusFactor(seed, x, z, MathUtil.floor(dy));
         dx -= x;
         dy -= (double)y;
         dz -= z;
         return dx * dx + dy * dy + dz * dz < r * r;
      } else {
         return false;
      }
   }

   private double projectPointOnNode(double px, double py, double pz) {
      double t = (px - this.o.x) * this.v.x + (py - this.o.y) * this.v.y + (pz - this.o.z) * this.v.z;
      t /= this.v.x * this.v.x + this.v.y * this.v.y + this.v.z * this.v.z;
      return t;
   }

   private double getRadiusAt(double t) {
      return t < 0.5 ? (this.middleRadius - this.radius1) * 2.0 * t + this.radius1 : (this.radius2 - this.middleRadius) * 2.0 * (t - 0.5) + this.middleRadius;
   }

   public double getFloorPosition(int seed, double x, double z) {
      for(int y = this.getLowBoundY(); y < this.getHighBoundY(); ++y) {
         if (this.shouldReplace(seed, x, z, y)) {
            return (double)(y - 1);
         }
      }

      return -1.0;
   }

   public double getCeilingPosition(int seed, double x, double z) {
      for(int y = this.getHighBoundY(); y > this.getLowBoundY(); --y) {
         if (this.shouldReplace(seed, x, z, y)) {
            return (double)(y + 1);
         }
      }

      return -1.0;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.caveType);
      return "CylinderCaveNodeShape{caveType=" + var10000 + ", o=" + String.valueOf(this.o) + ", v=" + String.valueOf(this.v) + ", lowBoundX=" + this.lowBoundX + ", lowBoundY=" + this.lowBoundY + ", lowBoundZ=" + this.lowBoundZ + ", highBoundX=" + this.highBoundX + ", highBoundY=" + this.highBoundY + ", highBoundZ=" + this.highBoundZ + ", radius1=" + this.radius1 + ", radius2=" + this.radius2 + ", middleRadius=" + this.middleRadius + "}";
   }

   public static class CylinderCaveNodeShapeGenerator implements CaveNodeShapeEnum.CaveNodeShapeGenerator {
      private final IDoubleRange radius;
      private final IDoubleRange middleRadius;
      private final IDoubleRange length;
      private final boolean inheritParentRadius;

      public CylinderCaveNodeShapeGenerator(IDoubleRange radius, IDoubleRange middleRadius, IDoubleRange length, boolean inheritParentRadius) {
         this.radius = radius;
         this.middleRadius = middleRadius;
         this.length = length;
         this.inheritParentRadius = inheritParentRadius;
      }

      @Nonnull
      public CaveNodeShape generateCaveNodeShape(@Nonnull Random random, CaveType caveType, CaveNode parentNode, @Nonnull CaveNodeType.CaveNodeChildEntry childEntry, @Nonnull Vector3d origin, float yaw, float pitch) {
         double l = this.length.getValue(random.nextDouble());
         Vector3d direction = (new Vector3d((double)(TrigMathUtil.sin(pitch) * TrigMathUtil.cos(yaw)), (double)TrigMathUtil.cos(pitch), (double)(TrigMathUtil.sin(pitch) * TrigMathUtil.sin(yaw)))).scale(l);
         double radius1;
         if (this.inheritParentRadius) {
            radius1 = CaveNodeShapeUtils.getEndRadius(parentNode, this.radius, random);
         } else {
            radius1 = this.radius.getValue(random.nextDouble());
         }

         double radius2 = this.radius.getValue(random.nextDouble());
         double radius3 = (radius2 - radius1) * 0.5 + radius1;
         if (this.middleRadius != null) {
            radius3 = this.middleRadius.getValue(random.nextDouble());
         }

         Vector3d offset = CaveNodeShapeUtils.getOffset(parentNode, childEntry);
         origin.add(offset);
         return new CylinderCaveNodeShape(caveType, origin, direction, radius1, radius2, radius3);
      }
   }
}
