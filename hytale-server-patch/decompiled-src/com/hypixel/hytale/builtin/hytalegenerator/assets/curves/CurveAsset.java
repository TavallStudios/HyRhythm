package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public abstract class CurveAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CurveAsset>>, Cleanable {
   @Nonnull
   private static final CurveAsset[] EMPTY_INPUTS = new CurveAsset[0];
   @Nonnull
   public static final AssetCodecMapCodec<String, CurveAsset> CODEC;
   @Nonnull
   private static final Map<String, CurveAsset> exportedNodes;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final BuilderCodec<CurveAsset> ABSTRACT_CODEC;
   private String id;
   private AssetExtraInfo.Data data;
   private String exportName = "";

   protected CurveAsset() {
   }

   public abstract Double2DoubleFunction build();

   public static CurveAsset getExportedAsset(@Nonnull String name) {
      return (CurveAsset)exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   static {
      CODEC = new AssetCodecMapCodec<String, CurveAsset>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.data = data, (t) -> t.data);
      exportedNodes = new ConcurrentHashMap();
      CHILD_ASSET_CODEC = new ContainedAssetCodec(CurveAsset.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      ABSTRACT_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(CurveAsset.class).append(new KeyedCodec("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, (t) -> t.exportName).add()).afterDecode((asset) -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            if (exportedNodes.containsKey(asset.exportName)) {
               LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
            }

            exportedNodes.put(asset.exportName, asset);
            LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }

      })).build();
   }
}
