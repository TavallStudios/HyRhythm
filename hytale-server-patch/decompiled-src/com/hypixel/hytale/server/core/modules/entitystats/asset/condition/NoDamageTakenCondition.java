package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.TimeUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;

public class NoDamageTakenCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<NoDamageTakenCondition> CODEC;
   protected Duration delay;

   protected NoDamageTakenCondition() {
   }

   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      DamageDataComponent damageDataComponent = (DamageDataComponent)componentAccessor.getComponent(ref, DamageDataComponent.getComponentType());

      assert damageDataComponent != null;

      Instant lastDamageTime = damageDataComponent.getLastDamageTime();
      return TimeUtil.compareDifference(lastDamageTime, currentTime, this.delay) >= 0;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.delay);
      return "NoDamageTakenCondition{delay=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(NoDamageTakenCondition.class, NoDamageTakenCondition::new, Condition.BASE_CODEC).append(new KeyedCodec("Delay", Codec.DURATION_SECONDS), (condition, value) -> condition.delay = value, (condition) -> condition.delay).documentation("The delay duration for the no damage taken condition.").addValidator(Validators.nonNull()).add()).build();
   }
}
