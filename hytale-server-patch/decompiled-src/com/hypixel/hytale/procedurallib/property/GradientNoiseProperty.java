package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;

public class GradientNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noise;
   protected final GradientMode mode;
   protected final double distance;
   protected final double invNormalize;

   public GradientNoiseProperty(NoiseProperty noise, GradientMode mode, double distance, double normalize) {
      this.noise = noise;
      this.mode = mode;
      this.distance = distance;
      this.invNormalize = 1.0 / distance / normalize;
   }

   public double get(int seed, double x, double y) {
      double v = this.noise.get(seed, x, y);
      double s = this.noise.get(seed, x, y + this.distance);
      double e = this.noise.get(seed, x + this.distance, y);
      double dx = e - v;
      double dy = s - v;
      double var10000;
      switch (this.mode.ordinal()) {
         case 0 -> var10000 = getMagnitude(dx, dy, this.invNormalize);
         case 1 -> var10000 = getAngle(dx, dy);
         case 2 -> var10000 = getAbsAngle(dx, dy);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public double get(int seed, double x, double y, double z) {
      throw new UnsupportedOperationException();
   }

   protected static double getAngle(double dx, double dy) {
      float angle = TrigMathUtil.atan2(dy, dx);
      angle = (angle + 3.1415927F) / 6.2831855F;
      return (double)convertRange(angle);
   }

   protected static double getAbsAngle(double dx, double dy) {
      float angle = TrigMathUtil.atan2(dy, dx);
      return (double)(Math.abs(angle) / 3.1415927F);
   }

   protected static double getMagnitude(double dx, double dy, double invNormalize) {
      double mag = MathUtil.length(dx, dy);
      return MathUtil.clamp(mag * invNormalize, 0.0, 1.0);
   }

   protected static float convertRange(float angle) {
      angle += 0.125F;
      return angle > 1.0F ? angle - 1.0F : angle;
   }

   public static enum GradientMode {
      MAGNITUDE,
      ANGLE,
      ANGLE_ABS;
   }
}
