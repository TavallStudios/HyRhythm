package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.ItemReticleClientEvent;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class ItemReticleConfig implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ItemReticleConfig>>, NetworkSerializable<com.hypixel.hytale.protocol.ItemReticleConfig> {
   public static final AssetBuilderCodec<String, ItemReticleConfig> CODEC;
   public static final int DEFAULT_INDEX = 0;
   public static final String DEFAULT_ID = "Default";
   public static final ItemReticleConfig DEFAULT;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, ItemReticleConfig, IndexedLookupTableAssetMap<String, ItemReticleConfig>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String[] base;
   protected Map<String, ItemReticleWithDuration> serverEvents;
   protected Int2ObjectMap<ItemReticleWithDuration> indexedServerEvents;
   protected Map<ItemReticleClientEvent, ItemReticle> clientEvents;
   private SoftReference<com.hypixel.hytale.protocol.ItemReticleConfig> cachedPacket;

   public static AssetStore<String, ItemReticleConfig, IndexedLookupTableAssetMap<String, ItemReticleConfig>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, ItemReticleConfig, IndexedLookupTableAssetMap<String, ItemReticleConfig>>getAssetStore(ItemReticleConfig.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, ItemReticleConfig> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   protected ItemReticleConfig() {
   }

   public ItemReticleConfig(String id) {
      this.id = id;
   }

   protected void processConfig() {
      if (this.serverEvents != null && !this.serverEvents.isEmpty()) {
         this.indexedServerEvents = new Int2ObjectOpenHashMap();
         this.serverEvents.forEach((event, config) -> this.indexedServerEvents.put(AssetRegistry.getOrCreateTagIndex(event), config));
      }

   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemReticleConfig toPacket() {
      com.hypixel.hytale.protocol.ItemReticleConfig cached = this.cachedPacket == null ? null : (com.hypixel.hytale.protocol.ItemReticleConfig)this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemReticleConfig packet = new com.hypixel.hytale.protocol.ItemReticleConfig();
         packet.base = this.base;
         if (this.indexedServerEvents != null) {
            packet.serverEvents = new Int2ObjectOpenHashMap();
            ObjectIterator var3 = this.indexedServerEvents.int2ObjectEntrySet().iterator();

            while(var3.hasNext()) {
               Int2ObjectMap.Entry<ItemReticleWithDuration> event = (Int2ObjectMap.Entry)var3.next();
               packet.serverEvents.put(event.getIntKey(), ((ItemReticleWithDuration)event.getValue()).toPacket());
            }
         }

         if (this.clientEvents != null) {
            packet.clientEvents = new EnumMap(ItemReticleClientEvent.class);

            for(Map.Entry<ItemReticleClientEvent, ItemReticle> event : this.clientEvents.entrySet()) {
               packet.clientEvents.put((ItemReticleClientEvent)event.getKey(), ((ItemReticle)event.getValue()).toPacket());
            }
         }

         this.cachedPacket = new SoftReference(packet);
         return packet;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "ItemReticleConfig{id='" + var10000 + "', base=" + Arrays.toString(this.base) + ", serverEvents=" + String.valueOf(this.serverEvents) + ", indexedServerEvents=" + String.valueOf(this.indexedServerEvents) + ", clientEvents=" + String.valueOf(this.clientEvents) + "}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ItemReticleConfig.class, ItemReticleConfig::new, Codec.STRING, (itemReticleConfig, s) -> itemReticleConfig.id = s, (itemReticleConfig) -> itemReticleConfig.id, (asset, data) -> asset.data = data, (asset) -> asset.data).appendInherited(new KeyedCodec("Base", new ArrayCodec(Codec.STRING, (x$0) -> new String[x$0])), (itemReticleConfig, o) -> itemReticleConfig.base = o, (itemReticleConfig) -> itemReticleConfig.base, (itemReticleConfig, parent) -> itemReticleConfig.base = parent.base).documentation("Paths to the parts that should be displayed for the base reticle.").addValidator(Validators.nonNull()).addValidator(Validators.nonEmptyArray()).addValidator(CommonAssetValidator.UI_RETICLE_PARTS_ARRAY).add()).appendInherited(new KeyedCodec("ServerEvents", new MapCodec(ItemReticleConfig.ItemReticleWithDuration.CODEC, HashMap::new)), (itemReticleConfig, o) -> itemReticleConfig.serverEvents = o, (itemReticleConfig) -> itemReticleConfig.serverEvents, (itemReticleConfig, parent) -> itemReticleConfig.serverEvents = parent.serverEvents).documentation("A map of reticle configurations for server-side events.").add()).appendInherited(new KeyedCodec("ClientEvents", new EnumMapCodec(ItemReticleClientEvent.class, ItemReticleConfig.ItemReticle.CODEC)), (itemReticleConfig, o) -> itemReticleConfig.clientEvents = o, (itemReticleConfig) -> itemReticleConfig.clientEvents, (itemReticleConfig, parent) -> itemReticleConfig.clientEvents = parent.clientEvents).documentation("A map of reticle configurations for client-side events.").add()).afterDecode(ItemReticleConfig::processConfig)).build();
      DEFAULT = new ItemReticleConfig("Default") {
         {
            this.base = new String[]{"UI/Reticles/Default.png"};
         }
      };
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ItemReticleConfig::getAssetStore));
   }

   static class ItemReticle implements NetworkSerializable<com.hypixel.hytale.protocol.ItemReticle> {
      public static final BuilderCodec<ItemReticle> CODEC;
      protected boolean hideBase;
      protected String[] parts;

      public ItemReticle(boolean hideBase, String[] parts) {
         this.hideBase = hideBase;
         this.parts = parts;
      }

      public ItemReticle() {
      }

      @Nonnull
      public com.hypixel.hytale.protocol.ItemReticle toPacket() {
         com.hypixel.hytale.protocol.ItemReticle packet = new com.hypixel.hytale.protocol.ItemReticle();
         packet.hideBase = this.hideBase;
         packet.parts = this.parts;
         return packet;
      }

      @Nonnull
      public String toString() {
         boolean var10000 = this.hideBase;
         return "ItemReticle{, hideBase=" + var10000 + ", parts=" + Arrays.toString(this.parts) + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ItemReticle.class, ItemReticle::new).append(new KeyedCodec("HideBase", Codec.BOOLEAN), (itemReticle, o) -> itemReticle.hideBase = o, (itemReticle) -> itemReticle.hideBase).documentation("Specifies whether the base reticle should be hidden while the configured parts are shown.").add()).append(new KeyedCodec("Parts", new ArrayCodec(Codec.STRING, (x$0) -> new String[x$0])), (itemReticle, o) -> itemReticle.parts = o, (itemReticle) -> itemReticle.parts).documentation("A list of reticle parts that should be displayed for this configuration.").addValidator(Validators.nonNull()).addValidator(Validators.nonEmptyArray()).addValidator(CommonAssetValidator.UI_RETICLE_PARTS_ARRAY).add()).build();
      }
   }

   static class ItemReticleWithDuration extends ItemReticle {
      public static final BuilderCodec<ItemReticleWithDuration> CODEC;
      protected float duration = 0.25F;

      public ItemReticleWithDuration(boolean hideBase, String[] parts, float duration) {
         this.hideBase = hideBase;
         this.parts = parts;
         this.duration = duration;
      }

      public ItemReticleWithDuration() {
      }

      @Nonnull
      public com.hypixel.hytale.protocol.ItemReticle toPacket() {
         com.hypixel.hytale.protocol.ItemReticle packet = new com.hypixel.hytale.protocol.ItemReticle();
         packet.hideBase = this.hideBase;
         packet.parts = this.parts;
         packet.duration = this.duration;
         return packet;
      }

      @Nonnull
      public String toString() {
         float var10000 = this.duration;
         return "ItemReticleWithDuration{, duration=" + var10000 + "}" + super.toString();
      }

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ItemReticleWithDuration.class, ItemReticleWithDuration::new, ItemReticleConfig.ItemReticle.CODEC).append(new KeyedCodec("Duration", Codec.FLOAT), (itemReticle, value) -> itemReticle.duration = value, (itemReticle) -> itemReticle.duration).documentation("The duration (in seconds) this reticle configuration should be displayed for.").addValidator(Validators.greaterThan(0.0F)).add()).build();
      }
   }
}
