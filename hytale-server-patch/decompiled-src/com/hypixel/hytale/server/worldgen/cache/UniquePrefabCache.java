package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.util.cache.SizedTimeoutCache;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabCache {
   @Nonnull
   protected final SizedTimeoutCache<Integer, UniquePrefabContainer.UniquePrefabEntry[]> cache;

   public UniquePrefabCache(@Nonnull UniquePrefabFunction function, int maxSize, long expireAfterSeconds) {
      TimeUnit var10004 = TimeUnit.SECONDS;
      Objects.requireNonNull(function);
      this.cache = new SizedTimeoutCache<Integer, UniquePrefabContainer.UniquePrefabEntry[]>(expireAfterSeconds, var10004, maxSize, function::get, (BiConsumer)null);
   }

   @Nullable
   public UniquePrefabContainer.UniquePrefabEntry[] get(int seed) {
      try {
         return this.cache.get(seed);
      } catch (Exception e) {
         throw new Error("Failed to receive UniquePrefabEntry for " + seed, e);
      }
   }

   @FunctionalInterface
   public interface UniquePrefabFunction {
      UniquePrefabContainer.UniquePrefabEntry[] get(int var1);
   }
}
