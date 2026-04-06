package com.hypixel.hytale.builtin.hytalegenerator.assets.framework;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PositionsFrameworkAsset extends FrameworkAsset {
   @Nonnull
   public static final String NAME = "Positions";
   @Nonnull
   public static final Class<Entries> CLASS = Entries.class;
   @Nonnull
   public static final BuilderCodec<PositionsFrameworkAsset> CODEC;
   private String id;
   private AssetExtraInfo.Data data;
   private EntryAsset[] entryAssets = new EntryAsset[0];

   private PositionsFrameworkAsset() {
   }

   public String getId() {
      return this.id;
   }

   public void build(@NonNullDecl WorldStructureAsset.Argument argument, @NonNullDecl ReferenceBundle referenceBundle) {
      Entries entries = new Entries();

      for(EntryAsset entryAsset : this.entryAssets) {
         entries.put(entryAsset.name, entryAsset.positionProviderAsset);
      }

      referenceBundle.put("Positions", entries, CLASS);
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(PositionsFrameworkAsset.class, PositionsFrameworkAsset::new, FrameworkAsset.ABSTRACT_CODEC).append(new KeyedCodec("Entries", new ArrayCodec(PositionsFrameworkAsset.EntryAsset.CODEC, (x$0) -> new EntryAsset[x$0]), true), (asset, value) -> asset.entryAssets = value, (asset) -> asset.entryAssets).add()).build();
   }

   public static class EntryAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, EntryAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, EntryAsset> CODEC;
      private String id;
      private AssetExtraInfo.Data data;
      private String name = "";
      private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

      public String getId() {
         return this.id;
      }

      static {
         CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(EntryAsset.class, EntryAsset::new, Codec.STRING, (asset, id) -> asset.id = id, (config) -> config.id, (config, data) -> config.data = data, (config) -> config.data).append(new KeyedCodec("Name", Codec.STRING, true), (asset, value) -> asset.name = value, (asset) -> asset.name).add()).append(new KeyedCodec("Positions", PositionProviderAsset.CODEC, true), (asset, value) -> asset.positionProviderAsset = value, (asset) -> asset.positionProviderAsset).add()).build();
      }
   }

   public static class Entries extends HashMap<String, PositionProviderAsset> {
      @Nullable
      public static Entries get(@Nonnull ReferenceBundle referenceBundle) {
         return (Entries)referenceBundle.get("Positions", PositionsFrameworkAsset.CLASS);
      }

      @Nullable
      public static PositionProviderAsset get(@Nonnull String name, @Nonnull ReferenceBundle referenceBundle) {
         Entries entries = get(referenceBundle);
         return entries == null ? null : (PositionProviderAsset)entries.get(name);
      }
   }
}
