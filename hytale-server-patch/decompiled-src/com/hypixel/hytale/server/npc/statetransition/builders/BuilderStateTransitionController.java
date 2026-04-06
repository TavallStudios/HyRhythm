package com.hypixel.hytale.server.npc.statetransition.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayValidator;
import com.hypixel.hytale.server.npc.statetransition.StateTransitionController;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderStateTransitionController extends BuilderBase<StateTransitionController> {
   protected final BuilderObjectListHelper<BuilderStateTransition.StateTransition> stateTransitionEntries = new BuilderObjectListHelper<BuilderStateTransition.StateTransition>(BuilderStateTransition.StateTransition.class, this);

   @Nonnull
   public String getShortDescription() {
      return "A list of state transitions";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public StateTransitionController build(@Nonnull BuilderSupport builderSupport) {
      return new StateTransitionController(this, builderSupport);
   }

   @Nonnull
   public Class<StateTransitionController> category() {
      return StateTransitionController.class;
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   public Builder<StateTransitionController> readConfig(@Nonnull JsonElement data) {
      this.requireArray(data, this.stateTransitionEntries, (ArrayValidator)null, BuilderDescriptorState.Stable, "A list of state transition entries with lists of actions", (String)null, this.validationHelper);
      return this;
   }

   public boolean validate(String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, @Nonnull ExecutionContext context, Scope globalScope, @Nonnull List<String> errors) {
      return super.validate(configName, validationHelper, context, globalScope, errors) & this.stateTransitionEntries.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   @Nullable
   public List<BuilderStateTransition.StateTransition> getStateTransitionEntries(@Nonnull BuilderSupport support) {
      return this.stateTransitionEntries.build(support);
   }
}
