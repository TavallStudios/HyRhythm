package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<ConstantPatternAsset> CODEC;
   private boolean value;

   @Nonnull
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      return super.isSkipped() ? Pattern.noPattern() : new Pattern() {
         public boolean matches(@Nonnull Pattern.Context context) {
            return ConstantPatternAsset.this.value;
         }

         @Nonnull
         public SpaceSize readSpace() {
            return SpaceSize.empty();
         }
      };
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ConstantPatternAsset.class, ConstantPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("Value", Codec.BOOLEAN, true), (asset, value) -> asset.value = value, (value) -> value.value).add()).build();
   }
}
