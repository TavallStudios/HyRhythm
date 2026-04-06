package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class EnvironmentContainer {
   protected final DefaultEnvironmentContainerEntry defaultEntry;
   protected final EnvironmentContainerEntry[] entries;

   public EnvironmentContainer(DefaultEnvironmentContainerEntry defaultEntry, EnvironmentContainerEntry[] entries) {
      this.defaultEntry = defaultEntry;
      this.entries = entries;
   }

   public int getEnvironmentAt(int seed, int x, int z) {
      for(EnvironmentContainerEntry entry : this.entries) {
         if (entry.shouldGenerate(seed, x, z)) {
            return entry.getEnvironmentAt(seed, x, z);
         }
      }

      return this.defaultEntry.getEnvironmentAt(seed, x, z);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.defaultEntry);
      return "EnvironmentContainer{defaultEntry=" + var10000 + ", entries=" + Arrays.toString(this.entries) + "}";
   }

   public static class DefaultEnvironmentContainerEntry extends EnvironmentContainerEntry {
      public DefaultEnvironmentContainerEntry(IWeightedMap<Integer> environmentMapping, NoiseProperty valueNoise) {
         super(environmentMapping, valueNoise, DefaultCoordinateCondition.DEFAULT_TRUE);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.environmentMapping);
         return "DefaultEnvironmentContainerEntry{environmentMapping=" + var10000 + ", valueNoise=" + String.valueOf(this.valueNoise) + ", mapCondition=" + String.valueOf(this.mapCondition) + "}";
      }
   }

   public static class EnvironmentContainerEntry {
      public static final EnvironmentContainerEntry[] EMPTY_ARRAY = new EnvironmentContainerEntry[0];
      protected final IWeightedMap<Integer> environmentMapping;
      protected final NoiseProperty valueNoise;
      protected final ICoordinateCondition mapCondition;

      public EnvironmentContainerEntry(IWeightedMap<Integer> environmentMapping, NoiseProperty valueNoise, ICoordinateCondition mapCondition) {
         this.environmentMapping = environmentMapping;
         this.valueNoise = valueNoise;
         this.mapCondition = mapCondition;
      }

      public boolean shouldGenerate(int seed, int x, int z) {
         return this.mapCondition.eval(seed, x, z);
      }

      public int getEnvironmentAt(int seed, int x, int z) {
         return (Integer)this.environmentMapping.get(seed, x, z, (iSeed, ix, iz, entry) -> entry.valueNoise.get(iSeed, (double)ix, (double)iz), this);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.environmentMapping);
         return "EnvironmentContainerEntry{environmentMapping=" + var10000 + ", valueNoise=" + String.valueOf(this.valueNoise) + ", mapCondition=" + String.valueOf(this.mapCondition) + "}";
      }
   }
}
