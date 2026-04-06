package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.CurveMapperDensity;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class CurveMapperDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<CurveMapperDensityAsset> CODEC;
   private CurveAsset curveAsset = new ConstantCurveAsset();

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new CurveMapperDensity(this.curveAsset.build(), this.buildFirstInput(argument)));
   }

   public void cleanUp() {
      this.cleanUpInputs();
      this.curveAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(CurveMapperDensityAsset.class, CurveMapperDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("Curve", CurveAsset.CODEC, true), (t, k) -> t.curveAsset = k, (k) -> k.curveAsset).add()).build();
   }
}
