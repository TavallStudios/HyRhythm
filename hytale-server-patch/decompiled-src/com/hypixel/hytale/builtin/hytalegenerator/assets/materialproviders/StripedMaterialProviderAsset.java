package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.StripedMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class StripedMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<StripedMaterialProviderAsset> CODEC;
   private StripeAsset[] stripeAssets = new StripeAsset[0];
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.<Material>noMaterialProvider();
      } else {
         ArrayList<StripedMaterialProvider.Stripe> stripes = new ArrayList();

         for(StripeAsset asset : this.stripeAssets) {
            if (asset == null) {
               LoggerUtil.getLogger().warning("Couldn't load a strip asset, will skip it.");
            } else {
               StripedMaterialProvider.Stripe stripe = new StripedMaterialProvider.Stripe(asset.topY, asset.bottomY);
               stripes.add(stripe);
            }
         }

         MaterialProvider<Material> materialProvider = this.materialProviderAsset.build(argument);
         return new StripedMaterialProvider<Material>(materialProvider, stripes);
      }
   }

   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StripedMaterialProviderAsset.class, StripedMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC).append(new KeyedCodec("Stripes", new ArrayCodec(StripedMaterialProviderAsset.StripeAsset.CODEC, (x$0) -> new StripeAsset[x$0]), true), (t, k) -> t.stripeAssets = k, (k) -> k.stripeAssets).add()).append(new KeyedCodec("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, (k) -> k.materialProviderAsset).add()).build();
   }

   public static class StripeAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, StripeAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, StripeAsset> CODEC;
      private String id;
      private AssetExtraInfo.Data data;
      private int topY;
      private int bottomY;

      public String getId() {
         return this.id;
      }

      static {
         CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(StripeAsset.class, StripeAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("TopY", Codec.INTEGER, true), (t, y) -> t.topY = y, (t) -> t.bottomY).add()).append(new KeyedCodec("BottomY", Codec.INTEGER, true), (t, y) -> t.bottomY = y, (t) -> t.bottomY).add()).build();
      }
   }
}
