package com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.EnumArrayNoDuplicatesValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorEntityPrioritiserBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.prioritisers.SensorEntityPrioritiserAttitude;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderSensorEntityPrioritiserAttitude extends BuilderSensorEntityPrioritiserBase {
   protected final EnumArrayHolder<Attitude> prioritisedAttitudes = new EnumArrayHolder<Attitude>();

   public BuilderSensorEntityPrioritiserAttitude() {
      super(Set.of("Attitude"));
   }

   @Nonnull
   public String getShortDescription() {
      return "Prioritises return entities by attitude";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public SensorEntityPrioritiserAttitude build(@Nonnull BuilderSupport builderSupport) {
      return new SensorEntityPrioritiserAttitude(this, builderSupport);
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderSensorEntityPrioritiserAttitude readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.requireEnumArray(data, "AttitudesByPriority", this.prioritisedAttitudes, Attitude.class, EnumArrayNoDuplicatesValidator.get(), BuilderDescriptorState.Stable, "A prioritised list of attitudes", (String)null);
      return this;
   }

   public Attitude[] getPrioritisedAttitudes(@Nonnull BuilderSupport support) {
      return (Attitude[])this.prioritisedAttitudes.get(support.getExecutionContext());
   }
}
