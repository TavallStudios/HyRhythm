package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.YSampledDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class YSampledDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<YSampledDensityAsset> CODEC;
   private double sampleDistance = 4.0;
   private double sampleOffset = 0.0;

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.sampleDistance <= 0.0 ? new ConstantValueDensity(0.0) : new YSampledDensity(this.buildFirstInput(argument), this.sampleDistance, this.sampleOffset));
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(YSampledDensityAsset.class, YSampledDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("SampleDistance", Codec.DOUBLE, true), (asset, value) -> asset.sampleDistance = value, (asset) -> asset.sampleDistance).addValidator(Validators.greaterThan(0.0)).add()).append(new KeyedCodec("SampleOffset", Codec.DOUBLE, true), (asset, value) -> asset.sampleOffset = value, (asset) -> asset.sampleOffset).add()).build();
   }
}
