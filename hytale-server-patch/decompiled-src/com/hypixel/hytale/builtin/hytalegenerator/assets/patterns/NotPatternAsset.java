package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.NotPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class NotPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<NotPatternAsset> CODEC;
   private PatternAsset patternAsset = new ConstantPatternAsset();

   @Nonnull
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      return (Pattern)(super.isSkipped() ? Pattern.noPattern() : new NotPattern(this.patternAsset.build(argument)));
   }

   public void cleanUp() {
      this.patternAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(NotPatternAsset.class, NotPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("Pattern", PatternAsset.CODEC, true), (t, k) -> t.patternAsset = k, (k) -> k.patternAsset).add()).build();
   }
}
