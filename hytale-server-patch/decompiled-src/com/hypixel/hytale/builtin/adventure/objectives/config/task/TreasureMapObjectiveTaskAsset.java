package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders.WorldLocationProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TreasureMapObjectiveTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<TreasureMapObjectiveTaskAsset> CODEC;
   protected ChestConfig[] chestConfigs;

   public TreasureMapObjectiveTaskAsset(String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, ChestConfig[] chestConfigs) {
      super(descriptionId, taskConditions, mapMarkers);
      this.chestConfigs = chestConfigs;
   }

   protected TreasureMapObjectiveTaskAsset() {
   }

   @Nonnull
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER;
   }

   public ChestConfig[] getChestConfigs() {
      return this.chestConfigs;
   }

   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (task instanceof TreasureMapObjectiveTaskAsset treasureMapObjectiveTaskAsset) {
         return Arrays.equals(treasureMapObjectiveTaskAsset.chestConfigs, this.chestConfigs);
      } else {
         return false;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.chestConfigs);
      return "TreasureMapObjectiveTaskAsset{chestConfigs=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(TreasureMapObjectiveTaskAsset.class, TreasureMapObjectiveTaskAsset::new, BASE_CODEC).append(new KeyedCodec("Chests", new ArrayCodec(TreasureMapObjectiveTaskAsset.ChestConfig.CODEC, (x$0) -> new ChestConfig[x$0])), (treasureMapObjectiveTaskAsset, chestConfigs) -> treasureMapObjectiveTaskAsset.chestConfigs = chestConfigs, (treasureMapObjectiveTaskAsset) -> treasureMapObjectiveTaskAsset.chestConfigs).addValidator(Validators.nonEmptyArray()).add()).build();
   }

   public static class ChestConfig {
      @Nonnull
      public static final BuilderCodec<ChestConfig> CODEC;
      protected float minRadius = 10.0F;
      protected float maxRadius = 20.0F;
      protected String droplistId;
      protected WorldLocationProvider worldLocationProvider;
      protected String chestBlockTypeKey;

      public float getMinRadius() {
         return this.minRadius;
      }

      public float getMaxRadius() {
         return this.maxRadius;
      }

      public String getDroplistId() {
         return this.droplistId;
      }

      public WorldLocationProvider getWorldLocationProvider() {
         return this.worldLocationProvider;
      }

      public String getChestBlockTypeKey() {
         return this.chestBlockTypeKey;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ChestConfig that = (ChestConfig)o;
            if (Float.compare(that.minRadius, this.minRadius) != 0) {
               return false;
            } else if (Float.compare(that.maxRadius, this.maxRadius) != 0) {
               return false;
            } else {
               if (this.droplistId != null) {
                  if (!this.droplistId.equals(that.droplistId)) {
                     return false;
                  }
               } else if (that.droplistId != null) {
                  return false;
               }

               if (this.worldLocationProvider != null) {
                  if (!this.worldLocationProvider.equals(that.worldLocationProvider)) {
                     return false;
                  }
               } else if (that.worldLocationProvider != null) {
                  return false;
               }

               return this.chestBlockTypeKey != null ? this.chestBlockTypeKey.equals(that.chestBlockTypeKey) : that.chestBlockTypeKey == null;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.minRadius != 0.0F ? Float.floatToIntBits(this.minRadius) : 0;
         result = 31 * result + (this.maxRadius != 0.0F ? Float.floatToIntBits(this.maxRadius) : 0);
         result = 31 * result + (this.droplistId != null ? this.droplistId.hashCode() : 0);
         result = 31 * result + (this.worldLocationProvider != null ? this.worldLocationProvider.hashCode() : 0);
         result = 31 * result + (this.chestBlockTypeKey != null ? this.chestBlockTypeKey.hashCode() : 0);
         return result;
      }

      @Nonnull
      public String toString() {
         float var10000 = this.minRadius;
         return "ChestConfig{minRadius=" + var10000 + ", maxRadius=" + this.maxRadius + ", droplistId='" + this.droplistId + "', worldLocationCondition=" + String.valueOf(this.worldLocationProvider) + ", chestBlockTypeKey=" + this.chestBlockTypeKey + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ChestConfig.class, ChestConfig::new).append(new KeyedCodec("MinRadius", Codec.FLOAT), (chestConfig, aFloat) -> chestConfig.minRadius = aFloat, (chestConfig) -> chestConfig.minRadius).addValidator(Validators.greaterThan(0.0F)).add()).append(new KeyedCodec("MaxRadius", Codec.FLOAT), (chestConfig, aFloat) -> chestConfig.maxRadius = aFloat, (chestConfig) -> chestConfig.maxRadius).addValidator(Validators.greaterThan(1.0F)).add()).append(new KeyedCodec("DropList", new ContainedAssetCodec(ItemDropList.class, ItemDropList.CODEC)), (chestConfig, s) -> chestConfig.droplistId = s, (chestConfig) -> chestConfig.droplistId).addValidator(Validators.nonNull()).addValidator(ItemDropList.VALIDATOR_CACHE.getValidator()).add()).append(new KeyedCodec("WorldLocationCondition", WorldLocationProvider.CODEC), (chestConfig, worldLocationCondition) -> chestConfig.worldLocationProvider = worldLocationCondition, (chestConfig) -> chestConfig.worldLocationProvider).add()).append(new KeyedCodec("ChestBlockTypeKey", Codec.STRING), (chestConfig, blockTypeKey) -> chestConfig.chestBlockTypeKey = blockTypeKey, (chestConfig) -> chestConfig.chestBlockTypeKey).addValidator(Validators.nonNull()).addValidator(BlockType.VALIDATOR_CACHE.getValidator()).add()).afterDecode((chestConfig) -> {
            if (chestConfig.minRadius >= chestConfig.maxRadius) {
               throw new IllegalArgumentException("ChestConfig.MinRadius (" + chestConfig.minRadius + ") needs to be greater than ChestConfig.MaxRadius (" + chestConfig.maxRadius + ")");
            }
         })).build();
      }
   }
}
