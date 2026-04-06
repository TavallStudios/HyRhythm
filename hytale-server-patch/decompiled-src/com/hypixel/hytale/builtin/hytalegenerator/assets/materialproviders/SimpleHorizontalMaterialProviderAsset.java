package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.DecimalConstantsFrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.HorizontalMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class SimpleHorizontalMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<SimpleHorizontalMaterialProviderAsset> CODEC;
   private int topY;
   private int bottomY;
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();
   private String topBaseHeightName = "";
   private String bottomBaseHeightName = "";

   @Nonnull
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.<Material>noMaterialProvider();
      } else {
         double topBaseHeight = 0.0;
         double bottomBaseHeight = 0.0;
         if (!this.topBaseHeightName.isEmpty()) {
            Double topValue = DecimalConstantsFrameworkAsset.Entries.get(this.topBaseHeightName, argument.referenceBundle);
            if (topValue != null) {
               topBaseHeight = topValue;
            }

            Double bottomValue = DecimalConstantsFrameworkAsset.Entries.get(this.bottomBaseHeightName, argument.referenceBundle);
            if (topValue != null) {
               bottomBaseHeight = bottomValue;
            }
         }

         return new HorizontalMaterialProvider<Material>(this.materialProviderAsset.build(argument), (double)this.topY + topBaseHeight, (double)this.bottomY + bottomBaseHeight);
      }
   }

   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(SimpleHorizontalMaterialProviderAsset.class, SimpleHorizontalMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC).append(new KeyedCodec("TopY", Codec.INTEGER, true), (t, k) -> t.topY = k, (k) -> k.topY).add()).append(new KeyedCodec("BottomY", Codec.INTEGER, true), (t, k) -> t.bottomY = k, (k) -> k.bottomY).add()).append(new KeyedCodec("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, (k) -> k.materialProviderAsset).add()).append(new KeyedCodec("TopBaseHeight", Codec.STRING, false), (t, k) -> t.topBaseHeightName = k, (t) -> t.topBaseHeightName).add()).append(new KeyedCodec("BottomBaseHeight", Codec.STRING, false), (t, k) -> t.bottomBaseHeightName = k, (t) -> t.bottomBaseHeightName).add()).build();
   }
}
