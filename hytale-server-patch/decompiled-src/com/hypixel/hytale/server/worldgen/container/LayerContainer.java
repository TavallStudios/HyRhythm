package com.hypixel.hytale.server.worldgen.container;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateSupplier;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LayerContainer {
   @Nonnull
   protected final BlockFluidEntry filling;
   protected final int fillingEnvironment;
   protected final StaticLayer[] staticLayers;
   protected final DynamicLayer[] dynamicLayers;

   public LayerContainer(int filling, int fillingEnvironment, StaticLayer[] staticLayers, DynamicLayer[] dynamicLayers) {
      this.filling = new BlockFluidEntry(filling, 0, 0);
      this.fillingEnvironment = fillingEnvironment;
      this.staticLayers = staticLayers;
      this.dynamicLayers = dynamicLayers;
   }

   public BlockFluidEntry getFilling() {
      return this.filling;
   }

   public int getFillingEnvironment() {
      return this.fillingEnvironment;
   }

   public StaticLayer[] getStaticLayers() {
      return this.staticLayers;
   }

   public DynamicLayer[] getDynamicLayers() {
      return this.dynamicLayers;
   }

   public BlockFluidEntry getTopBlockAt(int seed, int x, int z) {
      for(DynamicLayer layer : this.dynamicLayers) {
         DynamicLayerEntry entry = (DynamicLayerEntry)layer.getActiveEntry(seed, x, z);
         if (entry != null) {
            return entry.blockArray.getTopBlockAt(seed, (double)x, (double)z);
         }
      }

      return this.filling;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.filling);
      return "LayerContainer{filling=" + var10000 + ", staticLayers=" + Arrays.toString(this.staticLayers) + ", dynamicLayers=" + Arrays.toString(this.dynamicLayers) + "}";
   }

   public static class Layer<T extends LayerEntry> {
      protected final T[] entries;
      protected final ICoordinateCondition mapCondition;
      protected final int environmentId;

      public Layer(T[] entries, ICoordinateCondition mapCondition, int environmentId) {
         this.entries = entries;
         this.mapCondition = mapCondition;
         this.environmentId = environmentId;
      }

      public int getEnvironmentId() {
         return this.environmentId;
      }

      @Nullable
      public T getActiveEntry(int seed, int x, int z) {
         if (!this.mapCondition.eval(seed, x, z)) {
            return null;
         } else {
            for(T entry : this.entries) {
               if (entry.isActive(seed, x, z)) {
                  return entry;
               }
            }

            return null;
         }
      }

      @Nonnull
      public String toString() {
         return "Layer{entries=" + Arrays.toString(this.entries) + "}";
      }
   }

   public static class StaticLayer extends Layer<StaticLayerEntry> {
      public StaticLayer(StaticLayerEntry[] entries, ICoordinateCondition mapCondition, int environmentId) {
         super(entries, mapCondition, environmentId);
      }

      @Nonnull
      public String toString() {
         return "StaticLayer{entries=" + Arrays.toString(this.entries) + "}";
      }
   }

   public static class DynamicLayer extends Layer<DynamicLayerEntry> {
      protected final IDoubleCoordinateSupplier offset;

      public DynamicLayer(DynamicLayerEntry[] entries, ICoordinateCondition mapCondition, int environmentId, IDoubleCoordinateSupplier offset) {
         super(entries, mapCondition, environmentId);
         this.offset = offset;
      }

      public int getOffset(int seed, int x, int z) {
         return MathUtil.floor(this.offset.get(seed, (double)x, (double)z));
      }

      @Nonnull
      public String toString() {
         String var10000 = Arrays.toString(this.entries);
         return "DynamicLayer{entries=" + var10000 + ", offset=" + String.valueOf(this.offset) + "}";
      }
   }

   public abstract static class LayerEntry {
      protected final NoiseBlockArray blockArray;
      protected final ICoordinateCondition mapCondition;

      public LayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition) {
         this.blockArray = blockArray;
         this.mapCondition = mapCondition;
      }

      public boolean isActive(int seed, int x, int z) {
         return this.mapCondition.eval(seed, x, z);
      }

      public NoiseBlockArray getBlockArray() {
         return this.blockArray;
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.blockArray);
         return "LayerEntry{blockArray=" + var10000 + ", mapCondition=" + String.valueOf(this.mapCondition) + "}";
      }
   }

   public static class StaticLayerEntry extends LayerEntry {
      protected final IDoubleCoordinateSupplier min;
      protected final IDoubleCoordinateSupplier max;

      public StaticLayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition, IDoubleCoordinateSupplier min, IDoubleCoordinateSupplier max) {
         super(blockArray, mapCondition);
         this.min = min;
         this.max = max;
      }

      public int getMinInt(int seed, int x, int z) {
         return MathUtil.floor(this.getMinValue(seed, x, z));
      }

      public double getMinValue(int seed, int x, int z) {
         return this.min.get(seed, (double)x, (double)z);
      }

      public int getMaxInt(int seed, int x, int z) {
         return MathUtil.floor(this.getMaxValue(seed, x, z));
      }

      public double getMaxValue(int seed, int x, int z) {
         return this.max.get(seed, (double)x, (double)z);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.blockArray);
         return "StaticLayerEntry{blockArray=" + var10000 + ", mapCondition=" + String.valueOf(this.mapCondition) + ", min=" + String.valueOf(this.min) + ", max=" + String.valueOf(this.max) + "}";
      }
   }

   public static class DynamicLayerEntry extends LayerEntry {
      public DynamicLayerEntry(NoiseBlockArray blockArray, ICoordinateCondition mapCondition) {
         super(blockArray, mapCondition);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.blockArray);
         return "DynamicLayerEntry{blockArray=" + var10000 + ", mapCondition=" + String.valueOf(this.mapCondition) + "}";
      }
   }
}
