package com.hypixel.hytale.builtin.npccombatactionevaluator.conditions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.DamageMemory;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;

public class RecentSustainedDamageCondition extends ScaledCurveCondition {
   @Nonnull
   public static final BuilderCodec<RecentSustainedDamageCondition> CODEC;
   @Nonnull
   protected static final ComponentType<EntityStore, DamageMemory> DAMAGE_MEMORY_COMPONENT_TYPE;

   protected double getInput(int selfIndex, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context) {
      DamageMemory damageMemoryComponent = (DamageMemory)archetypeChunk.getComponent(selfIndex, DAMAGE_MEMORY_COMPONENT_TYPE);
      return damageMemoryComponent == null ? 1.7976931348623157E308 : (double)damageMemoryComponent.getRecentDamage();
   }

   public void setupNPC(@Nonnull Holder<EntityStore> holder) {
      holder.ensureComponent(DAMAGE_MEMORY_COMPONENT_TYPE);
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(RecentSustainedDamageCondition.class, RecentSustainedDamageCondition::new, ScaledCurveCondition.ABSTRACT_CODEC).documentation("A scaled curve condition that returns a utility value based on damage taken since the combat action evaluator was last run.")).build();
      DAMAGE_MEMORY_COMPONENT_TYPE = DamageMemory.getComponentType();
   }
}
