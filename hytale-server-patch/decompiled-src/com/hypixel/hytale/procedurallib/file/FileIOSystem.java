package com.hypixel.hytale.procedurallib.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;

public interface FileIOSystem extends AutoCloseable {
   @Nonnull
   Path baseRoot();

   @Nonnull
   PathArray roots();

   @Nonnull
   default AssetPath resolve(@Nonnull Path path) {
      Path relPath = FileIO.relativize(path, this.baseRoot());

      for(Path root : this.roots().paths) {
         AssetPath assetPath = AssetPath.fromRelative(root, relPath);
         if (FileIO.exists(assetPath)) {
            return assetPath;
         }
      }

      return AssetPath.fromRelative(this.baseRoot(), relPath);
   }

   @Nonnull
   default <T> T load(@Nonnull AssetPath path, @Nonnull AssetLoader<T> loader) throws IOException {
      if (!Files.exists(path.filepath(), new LinkOption[0])) {
         throw new FileNotFoundException("Unable to find file: " + String.valueOf(path));
      } else {
         InputStream stream = Files.newInputStream(path.filepath());

         Object var4;
         try {
            var4 = loader.load(stream);
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

         return (T)var4;
      }
   }

   default void close() {
   }

   public static final class PathArray {
      final Path[] paths;

      public PathArray(Path... paths) {
         this.paths = paths;
      }

      public int size() {
         return this.paths.length;
      }

      public Path get(int index) {
         return this.paths[index];
      }
   }

   public static final class Provider {
      private static final DefaultIOFileSystem DEFAULT = new DefaultIOFileSystem();
      private static final ThreadLocal<FileIOSystem> HOLDER = ThreadLocal.withInitial(() -> DEFAULT);

      static FileIOSystem get() {
         return (FileIOSystem)HOLDER.get();
      }

      static void set(@Nonnull FileIOSystem fs) {
         HOLDER.set(fs);
      }

      static void unset() {
         HOLDER.set(DEFAULT);
      }

      static void setRoot(@Nonnull Path path) {
         DEFAULT.setBase(path);
      }

      private static final class DefaultIOFileSystem implements FileIOSystem {
         private static final Path DEFAULT_ROOT = Paths.get(".").toAbsolutePath();
         private Path base;
         private PathArray roots;

         private DefaultIOFileSystem() {
            this.base = DEFAULT_ROOT;
            this.roots = new PathArray(new Path[]{DEFAULT_ROOT});
         }

         public synchronized void setBase(Path base) {
            this.base = base;
            this.roots = new PathArray(new Path[]{base});
         }

         @Nonnull
         public synchronized Path baseRoot() {
            return this.base;
         }

         @Nonnull
         public synchronized PathArray roots() {
            return this.roots;
         }
      }
   }
}
