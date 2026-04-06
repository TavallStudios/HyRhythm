package com.hypixel.hytale.builtin.mounts.npc.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.mounts.npc.ActionMount;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionMount extends BuilderActionBase {
   protected final FloatHolder anchorX = new FloatHolder();
   protected final FloatHolder anchorY = new FloatHolder();
   protected final FloatHolder anchorZ = new FloatHolder();
   protected final StringHolder movementConfig = new StringHolder();

   @Nonnull
   public String getShortDescription() {
      return "Enable the player to mount the entity";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public float getAnchorX(@Nonnull BuilderSupport support) {
      return this.anchorX.get(support.getExecutionContext());
   }

   public float getAnchorY(@Nonnull BuilderSupport support) {
      return this.anchorY.get(support.getExecutionContext());
   }

   public float getAnchorZ(@Nonnull BuilderSupport support) {
      return this.anchorZ.get(support.getExecutionContext());
   }

   public String getMovementConfig(@Nonnull BuilderSupport support) {
      return this.movementConfig.get(support.getExecutionContext());
   }

   @Nonnull
   public ActionMount build(@Nonnull BuilderSupport builderSupport) {
      return new ActionMount(this, builderSupport);
   }

   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.requireFloat(data, "AnchorX", this.anchorX, (DoubleValidator)null, BuilderDescriptorState.Stable, "The X anchor pos", (String)null);
      this.requireFloat(data, "AnchorY", this.anchorY, (DoubleValidator)null, BuilderDescriptorState.Stable, "The Y anchor pos", (String)null);
      this.requireFloat(data, "AnchorZ", this.anchorZ, (DoubleValidator)null, BuilderDescriptorState.Stable, "The Z anchor pos", (String)null);
      this.requireString(data, "MovementConfig", this.movementConfig, (StringValidator)null, BuilderDescriptorState.Stable, "The MovementConfig to use for this mount", (String)null);
      return super.readConfig(data);
   }
}
