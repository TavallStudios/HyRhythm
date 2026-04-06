package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.FieldFunctionPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public class DensityPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<DensityPatternAsset> CODEC;
   private DelimiterAsset[] delimiterAssets = new DelimiterAsset[0];
   private DensityAsset densityAsset = new ConstantDensityAsset();

   @Nonnull
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Density field = this.densityAsset.build(DensityAsset.from(argument));
         FieldFunctionPattern out = new FieldFunctionPattern(field);

         for(DelimiterAsset asset : this.delimiterAssets) {
            out.addDelimiter(asset.min, asset.max);
         }

         return out;
      }
   }

   public void cleanUp() {
      this.densityAsset.cleanUp();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DensityPatternAsset.class, DensityPatternAsset::new, PatternAsset.ABSTRACT_CODEC).append(new KeyedCodec("Delimiters", new ArrayCodec(DensityPatternAsset.DelimiterAsset.CODEC, (x$0) -> new DelimiterAsset[x$0]), true), (t, k) -> t.delimiterAssets = k, (k) -> k.delimiterAssets).add()).append(new KeyedCodec("FieldFunction", DensityAsset.CODEC, true), (t, k) -> t.densityAsset = k, (k) -> k.densityAsset).add()).build();
   }

   public static class DelimiterAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, DelimiterAsset> CODEC;
      private String id;
      private AssetExtraInfo.Data data;
      private double min;
      private double max;

      public String getId() {
         return this.id;
      }

      static {
         CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(DelimiterAsset.class, DelimiterAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("Min", Codec.DOUBLE, true), (t, v) -> t.min = v, (t) -> t.min).add()).append(new KeyedCodec("Max", Codec.DOUBLE, true), (t, v) -> t.max = v, (t) -> t.max).add()).build();
      }
   }
}
