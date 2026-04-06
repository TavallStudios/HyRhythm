package com.hypixel.hytale.server.core.prefab;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.asset.AssetModule;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record PrefabEntry(@Nonnull Path path, @Nonnull Path relativePath, @Nullable AssetPack pack, @Nonnull String displayName) {
   public PrefabEntry(@Nonnull Path path, @Nonnull Path relativePath, @Nullable AssetPack pack) {
      this(path, relativePath, pack, buildDisplayName(relativePath, pack));
   }

   public boolean isFromBasePack() {
      return this.pack != null && this.pack.equals(AssetModule.get().getBaseAssetPack());
   }

   public boolean isFromAssetPack() {
      return this.pack != null;
   }

   @Nonnull
   public String getPackName() {
      return this.pack != null ? this.pack.getName() : "Server";
   }

   @Nonnull
   public String getFileName() {
      return this.path.getFileName().toString();
   }

   @Nonnull
   public String getDisplayNameWithPack() {
      if (this.pack != null && !this.isFromBasePack()) {
         String var10000 = this.pack.getName();
         return "[" + var10000 + "] " + this.getFileName();
      } else {
         return this.getFileName();
      }
   }

   @Nonnull
   private static String buildDisplayName(@Nonnull Path relativePath, @Nullable AssetPack pack) {
      String fileName = relativePath.getFileName().toString();
      if (pack != null && !pack.equals(AssetModule.get().getBaseAssetPack())) {
         String var10000 = pack.getName();
         return "[" + var10000 + "] " + fileName;
      } else {
         return fileName;
      }
   }
}
