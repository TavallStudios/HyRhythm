package com.hypixel.hytale.builtin.hytalegenerator.assets;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.List;
import javax.annotation.Nonnull;

public class SettingsAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SettingsAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, SettingsAsset> CODEC;
   private String id;
   private AssetExtraInfo.Data data;
   private Integer[] checkpoints = new Integer[0];
   private int customConcurrency = -1;
   private double bufferCapacityFactor = 0.3;
   private double targetViewDistance = 512.0;
   private double targetPlayerCount = 3.0;

   private SettingsAsset() {
   }

   @Nonnull
   public List<Integer> getStatsCheckpoints() {
      return List.of(this.checkpoints);
   }

   public int getCustomConcurrency() {
      return this.customConcurrency;
   }

   public double getBufferCapacityFactor() {
      return this.bufferCapacityFactor;
   }

   public double getTargetViewDistance() {
      return this.targetViewDistance;
   }

   public double getTargetPlayerCount() {
      return this.targetPlayerCount;
   }

   public static int getSampleBits(int v) {
      byte var10000;
      switch (v) {
         case 2 -> var10000 = 1;
         case 4 -> var10000 = 2;
         case 8 -> var10000 = 3;
         default -> var10000 = 0;
      }

      return var10000;
   }

   public String getId() {
      return this.id;
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(SettingsAsset.class, SettingsAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("StatsCheckpoints", new ArrayCodec(Codec.INTEGER, (x$0) -> new Integer[x$0]), true), (t, k) -> t.checkpoints = k, (t) -> t.checkpoints).add()).append(new KeyedCodec("CustomConcurrency", Codec.INTEGER, true), (t, k) -> t.customConcurrency = k, (t) -> t.customConcurrency).addValidator(Validators.greaterThan(-2)).add()).append(new KeyedCodec("BufferCapacityFactor", Codec.DOUBLE, true), (asset, value) -> asset.bufferCapacityFactor = value, (asset) -> asset.bufferCapacityFactor).addValidator(Validators.greaterThanOrEqual(0.0)).add()).append(new KeyedCodec("TargetViewDistance", Codec.DOUBLE, true), (asset, value) -> asset.targetViewDistance = value, (asset) -> asset.targetViewDistance).addValidator(Validators.greaterThanOrEqual(0.0)).add()).append(new KeyedCodec("TargetPlayerCount", Codec.DOUBLE, true), (asset, value) -> asset.targetPlayerCount = value, (asset) -> asset.targetPlayerCount).addValidator(Validators.greaterThanOrEqual(0.0)).add()).build();
   }
}
