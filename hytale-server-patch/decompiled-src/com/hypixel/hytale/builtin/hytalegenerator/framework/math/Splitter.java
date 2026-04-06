package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import javax.annotation.Nonnull;

public class Splitter {
   @Nonnull
   public static Range[] split(@Nonnull Range range, int pieces) {
      if (pieces < 0) {
         throw new IllegalArgumentException("negative number of pieces");
      } else {
         int size = range.max - range.min;
         int pieceSize = size / pieces;
         if (size % pieces > 0) {
            ++pieceSize;
         }

         Range[] output = new Range[pieces];

         for(int i = 0; i < output.length; ++i) {
            int min = Math.min(i * pieceSize + range.min, range.max);
            int max = Math.min(min + pieceSize, range.max);
            output[i] = new Range(min, max);
         }

         return output;
      }
   }

   @Nonnull
   public static Area[] split(@Nonnull Area area, int pieces) {
      if (pieces < 1) {
         throw new IllegalArgumentException("negative number of pieces");
      } else if (pieces == 1) {
         return new Area[]{area};
      } else {
         int sizeX = area.maxX - area.minX;
         int var10000 = area.maxZ - area.minZ;
         if (pieces > sizeX) {
            pieces = sizeX;
         }

         Area[] output = new Area[pieces];
         if (pieces % 3 == 0) {
            Range[] rangesX = split((Range)(new Range(area.minX, area.maxX)), 3);
            Range[] rangesZ = split(new Range(area.minZ, area.maxZ), pieces / 3);
            int o = 0;

            for(Range x : rangesX) {
               for(Range range : rangesZ) {
                  output[o++] = new Area(x.min, range.min, x.max, range.max);
               }
            }
         } else if (pieces % 2 == 0) {
            Range[] rangesX = split((Range)(new Range(area.minX, area.maxX)), 2);
            Range[] rangesZ = split(new Range(area.minZ, area.maxZ), pieces / 2);
            int o = 0;

            for(Range x : rangesX) {
               for(Range range : rangesZ) {
                  output[o++] = new Area(x.min, range.min, x.max, range.max);
               }
            }
         } else {
            Range[] ranges = split(new Range(area.minX, area.maxX), pieces);

            for(int i = 0; i < ranges.length; ++i) {
               output[i] = new Area(ranges[i].min, area.minZ, ranges[i].max, area.maxZ);
            }
         }

         return output;
      }
   }

   @Nonnull
   public static Area[] splitX(@Nonnull Area area, int pieces) {
      if (pieces < 1) {
         throw new IllegalArgumentException("negative number of pieces");
      } else if (pieces == 1) {
         return new Area[]{area};
      } else {
         int sizeX = area.maxX - area.minX;
         int var10000 = area.maxZ - area.minZ;
         if (pieces > sizeX) {
            pieces = sizeX;
         }

         Area[] output = new Area[pieces];
         Range[] ranges = split(new Range(area.minX, area.maxX), pieces);

         for(int i = 0; i < ranges.length; ++i) {
            output[i] = new Area(ranges[i].min, area.minZ, ranges[i].max, area.maxZ);
         }

         return output;
      }
   }

   public static class Area {
      public final int minX;
      public final int minZ;
      public final int maxX;
      public final int maxZ;

      public Area(int minX, int minZ, int maxX, int maxZ) {
         if (maxX >= minX && maxZ >= minZ) {
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
         } else {
            throw new IllegalArgumentException("max smaller than min");
         }
      }

      @Nonnull
      public String toString() {
         return "Area{minX=" + this.minX + ", minZ=" + this.minZ + ", maxX=" + this.maxX + ", maxZ=" + this.maxZ + "}";
      }
   }

   public static class Range {
      public final int min;
      public final int max;

      public Range(int min, int max) {
         if (max < min) {
            throw new IllegalArgumentException("max smaller than min");
         } else {
            this.min = min;
            this.max = max;
         }
      }
   }
}
