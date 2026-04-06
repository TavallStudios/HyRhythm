package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionDelayDespawn;
import javax.annotation.Nonnull;

public class BuilderActionDelayDespawn extends BuilderActionBase {
   protected float time;
   protected boolean shorten;

   @Nonnull
   public String getShortDescription() {
      return "Delay the despawning cycle for some amount of time";
   }

   @Nonnull
   public String getLongDescription() {
      return "Delay the despawning cycle for some amount of time";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public ActionDelayDespawn build(BuilderSupport builderSupport) {
      return new ActionDelayDespawn(this);
   }

   @Nonnull
   public BuilderActionDelayDespawn readConfig(@Nonnull JsonElement data) {
      this.requireFloat(data, "Time", (d) -> this.time = d, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "How long to set the to delay", (String)null);
      this.getBoolean(data, "Shorten", (b) -> this.shorten = b, false, BuilderDescriptorState.Stable, "Set the delay to either the current delay or the given time. Whatever is smaller.", (String)null);
      return this;
   }

   public float getTime() {
      return this.time;
   }

   public boolean getShorten() {
      return this.shorten;
   }
}
