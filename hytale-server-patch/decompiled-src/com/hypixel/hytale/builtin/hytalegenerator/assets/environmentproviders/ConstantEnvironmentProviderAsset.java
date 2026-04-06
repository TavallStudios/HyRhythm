package com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders;

import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.ConstantEnvironmentProvider;
import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.EnvironmentProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import javax.annotation.Nonnull;

public class ConstantEnvironmentProviderAsset extends EnvironmentProviderAsset {
   @Nonnull
   public static final BuilderCodec<ConstantEnvironmentProviderAsset> CODEC;
   private String environment = "Unknown";

   @Nonnull
   public EnvironmentProvider build(@Nonnull EnvironmentProviderAsset.Argument argument) {
      if (super.isSkipped()) {
         return EnvironmentProvider.noEnvironmentProvider();
      } else {
         int index = Environment.getAssetMap().getIndex(this.environment);
         if (index == -2147483648) {
            index = 0;
         }

         return new ConstantEnvironmentProvider(index);
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ConstantEnvironmentProviderAsset.class, ConstantEnvironmentProviderAsset::new, EnvironmentProviderAsset.ABSTRACT_CODEC).append(new KeyedCodec("Environment", Codec.STRING, true), (t, k) -> t.environment = k, (k) -> k.environment).add()).build();
   }
}
