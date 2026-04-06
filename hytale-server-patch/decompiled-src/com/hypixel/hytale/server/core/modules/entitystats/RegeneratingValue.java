package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.modules.entitystats.asset.modifier.RegeneratingModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class RegeneratingValue {
   @Nonnull
   private final EntityStatType.Regenerating regenerating;
   private float remainingUntilRegen;

   public RegeneratingValue(@Nonnull EntityStatType.Regenerating regenerating) {
      this.regenerating = regenerating;
   }

   public boolean shouldRegenerate(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, float dt, @Nonnull EntityStatType.Regenerating regenerating) {
      this.remainingUntilRegen -= dt;
      if (this.remainingUntilRegen < 0.0F) {
         this.remainingUntilRegen += regenerating.getInterval();
         return Condition.allConditionsMet(store, ref, currentTime, regenerating);
      } else {
         return false;
      }
   }

   public float regenerate(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, float dt, @Nonnull EntityStatValue value, float currentAmount) {
      if (!this.shouldRegenerate(store, ref, currentTime, dt, this.regenerating)) {
         return 0.0F;
      } else {
         float var10000;
         switch (this.regenerating.getRegenType()) {
            case ADDITIVE -> var10000 = this.regenerating.getAmount();
            case PERCENTAGE -> var10000 = this.regenerating.getAmount() * (value.getMax() - value.getMin());
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         float toAdd = var10000;
         if (this.regenerating.getModifiers() != null) {
            for(RegeneratingModifier modifier : this.regenerating.getModifiers()) {
               toAdd *= modifier.getModifier(store, ref, currentTime);
            }
         }

         return this.regenerating.clampAmount(toAdd, currentAmount, value);
      }
   }

   public EntityStatType.Regenerating getRegenerating() {
      return this.regenerating;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.regenerating);
      return "RegeneratingValue{regenerating=" + var10000 + ", remainingUntilRegen=" + this.remainingUntilRegen + "}";
   }
}
