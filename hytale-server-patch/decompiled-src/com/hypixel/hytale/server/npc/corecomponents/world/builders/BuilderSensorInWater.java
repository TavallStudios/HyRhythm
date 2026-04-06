package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorInWater;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorInWater extends BuilderSensorBase {
   @Nonnull
   public String getShortDescription() {
      return "Check if NPC is currently in water";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(BuilderSupport builderSupport) {
      return new SensorInWater(this);
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
