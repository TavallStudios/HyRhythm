package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.AndPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class AndPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<AndPatternAsset> CODEC;
   private PatternAsset[] patternAssets = new PatternAsset[0];

   @Nonnull
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         ArrayList<Pattern> patterns = new ArrayList(this.patternAssets.length);

         for(PatternAsset asset : this.patternAssets) {
            if (!asset.isSkipped()) {
               patterns.add(asset.build(argument));
            }
         }

         return new AndPattern(patterns);
      }
   }

   public void cleanUp() {
      for(PatternAsset patternAsset : this.patternAssets) {
         patternAsset.cleanUp();
      }

   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(AndPatternAsset.class, AndPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("Patterns", new ArrayCodec(PatternAsset.CODEC, (x$0) -> new PatternAsset[x$0]), true), (t, k) -> t.patternAssets = k, (k) -> k.patternAssets).add()).build();
   }
}
