package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MultiMixDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class MultiMixDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<MultiMixDensityAsset> CODEC;
   private KeyAsset[] keyAssets = new KeyAsset[0];

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         List<Density> densityInputs = this.buildInputs(argument, true);
         if (densityInputs.isEmpty()) {
            return new ConstantValueDensity(0.0);
         } else {
            ArrayList<MultiMixDensity.Key> keys = new ArrayList(this.keyAssets.length);

            for(KeyAsset keyAsset : this.keyAssets) {
               if (keyAsset.densityIndex < 0) {
                  keys.add(new MultiMixDensity.Key(keyAsset.value, (Density)null));
               } else if (keyAsset.densityIndex >= densityInputs.size() - 1) {
                  Logger var10000 = LoggerUtil.getLogger();
                  int var10001 = keyAsset.densityIndex;
                  var10000.warning("Density Index out of bounds in MultiMix node " + var10001 + ", valid range is [0, " + (densityInputs.size() - 1) + "]");
                  keys.add(new MultiMixDensity.Key(keyAsset.value, (Density)null));
               } else {
                  Density density = (Density)densityInputs.get(keyAsset.densityIndex);
                  keys.add(new MultiMixDensity.Key(keyAsset.value, density));
               }
            }

            int i = 1;

            while(i < keys.size()) {
               MultiMixDensity.Key previousKey = (MultiMixDensity.Key)keys.get(i - 1);
               MultiMixDensity.Key currentKey = (MultiMixDensity.Key)keys.get(i);
               if (previousKey.value() == currentKey.value()) {
                  keys.remove(i);
               } else {
                  ++i;
               }
            }

            i = 0;

            while(i < keys.size()) {
               if (((MultiMixDensity.Key)keys.get(i)).density() == null) {
                  keys.remove(i);
               } else {
                  ++i;
               }
            }

            for(int i = keys.size() - 1; i >= 0 && ((MultiMixDensity.Key)keys.get(i)).density() == null; --i) {
               keys.remove(i);
            }

            for(int i = keys.size() - 2; i >= 0; --i) {
               if (((MultiMixDensity.Key)keys.get(i)).density() == null && ((MultiMixDensity.Key)keys.get(i + 1)).density() == null) {
                  keys.remove(i);
               }
            }

            if (keys.isEmpty()) {
               return new ConstantValueDensity(0.0);
            } else if (keys.size() == 1) {
               return ((MultiMixDensity.Key)keys.getFirst()).density();
            } else {
               keys.trimToSize();
               Density influenceDensity = (Density)densityInputs.getLast();
               return new MultiMixDensity(keys, influenceDensity);
            }
         }
      }
   }

   public void cleanUp() {
      this.cleanUpInputs();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(MultiMixDensityAsset.class, MultiMixDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("Keys", new ArrayCodec(MultiMixDensityAsset.KeyAsset.CODEC, (x$0) -> new KeyAsset[x$0]), true), (asset, v) -> asset.keyAssets = v, (asset) -> asset.keyAssets).add()).build();
   }

   public static class KeyAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, KeyAsset>> {
      public static final int NO_DENSITY_INDEX = 0;
      @Nonnull
      public static final AssetBuilderCodec<String, KeyAsset> CODEC;
      private String id;
      private AssetExtraInfo.Data data;
      private double value = 0.0;
      private int densityIndex = 0;

      public String getId() {
         return this.id;
      }

      static {
         CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(KeyAsset.class, KeyAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("Value", Codec.DOUBLE, true), (t, value) -> t.value = value, (t) -> t.value).add()).append(new KeyedCodec("DensityIndex", Codec.INTEGER, true), (t, value) -> t.densityIndex = value, (t) -> t.densityIndex).add()).build();
      }
   }
}
