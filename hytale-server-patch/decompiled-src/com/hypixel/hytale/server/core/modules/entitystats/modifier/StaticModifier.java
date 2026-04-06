package com.hypixel.hytale.server.core.modules.entitystats.modifier;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaticModifier extends Modifier {
   public static final BuilderCodec<StaticModifier> CODEC;
   public static final BuilderCodec<StaticModifier> ENTITY_CODEC;
   protected CalculationType calculationType;
   protected float amount;

   protected StaticModifier() {
   }

   public StaticModifier(Modifier.ModifierTarget target, CalculationType calculationType, float amount) {
      super(target);
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public CalculationType getCalculationType() {
      return this.calculationType;
   }

   public float getAmount() {
      return this.amount;
   }

   public float apply(float statValue) {
      return this.calculationType.compute(statValue, this.amount);
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Modifier toPacket() {
      com.hypixel.hytale.protocol.Modifier packet = super.toPacket();
      com.hypixel.hytale.protocol.CalculationType var10001;
      switch (this.calculationType.ordinal()) {
         case 0 -> var10001 = com.hypixel.hytale.protocol.CalculationType.Additive;
         case 1 -> var10001 = com.hypixel.hytale.protocol.CalculationType.Multiplicative;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      packet.calculationType = var10001;
      packet.amount = this.amount;
      return packet;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            StaticModifier that = (StaticModifier)o;
            if (Float.compare(that.amount, this.amount) != 0) {
               return false;
            } else {
               return this.calculationType == that.calculationType;
            }
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.calculationType != null ? this.calculationType.hashCode() : 0);
      result = 31 * result + (this.amount != 0.0F ? Float.floatToIntBits(this.amount) : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.calculationType);
      return "StaticModifier{calculationType=" + var10000 + ", amount=" + this.amount + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StaticModifier.class, StaticModifier::new, BASE_CODEC).append(new KeyedCodec("CalculationType", new EnumCodec(CalculationType.class)), (modifier, value) -> modifier.calculationType = value, (modifier) -> modifier.calculationType).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Amount", Codec.FLOAT), (modifier, value) -> modifier.amount = value, (modifier) -> modifier.amount).add()).build();
      ENTITY_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StaticModifier.class, StaticModifier::new).append(new KeyedCodec("CalculationType", new EnumCodec(CalculationType.class, EnumCodec.EnumStyle.LEGACY)), (modifier, value) -> modifier.calculationType = value, (modifier) -> modifier.calculationType).setVersionRange(0, 3).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("CalculationType", new EnumCodec(CalculationType.class)), (modifier, value) -> modifier.calculationType = value, (modifier) -> modifier.calculationType).setVersionRange(4, 5).addValidator(Validators.nonNull()).add()).addField(new KeyedCodec("Amount", Codec.FLOAT), (modifier, value) -> modifier.amount = value, (modifier) -> modifier.amount)).build();
   }

   public static enum CalculationType {
      ADDITIVE {
         public float compute(float value, float amount) {
            return value + amount;
         }
      },
      MULTIPLICATIVE {
         public float compute(float value, float amount) {
            return value * amount;
         }
      };

      public abstract float compute(float var1, float var2);

      @Nonnull
      public String createKey(String armor) {
         return armor + "_" + String.valueOf(this);
      }
   }
}
