package com.hypixel.hytale.server.worldgen.zone;

import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.worldgen.biome.BiomePatternGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Zone(int id, String name, @Nonnull ZoneDiscoveryConfig discoveryConfig, @Nullable CaveGenerator caveGenerator, @Nonnull BiomePatternGenerator biomePatternGenerator, @Nonnull UniquePrefabContainer uniquePrefabContainer) {
   public Zone(final int id, @Nonnull final String name, @Nonnull final ZoneDiscoveryConfig discoveryConfig, @Nullable final CaveGenerator caveGenerator, @Nonnull final BiomePatternGenerator biomePatternGenerator, @Nonnull final UniquePrefabContainer uniquePrefabContainer) {
      this.id = id;
      this.name = name;
      this.discoveryConfig = discoveryConfig;
      this.caveGenerator = caveGenerator;
      this.biomePatternGenerator = biomePatternGenerator;
      this.uniquePrefabContainer = uniquePrefabContainer;
   }

   public int hashCode() {
      return this.id;
   }

   @Nonnull
   public String toString() {
      int var10000 = this.id;
      return "Zone{id=" + var10000 + ", name='" + this.name + "', discoveryConfig=" + String.valueOf(this.discoveryConfig) + ", caveGenerator=" + String.valueOf(this.caveGenerator) + ", biomePatternGenerator=" + String.valueOf(this.biomePatternGenerator) + ", uniquePrefabContainer=" + String.valueOf(this.uniquePrefabContainer) + "}";
   }

   public static record Unique(@Nonnull Zone zone, @Nonnull CompletableFuture<Vector2i> position) {
      public Vector2i getPosition() {
         return (Vector2i)this.position.join();
      }
   }

   public static record UniqueEntry(@Nonnull Zone zone, int color, int[] parent, int radius, int padding) {
      @Nonnull
      public static final UniqueEntry[] EMPTY_ARRAY = new UniqueEntry[0];

      public boolean matchesParent(int color) {
         for(int p : this.parent) {
            if (p == color) {
               return true;
            }
         }

         return false;
      }
   }

   public static record UniqueCandidate(@Nonnull UniqueEntry zone, @Nonnull Vector2i[] positions) {
      public static final UniqueCandidate[] EMPTY_ARRAY = new UniqueCandidate[0];
   }
}
