package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.ConditionParameter;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.SmallerThanCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class SmallerThanConditionAsset extends ConditionAsset {
   @Nonnull
   public static final BuilderCodec<SmallerThanConditionAsset> CODEC;
   private ConditionParameter parameter;
   private int threshold;

   public SmallerThanConditionAsset() {
      this.parameter = ConditionParameter.SPACE_ABOVE_FLOOR;
   }

   @Nonnull
   public SpaceAndDepthMaterialProvider.Condition build() {
      return new SmallerThanCondition(this.threshold, this.parameter);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(SmallerThanConditionAsset.class, SmallerThanConditionAsset::new, ConditionAsset.ABSTRACT_CODEC).append(new KeyedCodec("ContextToCheck", ConditionParameter.CODEC, true), (t, k) -> t.parameter = k, (k) -> k.parameter).add()).append(new KeyedCodec("Threshold", Codec.INTEGER, true), (t, k) -> t.threshold = k, (k) -> k.threshold).add()).build();
   }
}
