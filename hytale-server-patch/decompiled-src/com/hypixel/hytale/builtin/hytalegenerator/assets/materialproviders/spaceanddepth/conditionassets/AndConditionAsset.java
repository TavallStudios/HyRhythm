package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.conditions.AndCondition;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class AndConditionAsset extends ConditionAsset {
   @Nonnull
   public static final BuilderCodec<AndConditionAsset> CODEC;
   private ConditionAsset[] conditionAssets = new ConditionAsset[0];

   @Nonnull
   public SpaceAndDepthMaterialProvider.Condition build() {
      ArrayList<SpaceAndDepthMaterialProvider.Condition> conditions = new ArrayList(this.conditionAssets.length);

      for(ConditionAsset asset : this.conditionAssets) {
         if (asset == null) {
            LoggerUtil.getLogger().warning("Null condition asset found, skipped.");
         } else {
            conditions.add(asset.build());
         }
      }

      return new AndCondition(conditions);
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(AndConditionAsset.class, AndConditionAsset::new, ConditionAsset.ABSTRACT_CODEC).append(new KeyedCodec("Conditions", new ArrayCodec(ConditionAsset.CODEC, (x$0) -> new ConditionAsset[x$0]), true), (t, k) -> t.conditionAssets = k, (k) -> k.conditionAssets).addValidator(Validators.nonNullArrayElements()).add()).build();
   }
}
