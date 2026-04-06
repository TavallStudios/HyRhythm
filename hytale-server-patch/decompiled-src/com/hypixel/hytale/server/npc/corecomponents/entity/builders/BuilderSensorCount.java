package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.TagSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorCount;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class BuilderSensorCount extends BuilderSensorBase {
   protected final NumberArrayHolder count = new NumberArrayHolder();
   protected final NumberArrayHolder range = new NumberArrayHolder();
   protected String[] includeGroups;
   protected String[] excludeGroups;

   @Nonnull
   public String getShortDescription() {
      return "Check if there is a certain number of NPCs or players within a specific range";
   }

   @Nonnull
   public String getLongDescription() {
      return "Check if there is a certain number of NPCs or players within a specific range";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public SensorCount build(@Nonnull BuilderSupport builderSupport) {
      return new SensorCount(this, builderSupport);
   }

   @Nonnull
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireIntRange(data, "Count", this.count, IntSequenceValidator.betweenWeaklyMonotonic(0, 2147483647), BuilderDescriptorState.Stable, "Specifies the allowed number of entities (inclusive)", (String)null);
      this.requireDoubleRange(data, "Range", this.range, DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 1.7976931348623157E308), BuilderDescriptorState.Stable, "Range to find entities in (inclusive)", (String)null);
      this.getAssetArray(data, "IncludeGroups", (t) -> this.includeGroups = t, (Function)null, (String[])null, TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty), BuilderDescriptorState.Stable, "Match for NPCs in these groups", (String)null);
      this.getAssetArray(data, "ExcludeGroups", (t) -> this.excludeGroups = t, (Function)null, (String[])null, TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty), BuilderDescriptorState.Stable, "Never match NPCs in these groups", (String)null);
      return this;
   }

   public int[] getCount(@Nonnull BuilderSupport builderSupport) {
      return this.count.getIntArray(builderSupport.getExecutionContext());
   }

   public double[] getRange(@Nonnull BuilderSupport builderSupport) {
      return this.range.get(builderSupport.getExecutionContext());
   }

   public int[] getIncludeGroups() {
      return WorldSupport.createTagSetIndexArray(this.includeGroups);
   }

   public int[] getExcludeGroups() {
      return WorldSupport.createTagSetIndexArray(this.excludeGroups);
   }
}
