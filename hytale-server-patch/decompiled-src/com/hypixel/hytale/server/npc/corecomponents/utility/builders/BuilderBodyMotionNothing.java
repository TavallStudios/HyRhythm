package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.BodyMotionNothing;
import javax.annotation.Nonnull;

public class BuilderBodyMotionNothing extends BuilderBodyMotionBase {
   @Nonnull
   public BodyMotionNothing build(BuilderSupport builderSupport) {
      return new BodyMotionNothing(this);
   }

   @Nonnull
   public String getShortDescription() {
      return "Do nothing";
   }

   @Nonnull
   public String getLongDescription() {
      return "Do nothing";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
