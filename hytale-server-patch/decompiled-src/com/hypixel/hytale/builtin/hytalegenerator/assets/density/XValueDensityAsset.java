package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.XValueDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class XValueDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<XValueDensityAsset> CODEC;

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new XValueDensity());
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = BuilderCodec.builder(XValueDensityAsset.class, XValueDensityAsset::new, DensityAsset.ABSTRACT_CODEC).build();
   }
}
