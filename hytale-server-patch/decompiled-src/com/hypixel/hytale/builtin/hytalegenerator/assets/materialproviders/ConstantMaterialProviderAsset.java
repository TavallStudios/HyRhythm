package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.MaterialAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.ConstantMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<ConstantMaterialProviderAsset> CODEC;
   private MaterialAsset materialAsset = new MaterialAsset();

   @Nonnull
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.<Material>noMaterialProvider();
      } else if (this.materialAsset == null) {
         return new ConstantMaterialProvider<Material>((Object)null);
      } else {
         Material material = this.materialAsset.build(argument.materialCache);
         return new ConstantMaterialProvider<Material>(material);
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ConstantMaterialProviderAsset.class, ConstantMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC).append(new KeyedCodec("Material", MaterialAsset.CODEC, true), (asset, value) -> asset.materialAsset = value, (asset) -> asset.materialAsset).add()).build();
   }
}
