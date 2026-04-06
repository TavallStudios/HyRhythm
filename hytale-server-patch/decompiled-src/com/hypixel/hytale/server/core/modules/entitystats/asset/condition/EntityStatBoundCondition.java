package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public abstract class EntityStatBoundCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<EntityStatBoundCondition> CODEC;
   protected String unknownStat;
   protected int stat = -2147483648;

   protected EntityStatBoundCondition() {
   }

   public EntityStatBoundCondition(boolean inverse, int stat) {
      super(inverse);
      this.stat = stat;
   }

   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      if (this.stat == -2147483648) {
         this.stat = EntityStatType.getAssetMap().getIndex(this.unknownStat);
      }

      EntityStatMap entityStatMapComponent = (EntityStatMap)componentAccessor.getComponent(ref, EntityStatsModule.get().getEntityStatMapComponentType());
      if (entityStatMapComponent == null) {
         return false;
      } else {
         EntityStatValue statValue = entityStatMapComponent.get(this.stat);
         return statValue == null ? false : this.eval0(ref, currentTime, statValue);
      }
   }

   public abstract boolean eval0(@Nonnull Ref<EntityStore> var1, @Nonnull Instant var2, @Nonnull EntityStatValue var3);

   @Nonnull
   public String toString() {
      String var10000 = this.unknownStat;
      return "EntityStatBoundCondition{unknownStat='" + var10000 + "', stat=" + this.stat + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.abstractBuilder(EntityStatBoundCondition.class, Condition.BASE_CODEC).append(new KeyedCodec("Stat", Codec.STRING), (condition, value) -> condition.unknownStat = value, (condition) -> condition.unknownStat).documentation("The stat to evaluate the condition against.").addValidator(Validators.nonNull()).addValidatorLate(() -> EntityStatType.VALIDATOR_CACHE.getValidator().late()).add()).build();
   }
}
