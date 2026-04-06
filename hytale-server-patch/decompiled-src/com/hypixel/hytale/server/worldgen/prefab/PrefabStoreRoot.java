package com.hypixel.hytale.server.worldgen.prefab;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public enum PrefabStoreRoot {
   ASSETS,
   WORLD_GEN;

   public static final PrefabStoreRoot DEFAULT = WORLD_GEN;

   @Nonnull
   public static Path resolvePrefabStore(@Nonnull PrefabStoreRoot store, @Nonnull Path dataFolder) {
      Path var10000;
      switch (store.ordinal()) {
         case 0 -> var10000 = PrefabStore.get().getAssetPrefabsPath();
         case 1 -> var10000 = dataFolder.resolve("Prefabs");
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }
}
