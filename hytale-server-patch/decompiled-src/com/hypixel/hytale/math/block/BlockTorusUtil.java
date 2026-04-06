package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTorusUtil {
   public static <T> boolean forEachBlock(int originX, int originY, int originZ, int outerRadius, int minorRadius, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      if (outerRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(outerRadius));
      } else if (minorRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(minorRadius));
      } else {
         int majorRadius = Math.max(1, outerRadius - minorRadius);
         int sizeXZ = majorRadius + minorRadius;
         float minorRadiusAdjusted = (float)minorRadius + 0.41F;

         for(int x = -sizeXZ; x <= sizeXZ; ++x) {
            for(int z = -sizeXZ; z <= sizeXZ; ++z) {
               double distFromCenter = Math.sqrt((double)(x * x + z * z));
               double distFromRing = distFromCenter - (double)majorRadius;

               for(int y = -minorRadius; y <= minorRadius; ++y) {
                  double distFromTube = Math.sqrt(distFromRing * distFromRing + (double)(y * y));
                  if (distFromTube <= (double)minorRadiusAdjusted && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(int originX, int originY, int originZ, int outerRadius, int minorRadius, int thickness, boolean capped, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, outerRadius, minorRadius, t, consumer);
      } else if (outerRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(outerRadius));
      } else if (minorRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(minorRadius));
      } else {
         int majorRadius = Math.max(1, outerRadius - minorRadius);
         int sizeXZ = majorRadius + minorRadius;
         float minorRadiusAdjusted = (float)minorRadius + 0.41F;
         float innerMinorRadius = Math.max(0.0F, minorRadiusAdjusted - (float)thickness);

         for(int x = -sizeXZ; x <= sizeXZ; ++x) {
            for(int z = -sizeXZ; z <= sizeXZ; ++z) {
               double distFromCenter = Math.sqrt((double)(x * x + z * z));
               double distFromRing = distFromCenter - (double)majorRadius;

               for(int y = -minorRadius; y <= minorRadius; ++y) {
                  double distFromTube = Math.sqrt(distFromRing * distFromRing + (double)(y * y));
                  boolean inOuter = distFromTube <= (double)minorRadiusAdjusted;
                  if (inOuter) {
                     boolean inInner = distFromTube < (double)innerMinorRadius;
                     if (!inInner && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                        return false;
                     }
                  }
               }
            }
         }

         return true;
      }
   }
}
