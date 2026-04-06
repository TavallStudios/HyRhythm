package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.SensorInflictedDamage;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorInflictedDamage extends BuilderSensorBase {
   protected SensorInflictedDamage.Target target;
   protected boolean friendlyFire;

   @Nonnull
   public SensorInflictedDamage build(BuilderSupport builderSupport) {
      return new SensorInflictedDamage(this);
   }

   @Nonnull
   public String getShortDescription() {
      return "Test if an individual or the flock it belongs to inflicted combat damage";
   }

   @Nonnull
   public String getLongDescription() {
      return "Return true if an individual or the flock it belongs to inflicted combat damage. Target position is entity which received most damage.";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getEnum(data, "Target", (v) -> this.target = v, SensorInflictedDamage.Target.class, SensorInflictedDamage.Target.Self, BuilderDescriptorState.Stable, "Who to check has inflicted damage", (String)null);
      this.getBoolean(data, "FriendlyFire", (v) -> this.friendlyFire = v, false, BuilderDescriptorState.Stable, "Consider friendly fire too", (String)null);
      this.provideFeature(Feature.LiveEntity);
      return this;
   }

   public boolean isFriendlyFire() {
      return this.friendlyFire;
   }

   public SensorInflictedDamage.Target getTarget() {
      return this.target;
   }
}
