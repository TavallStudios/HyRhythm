package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class LogicCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<LogicCondition> CODEC;
   protected Operator operator;
   protected Condition[] conditions;

   protected LogicCondition() {
   }

   public LogicCondition(boolean inverse, @Nonnull Operator operator, @Nonnull Condition[] conditions) {
      super(inverse);
      this.operator = operator;
      this.conditions = conditions;
   }

   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      return this.operator.eval(componentAccessor, ref, currentTime, this.conditions);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.operator);
      return "LogicCondition{operator=" + var10000 + ", conditions=" + Arrays.toString(this.conditions) + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(LogicCondition.class, LogicCondition::new, Condition.BASE_CODEC).append(new KeyedCodec("Operator", new EnumCodec(Operator.class)), (condition, value) -> condition.operator = value, (condition) -> condition.operator).documentation("The logical operator to combine the conditions.").addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Conditions", new ArrayCodec(Condition.CODEC, (x$0) -> new Condition[x$0])), (condition, value) -> condition.conditions = value, (condition) -> condition.conditions).documentation("The array of conditions to be evaluated.").addValidator(Validators.nonNull()).add()).build();
   }

   public static enum Operator {
      AND {
         public boolean eval(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, @Nonnull Condition[] conditions) {
            for(Condition condition : conditions) {
               if (!condition.eval(store, ref, currentTime)) {
                  return false;
               }
            }

            return true;
         }
      },
      OR {
         public boolean eval(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime, @Nonnull Condition[] conditions) {
            for(Condition condition : conditions) {
               if (condition.eval(store, ref, currentTime)) {
                  return true;
               }
            }

            return false;
         }
      };

      public abstract boolean eval(@Nonnull ComponentAccessor<EntityStore> var1, @Nonnull Ref<EntityStore> var2, @Nonnull Instant var3, @Nonnull Condition[] var4);
   }
}
