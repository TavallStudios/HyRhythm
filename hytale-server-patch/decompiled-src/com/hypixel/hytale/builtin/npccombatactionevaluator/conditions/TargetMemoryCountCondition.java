package com.hypixel.hytale.builtin.npccombatactionevaluator.conditions;

import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.ScaledCurveCondition;
import javax.annotation.Nonnull;

public class TargetMemoryCountCondition extends ScaledCurveCondition {
   @Nonnull
   public static final EnumCodec<TargetType> TARGET_TYPE_CODEC;
   @Nonnull
   public static final BuilderCodec<TargetMemoryCountCondition> CODEC;
   @Nonnull
   protected static final ComponentType<EntityStore, TargetMemory> TARGET_MEMORY_COMPONENT_TYPE;
   protected TargetType targetType;

   public TargetMemoryCountCondition() {
      this.targetType = TargetMemoryCountCondition.TargetType.Hostile;
   }

   protected double getInput(int selfIndex, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, Ref<EntityStore> target, CommandBuffer<EntityStore> commandBuffer, EvaluationContext context) {
      TargetMemory targetMemoryComponent = (TargetMemory)archetypeChunk.getComponent(selfIndex, TARGET_MEMORY_COMPONENT_TYPE);

      assert targetMemoryComponent != null;

      double var10000;
      switch (this.targetType.ordinal()) {
         case 0 -> var10000 = (double)targetMemoryComponent.getKnownHostiles().size();
         case 1 -> var10000 = (double)targetMemoryComponent.getKnownFriendlies().size();
         case 2 -> var10000 = (double)(targetMemoryComponent.getKnownFriendlies().size() + targetMemoryComponent.getKnownHostiles().size());
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   static {
      TARGET_TYPE_CODEC = (new EnumCodec<TargetType>(TargetType.class)).documentKey(TargetMemoryCountCondition.TargetType.All, "All known targets.").documentKey(TargetMemoryCountCondition.TargetType.Friendly, "Known friendly targets.").documentKey(TargetMemoryCountCondition.TargetType.Hostile, "Known hostile targets.");
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(TargetMemoryCountCondition.class, TargetMemoryCountCondition::new, ScaledCurveCondition.ABSTRACT_CODEC).documentation("A scaled curve condition that returns a utility value based on the number of known targets in the memory.")).appendInherited(new KeyedCodec("TargetType", TARGET_TYPE_CODEC), (condition, e) -> condition.targetType = e, (condition) -> condition.targetType, (condition, parent) -> condition.targetType = parent.targetType).documentation("The type of targets to count.").add()).build();
      TARGET_MEMORY_COMPONENT_TYPE = TargetMemory.getComponentType();
   }

   private static enum TargetType {
      Hostile,
      Friendly,
      All;
   }
}
