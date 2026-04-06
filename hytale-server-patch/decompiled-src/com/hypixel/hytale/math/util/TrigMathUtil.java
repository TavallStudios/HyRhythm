package com.hypixel.hytale.math.util;

import javax.annotation.Nonnull;

public class TrigMathUtil {
   public static final float PI = 3.1415927F;
   public static final float PI_HALF = 1.5707964F;
   public static final float PI_QUARTER = 0.7853982F;
   public static final float PI2 = 6.2831855F;
   public static final float PI4 = 12.566371F;
   public static final float radToDeg = 57.295776F;
   public static final float degToRad = 0.017453292F;

   public static float sin(float radians) {
      return TrigMathUtil.Riven.sin(radians);
   }

   public static float cos(float radians) {
      return TrigMathUtil.Riven.cos(radians);
   }

   public static float sin(double radians) {
      return TrigMathUtil.Riven.sin((float)radians);
   }

   public static float cos(double radians) {
      return TrigMathUtil.Riven.cos((float)radians);
   }

   public static float atan2(float y, float x) {
      return TrigMathUtil.Icecore.atan2(y, x);
   }

   public static float atan2(double y, double x) {
      return TrigMathUtil.Icecore.atan2((float)y, (float)x);
   }

   public static float atan(double d) {
      return (float)Math.atan(d);
   }

   public static float asin(double d) {
      return (float)Math.asin(d);
   }

   private TrigMathUtil() {
   }

   private static final class Riven {
      private static final int SIN_BITS = 12;
      private static final int SIN_MASK;
      private static final int SIN_COUNT;
      private static final float radFull;
      private static final float radToIndex;
      private static final float degFull;
      private static final float degToIndex;
      @Nonnull
      private static final float[] SIN;
      @Nonnull
      private static final float[] COS;

      public static float sin(float rad) {
         return SIN[(int)(rad * radToIndex) & SIN_MASK];
      }

      public static float cos(float rad) {
         return COS[(int)(rad * radToIndex) & SIN_MASK];
      }

      static {
         SIN_MASK = ~(-1 << SIN_BITS);
         SIN_COUNT = SIN_MASK + 1;
         radFull = 6.2831855F;
         degFull = 360.0F;
         radToIndex = (float)SIN_COUNT / radFull;
         degToIndex = (float)SIN_COUNT / degFull;
         SIN = new float[SIN_COUNT];
         COS = new float[SIN_COUNT];

         for(int i = 0; i < SIN_COUNT; ++i) {
            SIN[i] = (float)Math.sin((double)(((float)i + 0.5F) / (float)SIN_COUNT * radFull));
            COS[i] = (float)Math.cos((double)(((float)i + 0.5F) / (float)SIN_COUNT * radFull));
         }

         for(int i = 0; i < 360; i += 90) {
            SIN[(int)((float)i * degToIndex) & SIN_MASK] = (float)Math.sin((double)i * 3.141592653589793 / 180.0);
            COS[(int)((float)i * degToIndex) & SIN_MASK] = (float)Math.cos((double)i * 3.141592653589793 / 180.0);
         }

      }
   }

   private static final class Icecore {
      private static final int SIZE_AC = 100000;
      private static final int SIZE_AR = 100001;
      private static final float[] ATAN2 = new float[100001];

      public static float atan2(float y, float x) {
         if (y < 0.0F) {
            if (x < 0.0F) {
               return y < x ? -ATAN2[(int)(x / y * 100000.0F)] - 1.5707964F : ATAN2[(int)(y / x * 100000.0F)] - 3.1415927F;
            } else {
               y = -y;
               return y > x ? ATAN2[(int)(x / y * 100000.0F)] - 1.5707964F : -ATAN2[(int)(y / x * 100000.0F)];
            }
         } else if (x < 0.0F) {
            x = -x;
            return y > x ? ATAN2[(int)(x / y * 100000.0F)] + 1.5707964F : -ATAN2[(int)(y / x * 100000.0F)] + 3.1415927F;
         } else {
            return y > x ? -ATAN2[(int)(x / y * 100000.0F)] + 1.5707964F : ATAN2[(int)(y / x * 100000.0F)];
         }
      }

      static {
         for(int i = 0; i <= 100000; ++i) {
            double d = (double)i / 100000.0;
            double x = 1.0;
            double y = x * d;
            float v = (float)Math.atan2(y, x);
            ATAN2[i] = v;
         }

      }
   }
}
