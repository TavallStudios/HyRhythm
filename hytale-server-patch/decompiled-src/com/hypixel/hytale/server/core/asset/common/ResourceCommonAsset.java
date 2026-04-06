package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceCommonAsset extends CommonAsset {
   private final Class<?> clazz;
   private final String path;

   public ResourceCommonAsset(Class<?> clazz, String path, @Nonnull String name, byte[] bytes) {
      super(name, bytes);
      this.clazz = clazz;
      this.path = path;
   }

   public ResourceCommonAsset(Class<?> clazz, String path, @Nonnull String name, @Nonnull String hash, byte[] bytes) {
      super(name, hash, bytes);
      this.clazz = clazz;
      this.path = path;
   }

   public String getPath() {
      return this.path;
   }

   @Nonnull
   public CompletableFuture<byte[]> getBlob0() {
      try {
         InputStream stream = this.clazz.getResourceAsStream(this.path);

         CompletableFuture var2;
         try {
            var2 = CompletableFuture.completedFuture(stream.readAllBytes());
         } catch (Throwable var5) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (stream != null) {
            stream.close();
         }

         return var2;
      } catch (IOException e) {
         return CompletableFuture.failedFuture(e);
      }
   }

   @Nonnull
   public String toString() {
      return "ResourceCommonAsset{" + super.toString() + "}";
   }

   @Nullable
   public static ResourceCommonAsset of(@Nonnull Class<?> clazz, @Nonnull String path, @Nonnull String name) {
      try {
         InputStream stream = clazz.getResourceAsStream(path);

         Object var9;
         label48: {
            ResourceCommonAsset var5;
            try {
               if (stream == null) {
                  var9 = null;
                  break label48;
               }

               byte[] bytes = stream.readAllBytes();
               var5 = new ResourceCommonAsset(clazz, path, name, bytes);
            } catch (Throwable var7) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stream != null) {
               stream.close();
            }

            return var5;
         }

         if (stream != null) {
            stream.close();
         }

         return (ResourceCommonAsset)var9;
      } catch (IOException e) {
         throw SneakyThrow.sneakyThrow(e);
      }
   }
}
