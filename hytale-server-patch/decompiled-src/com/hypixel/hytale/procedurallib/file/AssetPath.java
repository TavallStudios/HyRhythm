package com.hypixel.hytale.procedurallib.file;

import java.nio.file.Path;
import java.util.Comparator;
import javax.annotation.Nonnull;

public final class AssetPath {
   private final Path path;
   private final Path filepath;
   private final transient int hash;
   public static final Comparator<AssetPath> COMPARATOR = (a, b) -> {
      int max = Math.min(a.filepath.getNameCount(), b.filepath.getNameCount());

      for(int i = 0; i < max; ++i) {
         int comp = a.filepath.getName(i).toString().compareTo(b.filepath.getName(i).toString());
         if (comp != 0) {
            return comp;
         }
      }

      return Integer.compare(a.filepath.getNameCount(), b.filepath.getNameCount());
   };

   private AssetPath(@Nonnull Path path, @Nonnull Path filepath) {
      this.path = path;
      this.filepath = filepath;
      this.hash = FileIO.hashCode(path);
   }

   @Nonnull
   public AssetPath rename(@Nonnull String filename) {
      Path rel = this.path.getParent().resolve(filename);
      Path path = this.filepath.getParent().resolve(filename);
      return new AssetPath(rel, path);
   }

   @Nonnull
   public Path path() {
      return this.path;
   }

   @Nonnull
   public Path filepath() {
      return this.filepath;
   }

   @Nonnull
   public String getFileName() {
      return this.filepath.getFileName().toString();
   }

   public String toString() {
      String var10000 = String.valueOf(this.path);
      return "AssetPath{path=" + var10000 + ", filepath=" + String.valueOf(this.filepath) + "}";
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean equals(Object obj) {
      boolean var10000;
      if (this != obj) {
         label28: {
            if (obj instanceof AssetPath) {
               AssetPath other = (AssetPath)obj;
               if (this.hash == other.hash && FileIO.equals(this.path, other.path)) {
                  break label28;
               }
            }

            var10000 = false;
            return var10000;
         }
      }

      var10000 = true;
      return var10000;
   }

   public static AssetPath fromAbsolute(@Nonnull Path root, @Nonnull Path filepath) {
      assert root.getNameCount() == 0 || FileIO.startsWith(filepath, root);

      Path relPath = FileIO.relativize(filepath, root);
      return new AssetPath(relPath, filepath);
   }

   public static AssetPath fromRelative(@Nonnull Path root, @Nonnull Path assetPath) {
      assert root.getNameCount() == 0 || !FileIO.startsWith(assetPath, root);

      Path filepath = FileIO.append(root, assetPath);
      return new AssetPath(assetPath, filepath);
   }
}
