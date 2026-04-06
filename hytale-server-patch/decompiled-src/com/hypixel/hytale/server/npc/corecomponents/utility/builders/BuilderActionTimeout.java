package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionWithDelay;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionTimeout;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionTimeout extends BuilderActionWithDelay {
   protected boolean delayAfter;
   protected final BuilderObjectReferenceHelper<Action> action = new BuilderObjectReferenceHelper<Action>(Action.class, this);

   @Nonnull
   public String getShortDescription() {
      return "Delay an action, or insert a delay in a sequence of actions";
   }

   @Nonnull
   public String getLongDescription() {
      return "Delay an action by a time which is randomly picked between a given minimum and maximum value.";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public ActionTimeout build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTimeout(this, builderSupport);
   }

   @Nonnull
   public BuilderActionTimeout readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "DelayAfter", (b) -> this.delayAfter = b, false, BuilderDescriptorState.Stable, "Delay after executing the action", (String)null);
      this.getObject(data, "Action", this.action, BuilderDescriptorState.Stable, "Optional action to delay", (String)null, this.validationHelper);
      return this;
   }

   public boolean validate(String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, @Nonnull ExecutionContext context, Scope globalScope, @Nonnull List<String> errors) {
      return super.validate(configName, validationHelper, context, globalScope, errors) & this.action.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   public boolean isDelayAfter() {
      return this.delayAfter;
   }

   @Nullable
   public Action getAction(@Nonnull BuilderSupport builderSupport) {
      return this.action.build(builderSupport);
   }
}
