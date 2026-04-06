package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.SwitchStateDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SwitchStateDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<SwitchStateDensityAsset> CODEC;
   private String switchState = "";

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         int stateHash = Objects.hash(new Object[]{this.switchState});
         return new SwitchStateDensity(this.buildFirstInput(argument), stateHash);
      }
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(SwitchStateDensityAsset.class, SwitchStateDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("SwitchState", Codec.STRING, true), (t, k) -> t.switchState = k, (t) -> t.switchState).add()).build();
   }
}
