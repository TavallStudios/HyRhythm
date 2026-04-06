package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class ConditionAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ConditionAsset>> {
   @Nonnull
   private static final ConditionAsset[] EMPTY_INPUTS = new ConditionAsset[0];
   @Nonnull
   public static final AssetCodecMapCodec<String, ConditionAsset> CODEC;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final BuilderCodec<ConditionAsset> ABSTRACT_CODEC;
   private String id;
   private AssetExtraInfo.Data data;

   protected ConditionAsset() {
   }

   public abstract SpaceAndDepthMaterialProvider.Condition build();

   public String getId() {
      return this.id;
   }

   static {
      CODEC = new AssetCodecMapCodec<String, ConditionAsset>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.data = data, (t) -> t.data);
      CHILD_ASSET_CODEC = new ContainedAssetCodec(ConditionAsset.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      ABSTRACT_CODEC = BuilderCodec.abstractBuilder(ConditionAsset.class).build();
   }
}
