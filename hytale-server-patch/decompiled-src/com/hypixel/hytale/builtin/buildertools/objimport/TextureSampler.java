package com.hypixel.hytale.builtin.buildertools.objimport;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public final class TextureSampler {
   private static final Map<Path, BufferedImage> textureCache = new HashMap();

   private TextureSampler() {
   }

   @Nullable
   public static BufferedImage loadTexture(@Nonnull Path path) {
      if (!Files.exists(path, new LinkOption[0])) {
         return null;
      } else {
         BufferedImage cached = (BufferedImage)textureCache.get(path);
         if (cached != null) {
            return cached;
         } else {
            try {
               BufferedImage image = ImageIO.read(path.toFile());
               if (image != null) {
                  textureCache.put(path, image);
               }

               return image;
            } catch (IOException var3) {
               return null;
            }
         }
      }
   }

   @Nonnull
   public static int[] sampleAt(@Nonnull BufferedImage texture, float u, float v) {
      u -= (float)Math.floor((double)u);
      v -= (float)Math.floor((double)v);
      v = 1.0F - v;
      int width = texture.getWidth();
      int height = texture.getHeight();
      int x = Math.min((int)(u * (float)width), width - 1);
      int y = Math.min((int)(v * (float)height), height - 1);
      int rgb = texture.getRGB(x, y);
      return new int[]{rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255};
   }

   public static int sampleAlphaAt(@Nonnull BufferedImage texture, float u, float v) {
      if (!texture.getColorModel().hasAlpha()) {
         return 255;
      } else {
         u -= (float)Math.floor((double)u);
         v -= (float)Math.floor((double)v);
         v = 1.0F - v;
         int width = texture.getWidth();
         int height = texture.getHeight();
         int x = Math.min((int)(u * (float)width), width - 1);
         int y = Math.min((int)(v * (float)height), height - 1);
         int rgba = texture.getRGB(x, y);
         return rgba >> 24 & 255;
      }
   }

   public static void clearCache() {
      textureCache.clear();
   }

   @Nullable
   public static int[] getAverageColor(@Nonnull Path path) {
      if (!Files.exists(path, new LinkOption[0])) {
         return null;
      } else {
         try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
               return null;
            } else {
               long totalR = 0L;
               long totalG = 0L;
               long totalB = 0L;
               int count = 0;
               int width = image.getWidth();
               int height = image.getHeight();
               boolean hasAlpha = image.getColorModel().hasAlpha();

               for(int y = 0; y < height; ++y) {
                  for(int x = 0; x < width; ++x) {
                     int rgba = image.getRGB(x, y);
                     if (hasAlpha) {
                        int alpha = rgba >> 24 & 255;
                        if (alpha == 0) {
                           continue;
                        }
                     }

                     int r = rgba >> 16 & 255;
                     int g = rgba >> 8 & 255;
                     int b = rgba & 255;
                     totalR += (long)r;
                     totalG += (long)g;
                     totalB += (long)b;
                     ++count;
                  }
               }

               if (count == 0) {
                  return null;
               } else {
                  return new int[]{(int)(totalR / (long)count), (int)(totalG / (long)count), (int)(totalB / (long)count)};
               }
            }
         } catch (IOException var18) {
            return null;
         }
      }
   }
}
