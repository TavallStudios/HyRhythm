package com.hypixel.hytale.builtin.hytalegenerator.assets.framework;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class FrameworkAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, FrameworkAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, FrameworkAsset> CODEC;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final BuilderCodec<FrameworkAsset> ABSTRACT_CODEC;
   private String id;
   private AssetExtraInfo.Data data;

   protected FrameworkAsset() {
   }

   public String getId() {
      return this.id;
   }

   public void cleanUp() {
   }

   public abstract void build(@Nonnull WorldStructureAsset.Argument var1, @Nonnull ReferenceBundle var2);

   static {
      CODEC = new AssetCodecMapCodec<String, FrameworkAsset>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.data = data, (t) -> t.data);
      CHILD_ASSET_CODEC = new ContainedAssetCodec(FrameworkAsset.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      ABSTRACT_CODEC = BuilderCodec.abstractBuilder(FrameworkAsset.class).build();
   }
}
