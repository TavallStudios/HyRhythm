package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.SwitchDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SwitchDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<SwitchDensityAsset> CODEC;
   @Nonnull
   public static final String DEFAULT_STATE = "Default";
   @Nonnull
   public static final int DEFAULT_STATE_HASH = 0;
   private SwitchCaseAsset[] switchCaseAssets = new SwitchCaseAsset[0];

   @Nonnull
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         ArrayList<Integer> switchStates = new ArrayList();
         ArrayList<Density> densityNodes = new ArrayList();

         for(int i = 0; i < this.switchCaseAssets.length; ++i) {
            if (this.switchCaseAssets[i] != null && this.switchCaseAssets[i].densityAsset != null) {
               String stringState = this.switchCaseAssets[i].caseState;
               int stateHash = getHashFromState(stringState);
               Density densityNode = this.switchCaseAssets[i].densityAsset.build(argument);
               switchStates.add(stateHash);
               densityNodes.add(densityNode);
            }
         }

         return new SwitchDensity(densityNodes, switchStates);
      }
   }

   public static int getHashFromState(String state) {
      return "Default".equals(state) ? 0 : Objects.hash(new Object[]{state});
   }

   public void cleanUp() {
      this.cleanUpInputs();

      for(SwitchCaseAsset switchCaseAsset : this.switchCaseAssets) {
         switchCaseAsset.cleanUp();
      }

   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(SwitchDensityAsset.class, SwitchDensityAsset::new, DensityAsset.ABSTRACT_CODEC).append(new KeyedCodec("SwitchCases", new ArrayCodec(SwitchDensityAsset.SwitchCaseAsset.CODEC, (x$0) -> new SwitchCaseAsset[x$0]), false), (t, k) -> t.switchCaseAssets = k, (t) -> t.switchCaseAssets).add()).build();
   }

   public static class SwitchCaseAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, SwitchCaseAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, SwitchCaseAsset> CODEC;
      private String id;
      private AssetExtraInfo.Data data;
      private String caseState = "";
      private DensityAsset densityAsset;

      public String getId() {
         return this.id;
      }

      public void cleanUp() {
         this.densityAsset.cleanUp();
      }

      static {
         CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(SwitchCaseAsset.class, SwitchCaseAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("CaseState", Codec.STRING, true), (t, y) -> t.caseState = y, (t) -> t.caseState).add()).append(new KeyedCodec("Density", DensityAsset.CODEC, true), (t, out) -> t.densityAsset = out, (t) -> t.densityAsset).add()).build();
      }
   }
}
