package com.hypixel.hytale.server.npc.decisionmaker.core.conditions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.EvaluationContext;
import com.hypixel.hytale.server.npc.decisionmaker.core.conditions.base.CurveCondition;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetStatPercentageCondition extends CurveCondition {
   public static final BuilderCodec<TargetStatPercentageCondition> CODEC;
   protected String stat;
   protected int statIndex;

   protected TargetStatPercentageCondition() {
   }

   protected double getNormalisedInput(int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, @Nullable Ref<EntityStore> target, @Nonnull CommandBuffer<EntityStore> commandBuffer, EvaluationContext context) {
      if (target != null && target.isValid()) {
         EntityStatMap entityStatMapComponent = (EntityStatMap)commandBuffer.getComponent(target, EntityStatsModule.get().getEntityStatMapComponentType());

         assert entityStatMapComponent != null;

         return (double)((EntityStatValue)Objects.requireNonNull(entityStatMapComponent.get(this.statIndex))).asPercentage();
      } else {
         return 1.7976931348623157E308;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = this.stat;
      return "TargetStatPercentageCondition{stat='" + var10000 + "', statIndex=" + this.statIndex + "}" + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(TargetStatPercentageCondition.class, TargetStatPercentageCondition::new, CurveCondition.ABSTRACT_CODEC).documentation("A curve condition that returns a utility value based on the percentage value of one of the target's stats.")).appendInherited(new KeyedCodec("Stat", Codec.STRING), (condition, s) -> condition.stat = s, (condition) -> condition.stat, (condition, parent) -> condition.stat = parent.stat).addValidator(Validators.nonNull()).addValidator(EntityStatType.VALIDATOR_CACHE.getValidator()).documentation("The stat to check.").add()).afterDecode((condition) -> condition.statIndex = EntityStatType.getAssetMap().getIndex(condition.stat))).build();
   }
}
