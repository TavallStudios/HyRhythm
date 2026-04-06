package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorBlockType;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorBlockType extends BuilderSensorBase {
   protected final BuilderObjectReferenceHelper<Sensor> sensor = new BuilderObjectReferenceHelper<Sensor>(Sensor.class, this);
   protected final AssetHolder blockSet = new AssetHolder();

   @Nonnull
   public String getShortDescription() {
      return "Checks if the block at the given position matches the provided block set";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nullable
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      Sensor sensor = this.getSensor(builderSupport);
      return sensor == null ? null : new SensorBlockType(this, builderSupport, sensor);
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireObject(data, "Sensor", this.sensor, BuilderDescriptorState.Stable, "Sensor to wrap", (String)null, this.validationHelper);
      this.requireAsset(data, "BlockSet", this.blockSet, BlockSetExistsValidator.required(), BuilderDescriptorState.Stable, "Block set to check against", (String)null);
      return this;
   }

   @Nullable
   public Sensor getSensor(@Nonnull BuilderSupport support) {
      return this.sensor.build(support);
   }

   public int getBlockSet(@Nonnull BuilderSupport support) {
      String key = this.blockSet.get(support.getExecutionContext());
      int index = BlockSet.getAssetMap().getIndex(key);
      if (index == -2147483648) {
         throw new IllegalArgumentException("Unknown key! " + key);
      } else {
         return index;
      }
   }
}
