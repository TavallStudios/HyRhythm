package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.BodyMotionFlock;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public class BuilderBodyMotionFlock extends BuilderBodyMotionBase {
   @Nonnull
   public BodyMotionFlock build(BuilderSupport builderSupport) {
      return new BodyMotionFlock(this);
   }

   @Nonnull
   public String getShortDescription() {
      return "Flocking - WIP";
   }

   @Nonnull
   public String getLongDescription() {
      return "Flocking - WIP";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public Builder<BodyMotion> readConfig(JsonElement data) {
      return this;
   }
}
