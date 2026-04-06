package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.FloorDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class FloorDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<FloorDensityAsset> CODEC;
   private double limit = 0.0;

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new FloorDensity(this.limit, this.buildFirstInput(argument)));
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(FloorDensityAsset.class, FloorDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("Limit", Codec.DOUBLE, true), (t, k) -> t.limit = k, (k) -> k.limit).add()).build();
   }
}
