package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.StaticDirectionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import javax.annotation.Nonnull;

public class StaticDirectionalityAsset extends DirectionalityAsset {
   @Nonnull
   public static final BuilderCodec<StaticDirectionalityAsset> CODEC;
   private int rotation = 0;
   private PatternAsset patternAsset = new ConstantPatternAsset();

   @Nonnull
   public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
      PrefabRotation var10000;
      switch (this.rotation) {
         case 90 -> var10000 = PrefabRotation.ROTATION_90;
         case 180 -> var10000 = PrefabRotation.ROTATION_180;
         case 270 -> var10000 = PrefabRotation.ROTATION_270;
         default -> var10000 = PrefabRotation.ROTATION_0;
      }

      PrefabRotation prefabRotation = var10000;
      return new StaticDirectionality(prefabRotation, this.patternAsset.build(PatternAsset.argumentFrom(argument)));
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StaticDirectionalityAsset.class, StaticDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC).append(new KeyedCodec("Rotation", Codec.INTEGER, false), (asset, v) -> asset.rotation = v, (asset) -> asset.rotation).addValidator((LegacyValidator)((v, r) -> {
         if (v != 0 && v != 90 && v != 180 && v != 270) {
            r.fail("Rotation can only have the values: 0, 90, 180, 270");
         }

      })).add()).append(new KeyedCodec("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, (asset) -> asset.patternAsset).add()).build();
   }
}
