package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockset.MaterialSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.MaterialSetPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class BlockSetPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<BlockSetPatternAsset> CODEC;
   private MaterialSetAsset materialSetAsset = new MaterialSetAsset();

   @Nonnull
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         MaterialSet blockSet = this.materialSetAsset.build(argument.materialCache);
         return new MaterialSetPattern(blockSet);
      }
   }

   public void cleanUp() {
      this.materialSetAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(BlockSetPatternAsset.class, BlockSetPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("BlockSet", MaterialSetAsset.CODEC, true), (t, k) -> t.materialSetAsset = k, (k) -> k.materialSetAsset).add()).build();
   }
}
