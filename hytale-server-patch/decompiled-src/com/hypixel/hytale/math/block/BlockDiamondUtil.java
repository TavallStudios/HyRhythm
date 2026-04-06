package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDiamondUtil {
   public static <T> boolean forEachBlock(int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = (float)radiusX + 0.41F;
         float radiusZAdjusted = (float)radiusZ + 0.41F;

         for(int y = 0; y <= radiusY; ++y) {
            float normalizedY = (float)y / (float)radiusY;
            float currentRadiusX = radiusXAdjusted * (1.0F - normalizedY);
            float currentRadiusZ = radiusZAdjusted * (1.0F - normalizedY);
            int maxX = (int)currentRadiusX;
            int maxZ = (int)currentRadiusZ;

            for(int x = 0; x <= maxX; ++x) {
               for(int z = 0; z <= maxZ; ++z) {
                  if ((float)Math.abs(x) <= currentRadiusX && (float)Math.abs(z) <= currentRadiusZ && !test(originX, originY, originZ, x, y, z, t, consumer)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ, int thickness, boolean capped, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = (float)radiusX + 0.41F;
         float radiusZAdjusted = (float)radiusZ + 0.41F;

         for(int y = 0; y <= radiusY; ++y) {
            float normalizedY = (float)y / (float)radiusY;
            float currentRadiusX = radiusXAdjusted * (1.0F - normalizedY);
            float currentRadiusZ = radiusZAdjusted * (1.0F - normalizedY);
            float innerRadiusX = Math.max(0.0F, currentRadiusX - (float)thickness);
            float innerRadiusZ = Math.max(0.0F, currentRadiusZ - (float)thickness);
            int maxX = (int)currentRadiusX;
            int maxZ = (int)currentRadiusZ;

            for(int x = 0; x <= maxX; ++x) {
               for(int z = 0; z <= maxZ; ++z) {
                  boolean inOuter = (float)Math.abs(x) <= currentRadiusX && (float)Math.abs(z) <= currentRadiusZ;
                  if (inOuter) {
                     boolean inInner = (float)Math.abs(x) < innerRadiusX && (float)Math.abs(z) < innerRadiusZ;
                     if (!inInner && !test(originX, originY, originZ, x, y, z, t, consumer)) {
                        return false;
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   private static <T> boolean test(int originX, int originY, int originZ, int x, int y, int z, T context, @Nonnull TriIntObjPredicate<T> consumer) {
      if (!consumer.test(originX + x, originY + y, originZ + z, context)) {
         return false;
      } else if (y > 0 && !consumer.test(originX + x, originY - y, originZ + z, context)) {
         return false;
      } else {
         if (x > 0) {
            if (!consumer.test(originX - x, originY + y, originZ + z, context)) {
               return false;
            }

            if (y > 0 && !consumer.test(originX - x, originY - y, originZ + z, context)) {
               return false;
            }

            if (z > 0 && !consumer.test(originX - x, originY + y, originZ - z, context)) {
               return false;
            }

            if (y > 0 && z > 0 && !consumer.test(originX - x, originY - y, originZ - z, context)) {
               return false;
            }
         }

         if (z > 0) {
            if (!consumer.test(originX + x, originY + y, originZ - z, context)) {
               return false;
            }

            if (y > 0 && !consumer.test(originX + x, originY - y, originZ - z, context)) {
               return false;
            }
         }

         return true;
      }
   }
}
