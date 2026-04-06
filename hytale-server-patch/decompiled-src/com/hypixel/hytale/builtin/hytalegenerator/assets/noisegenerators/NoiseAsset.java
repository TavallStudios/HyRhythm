package com.hypixel.hytale.builtin.hytalegenerator.assets.noisegenerators;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.NoiseField;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class NoiseAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, NoiseAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, NoiseAsset> CODEC;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final BuilderCodec<NoiseAsset> ABSTRACT_CODEC;
   private String id;
   private AssetExtraInfo.Data data;

   protected NoiseAsset() {
   }

   public abstract NoiseField build(@Nonnull SeedBox var1);

   public String getId() {
      return this.id;
   }

   static {
      CODEC = new AssetCodecMapCodec<String, NoiseAsset>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.data = data, (t) -> t.data);
      CHILD_ASSET_CODEC = new ContainedAssetCodec(NoiseAsset.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      ABSTRACT_CODEC = BuilderCodec.abstractBuilder(NoiseAsset.class).build();
   }
}
