package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.CombatTargetCollector;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class BuilderCombatTargetCollector extends BuilderBase<ISensorEntityCollector> {
   @Nonnull
   public ISensorEntityCollector build(BuilderSupport builderSupport) {
      return new CombatTargetCollector();
   }

   @Nonnull
   public Class<ISensorEntityCollector> category() {
      return ISensorEntityCollector.class;
   }

   public boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   public String getShortDescription() {
      return "A collector which processes matched friendly and hostile targets and adds them to the NPC's short-term combat memory.";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
