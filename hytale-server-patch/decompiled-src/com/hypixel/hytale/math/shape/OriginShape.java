package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class OriginShape<S extends Shape> implements Shape {
   public final Vector3d origin;
   public S shape;

   public OriginShape() {
      this.origin = new Vector3d();
   }

   public OriginShape(Vector3d origin, S shape) {
      this.origin = origin;
      this.shape = shape;
   }

   public Vector3d getOrigin() {
      return this.origin;
   }

   public S getShape() {
      return this.shape;
   }

   public Box getBox(double x, double y, double z) {
      return this.shape.getBox(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ());
   }

   public boolean containsPosition(double x, double y, double z) {
      return this.shape.containsPosition(x - this.origin.getX(), y - this.origin.getY(), z - this.origin.getZ());
   }

   public void expand(double radius) {
      this.shape.expand(radius);
   }

   public boolean forEachBlock(double x, double y, double z, double epsilon, TriIntPredicate consumer) {
      return this.shape.forEachBlock(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ(), epsilon, consumer);
   }

   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, TriIntObjPredicate<T> consumer) {
      return this.shape.forEachBlock(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ(), epsilon, t, consumer);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.origin);
      return "OriginShape{origin=" + var10000 + ", shape=" + String.valueOf(this.shape) + "}";
   }
}
