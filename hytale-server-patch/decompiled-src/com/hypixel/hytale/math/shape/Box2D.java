package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2d;
import javax.annotation.Nonnull;

public class Box2D implements Shape2D {
   public static final BuilderCodec<Box2D> CODEC;
   @Nonnull
   public final Vector2d min;
   @Nonnull
   public final Vector2d max;

   public Box2D() {
      this.min = new Vector2d();
      this.max = new Vector2d();
   }

   public Box2D(@Nonnull Box2D box) {
      this();
      this.min.assign(box.min);
      this.max.assign(box.max);
   }

   public Box2D(@Nonnull Vector2d min, @Nonnull Vector2d max) {
      this();
      this.min.assign(min);
      this.max.assign(max);
   }

   public Box2D(double xMin, double yMin, double xMax, double yMax) {
      this();
      this.min.assign(xMin, yMin);
      this.max.assign(xMax, yMax);
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull Vector2d min, @Nonnull Vector2d max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull double[] min, @Nonnull double[] max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(@Nonnull float[] min, @Nonnull float[] max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box2D setEmpty() {
      this.setMinMax(1.7976931348623157E308, -1.7976931348623157E308);
      return this;
   }

   @Nonnull
   public Box2D setMinMax(double min, double max) {
      this.min.assign(min);
      this.max.assign(max);
      return this;
   }

   @Nonnull
   public Box2D union(@Nonnull Box2D bb) {
      if (this.min.x > bb.min.x) {
         this.min.x = bb.min.x;
      }

      if (this.min.y > bb.min.y) {
         this.min.y = bb.min.y;
      }

      if (this.max.x < bb.max.x) {
         this.max.x = bb.max.x;
      }

      if (this.max.y < bb.max.y) {
         this.max.y = bb.max.y;
      }

      return this;
   }

   @Nonnull
   public Box2D assign(@Nonnull Box2D other) {
      this.min.assign(other.min);
      this.max.assign(other.max);
      return this;
   }

   @Nonnull
   public Box2D minkowskiSum(@Nonnull Box2D bb) {
      this.min.subtract(bb.max);
      this.max.subtract(bb.min);
      return this;
   }

   @Nonnull
   public Box2D normalize() {
      if (this.min.x > this.max.x) {
         double t = this.min.x;
         this.min.x = this.max.x;
         this.max.x = t;
      }

      if (this.min.y > this.max.y) {
         double t = this.min.y;
         this.min.y = this.max.y;
         this.max.y = t;
      }

      return this;
   }

   @Nonnull
   public Box2D offset(@Nonnull Vector2d pos) {
      this.min.add(pos);
      this.max.add(pos);
      return this;
   }

   @Nonnull
   public Box2D sweep(@Nonnull Vector2d v) {
      if (v.x < 0.0) {
         Vector2d var10000 = this.min;
         var10000.x += v.x;
      } else if (v.x > 0.0) {
         Vector2d var2 = this.max;
         var2.x += v.x;
      }

      if (v.y < 0.0) {
         Vector2d var3 = this.min;
         var3.y += v.y;
      } else if (v.y > 0.0) {
         Vector2d var4 = this.max;
         var4.y += v.y;
      }

      return this;
   }

   @Nonnull
   public Box2D extendToInt() {
      this.min.floor();
      this.max.ceil();
      return this;
   }

   @Nonnull
   public Box2D extend(double extentX, double extentY) {
      this.min.subtract(extentX, extentY);
      this.max.add(extentX, extentY);
      return this;
   }

   public double width() {
      return this.max.x - this.min.x;
   }

   public double height() {
      return this.max.y - this.min.y;
   }

   public boolean isIntersecting(@Nonnull Box2D other) {
      return !(this.min.x > other.max.x) && !(other.min.x > this.max.x) && !(this.min.y > other.max.y) && !(other.min.y > this.max.y);
   }

   @Nonnull
   public Box2D getBox(double x, double y) {
      return new Box2D(this.min.getX() + x, this.min.getY() + y, this.max.getX() + x, this.max.getY() + y);
   }

   public boolean containsPosition(@Nonnull Vector2d origin, @Nonnull Vector2d position) {
      double x = position.getX() - origin.getX();
      double y = position.getY() - origin.getY();
      return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY();
   }

   public boolean containsPosition(@Nonnull Vector2d origin, double xx, double yy) {
      double x = xx - origin.getX();
      double y = yy - origin.getY();
      return x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY();
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.min);
      return "Box2D{min=" + var10000 + ", max=" + String.valueOf(this.max) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Box2D.class, Box2D::new).append(new KeyedCodec("Min", Vector2d.CODEC), (shape, min) -> shape.min.assign(min), (shape) -> shape.min).add()).append(new KeyedCodec("Max", Vector2d.CODEC), (shape, max) -> shape.max.assign(max), (shape) -> shape.max).add()).build();
   }
}
