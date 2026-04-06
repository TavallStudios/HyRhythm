package com.hypixel.hytale.server.core.modules.entitystats.asset.modifier;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.asset.condition.Condition;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class RegeneratingModifier {
   public static final BuilderCodec<RegeneratingModifier> CODEC;
   protected Condition[] conditions;
   protected float amount;

   protected RegeneratingModifier() {
   }

   public RegeneratingModifier(Condition[] conditions, float amount) {
      this.conditions = conditions;
      this.amount = amount;
   }

   public float getModifier(ComponentAccessor<EntityStore> store, Ref<EntityStore> ref, Instant currentTime) {
      return Condition.allConditionsMet(store, ref, currentTime, this.conditions) ? this.amount : 1.0F;
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.conditions);
      return "RegeneratingModifier{conditions=" + var10000 + ", amount=" + this.amount + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RegeneratingModifier.class, RegeneratingModifier::new).append(new KeyedCodec("Conditions", new ArrayCodec(Condition.CODEC, (x$0) -> new Condition[x$0])), (condition, value) -> condition.conditions = value, (condition) -> condition.conditions).add()).append(new KeyedCodec("Amount", Codec.FLOAT), (condition, value) -> condition.amount = value, (condition) -> condition.amount).add()).build();
   }
}
