package com.hypixel.hytale.server.worldgen.zone;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ZonePatternGeneratorCache {
   protected final Function<Integer, ZonePatternGenerator> compute;
   protected final Map<Integer, ZonePatternGenerator> cache = new ConcurrentHashMap();

   public ZonePatternGeneratorCache(ZonePatternProvider provider) {
      Objects.requireNonNull(provider);
      this.compute = provider::createGenerator;
   }

   public ZonePatternGenerator get(int seed) {
      try {
         return (ZonePatternGenerator)this.cache.computeIfAbsent(seed, this.compute);
      } catch (Exception e) {
         throw new Error("Failed to receive UniquePrefabEntry for " + seed, e);
      }
   }
}
