package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterWithToggle;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterNot;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderEntityFilterNot extends BuilderEntityFilterWithToggle {
   protected final BuilderObjectReferenceHelper<IEntityFilter> filter = new BuilderObjectReferenceHelper<IEntityFilter>(IEntityFilter.class, this);

   @Nullable
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      IEntityFilter filter = this.getFilter(builderSupport);
      return filter == null ? null : new EntityFilterNot(filter);
   }

   @Nonnull
   public String getShortDescription() {
      return "Invert filter test";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireObject(data, "Filter", this.filter, BuilderDescriptorState.Stable, "Filter to test", (String)null, this.validationHelper);
      return this;
   }

   public boolean validate(String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, @Nonnull ExecutionContext context, Scope globalScope, @Nonnull List<String> errors) {
      return super.validate(configName, validationHelper, context, globalScope, errors) & this.filter.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nullable
   public IEntityFilter getFilter(@Nonnull BuilderSupport support) {
      return this.filter.build(support);
   }
}
