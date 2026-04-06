package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterLineOfSight;
import javax.annotation.Nonnull;

public class BuilderEntityFilterLineOfSight extends BuilderEntityFilterBase {
   @Nonnull
   public String getShortDescription() {
      return "Matches if there is line of sight to the target";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(BuilderSupport builderSupport) {
      return new EntityFilterLineOfSight();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<IEntityFilter> readConfig(JsonElement data) {
      this.requireContext(InstructionType.Any, ComponentContext.NotSelfEntitySensor);
      return this;
   }
}
