package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageCalculator {
   public static final BuilderCodec<DamageCalculator> CODEC;
   protected Type type;
   @Nonnull
   protected DamageClass damageClass;
   protected Object2FloatMap<String> baseDamageRaw;
   protected float sequentialModifierStep;
   protected float sequentialModifierMinimum;
   protected float randomPercentageModifier;
   @Nonnull
   protected transient Int2FloatMap baseDamage;

   protected DamageCalculator() {
      this.type = DamageCalculator.Type.ABSOLUTE;
      this.damageClass = DamageClass.UNKNOWN;
      this.baseDamage = Int2FloatMaps.EMPTY_MAP;
   }

   @Nullable
   public Object2FloatMap<DamageCause> calculateDamage(double durationSeconds) {
      if (this.baseDamageRaw != null && !this.baseDamageRaw.isEmpty()) {
         Object2FloatMap<DamageCause> outDamage = new Object2FloatOpenHashMap(this.baseDamage.size());
         float randomPercentageModifier = MathUtil.randomFloat(-this.randomPercentageModifier, this.randomPercentageModifier);
         ObjectIterator var5 = this.baseDamage.int2FloatEntrySet().iterator();

         while(var5.hasNext()) {
            Int2FloatMap.Entry entry = (Int2FloatMap.Entry)var5.next();
            DamageCause damageCause = (DamageCause)DamageCause.getAssetMap().getAsset(entry.getIntKey());
            float value = entry.getFloatValue();
            float damage = this.scaleDamage(durationSeconds, value);
            damage += damage * randomPercentageModifier;
            outDamage.put(damageCause, damage);
         }

         return outDamage;
      } else {
         return null;
      }
   }

   private float scaleDamage(double durationSeconds, float damage) {
      float var10000;
      switch (this.type.ordinal()) {
         case 0 -> var10000 = (float)durationSeconds * damage;
         case 1 -> var10000 = damage;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public Type getType() {
      return this.type;
   }

   @Nonnull
   public DamageClass getDamageClass() {
      return this.damageClass;
   }

   public float getSequentialModifierStep() {
      return this.sequentialModifierStep;
   }

   public float getSequentialModifierMinimum() {
      return this.sequentialModifierMinimum;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof DamageCalculator) {
         DamageCalculator that = (DamageCalculator)o;
         if (Double.compare((double)that.sequentialModifierStep, (double)this.sequentialModifierStep) != 0) {
            return false;
         } else if (Double.compare((double)that.sequentialModifierMinimum, (double)this.sequentialModifierMinimum) != 0) {
            return false;
         } else if (Double.compare((double)that.randomPercentageModifier, (double)this.randomPercentageModifier) != 0) {
            return false;
         } else if (this.type != that.type) {
            return false;
         } else {
            return !Objects.equals(this.baseDamageRaw, that.baseDamageRaw) ? false : false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.type != null ? this.type.hashCode() : 0;
      result = 31 * result + (this.baseDamageRaw != null ? this.baseDamageRaw.hashCode() : 0);
      long temp = Double.doubleToLongBits((double)this.sequentialModifierStep);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits((double)this.sequentialModifierMinimum);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      temp = Double.doubleToLongBits((double)this.randomPercentageModifier);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.type);
      return "DamageCalculator{type=" + var10000 + ", baseDamage=" + String.valueOf(this.baseDamageRaw) + ", sequentialModifierStep=" + this.sequentialModifierStep + ", sequentialModifierMinimum=" + this.sequentialModifierMinimum + ", randomPercentageModifier=" + this.randomPercentageModifier + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DamageCalculator.class, DamageCalculator::new).appendInherited(new KeyedCodec("Type", DamageCalculator.Type.CODEC), (damageCalculator, type) -> damageCalculator.type = type, (damageCalculator) -> damageCalculator.type, (damageCalculator, parent) -> damageCalculator.type = parent.type).add()).appendInherited(new KeyedCodec("Class", DamageClass.CODEC), (o, v) -> o.damageClass = v, (o) -> o.damageClass, (o, p) -> o.damageClass = p.damageClass).documentation("The class of the damage being created, used by the damage system to apply modifiers based on equipment of the source.").addValidator(Validators.nonNull()).add()).appendInherited(new KeyedCodec("BaseDamage", new Object2FloatMapCodec(Codec.STRING, Object2FloatOpenHashMap::new)), (damageCalculator, map) -> damageCalculator.baseDamageRaw = map, (damageCalculator) -> damageCalculator.baseDamageRaw, (damageCalculator, parent) -> damageCalculator.baseDamageRaw = parent.baseDamageRaw).addValidator(DamageCause.VALIDATOR_CACHE.getMapKeyValidator()).add()).appendInherited(new KeyedCodec("SequentialModifierStep", Codec.FLOAT), (damageCalculator, sequentialModifierStep) -> damageCalculator.sequentialModifierStep = sequentialModifierStep, (damageCalculator) -> damageCalculator.sequentialModifierStep, (damageCalculator, parent) -> damageCalculator.sequentialModifierStep = parent.sequentialModifierStep).add()).appendInherited(new KeyedCodec("SequentialModifierMinimum", Codec.FLOAT), (damageCalculator, sequentialModifierMinimum) -> damageCalculator.sequentialModifierMinimum = sequentialModifierMinimum, (damageCalculator) -> damageCalculator.sequentialModifierMinimum, (damageCalculator, parent) -> damageCalculator.sequentialModifierMinimum = parent.sequentialModifierMinimum).add()).appendInherited(new KeyedCodec("RandomPercentageModifier", Codec.FLOAT), (damageCalculator, randomPercentageModifier) -> damageCalculator.randomPercentageModifier = randomPercentageModifier, (damageCalculator) -> damageCalculator.randomPercentageModifier, (damageCalculator, parent) -> damageCalculator.randomPercentageModifier = parent.randomPercentageModifier).addValidator(Validators.greaterThanOrEqual(0.0F)).add()).afterDecode((asset) -> {
         if (asset.baseDamageRaw != null) {
            asset.baseDamage = new Int2FloatOpenHashMap();
            Iterator i$ = asset.baseDamageRaw.object2FloatEntrySet().iterator();

            while(i$.hasNext()) {
               Object2FloatMap.Entry<String> entry = (Object2FloatMap.Entry)i$.next();
               int index = DamageCause.getAssetMap().getIndex((String)entry.getKey());
               asset.baseDamage.put(index, entry.getFloatValue());
            }
         }

      })).build();
   }

   public static enum Type {
      DPS,
      ABSOLUTE;

      public static final EnumCodec<Type> CODEC = new EnumCodec<Type>(Type.class);
   }
}
