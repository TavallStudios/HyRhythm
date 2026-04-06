package com.hypixel.hytale.server.npc.path.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class BuilderRelativeWaypointDefinition extends BuilderBase<RelativeWaypointDefinition> {
   protected float rotation;
   protected double distance;

   @Nonnull
   public String getShortDescription() {
      return "A simple path waypoint definition where each waypoint is relative to the previous";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public RelativeWaypointDefinition build(BuilderSupport builderSupport) {
      return new RelativeWaypointDefinition(this.getRotation(), this.getDistance());
   }

   @Nonnull
   public Class<RelativeWaypointDefinition> category() {
      return RelativeWaypointDefinition.class;
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<RelativeWaypointDefinition> readConfig(@Nonnull JsonElement data) {
      this.getFloat(data, "Rotation", (f) -> this.rotation = f * 0.017453292F, 0.0F, DoubleRangeValidator.fromExclToExcl(-360.0, 360.0), BuilderDescriptorState.Stable, "Rotation to turn from previous waypoint", (String)null);
      this.requireDouble(data, "Distance", (d) -> this.distance = d, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "A distance to move from the previous waypoint", (String)null);
      return this;
   }

   public final boolean isEnabled(ExecutionContext context) {
      return true;
   }

   public float getRotation() {
      return this.rotation;
   }

   public double getDistance() {
      return this.distance;
   }
}
