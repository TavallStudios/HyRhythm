package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.List;
import javax.annotation.Nonnull;

public class TintContainer {
   private final DefaultTintContainerEntry defaultEntry;
   private final List<TintContainerEntry> entries;

   public TintContainer(DefaultTintContainerEntry defaultEntry, List<TintContainerEntry> entries) {
      this.defaultEntry = defaultEntry;
      this.entries = entries;
   }

   public int getTintColorAt(int seed, int x, int z) {
      for(int i = 0; i < this.entries.size(); ++i) {
         if (((TintContainerEntry)this.entries.get(i)).shouldGenerate(seed, x, z)) {
            return ((TintContainerEntry)this.entries.get(i)).getTintColorAt(seed, x, z);
         }
      }

      return this.defaultEntry.getTintColorAt(seed, x, z);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.defaultEntry);
      return "TintContainer{defaultEntry=" + var10000 + ", entries=" + String.valueOf(this.entries) + "}";
   }

   public static class DefaultTintContainerEntry extends TintContainerEntry {
      public DefaultTintContainerEntry(IWeightedMap<Integer> colorMapping, NoiseProperty valueNoise) {
         super(colorMapping, valueNoise, DefaultCoordinateCondition.DEFAULT_TRUE);
      }

      @Nonnull
      public String toString() {
         return "DefaultTintContainerEntry{}";
      }
   }

   public static class TintContainerEntry {
      private final IWeightedMap<Integer> colorMapping;
      private final NoiseProperty valueNoise;
      private final ICoordinateCondition mapCondition;

      public TintContainerEntry(IWeightedMap<Integer> colorMapping, NoiseProperty valueNoise, ICoordinateCondition mapCondition) {
         this.colorMapping = colorMapping;
         this.valueNoise = valueNoise;
         this.mapCondition = mapCondition;
      }

      public boolean shouldGenerate(int seed, int x, int z) {
         return this.mapCondition.eval(seed, x, z);
      }

      public int getTintColorAt(int seed, int x, int z) {
         return (Integer)this.colorMapping.get(seed, x, z, (iSeed, ix, iz, entry) -> entry.valueNoise.get(iSeed, (double)ix, (double)iz), this);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.colorMapping);
         return "TintContainerEntry{colorMapping=" + var10000 + ", valueNoise=" + String.valueOf(this.valueNoise) + ", mapCondition=" + String.valueOf(this.mapCondition) + "}";
      }
   }
}
