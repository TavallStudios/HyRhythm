package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MaxDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class MaxDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<MaxDensityAsset> CODEC;

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new MaxDensity(this.buildInputs(argument, true)));
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = BuilderCodec.builder(MaxDensityAsset.class, MaxDensityAsset::new, DensityAsset.ABSTRACT_CODEC).build();
   }
}
