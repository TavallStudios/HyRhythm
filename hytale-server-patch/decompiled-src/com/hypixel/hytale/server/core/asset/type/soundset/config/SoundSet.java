package com.hypixel.hytale.server.core.asset.type.soundset.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;

public class SoundSet implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, SoundSet>>, NetworkSerializable<com.hypixel.hytale.protocol.SoundSet> {
   public static final int EMPTY_ID = 0;
   public static final String EMPTY = "EMPTY";
   public static final SoundSet EMPTY_SOUND_SET = new SoundSet("EMPTY");
   public static final AssetBuilderCodec<String, SoundSet> CODEC;
   public static final Codec<String> CHILD_ASSET_CODEC;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, SoundSet, IndexedLookupTableAssetMap<String, SoundSet>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected Map<String, String> soundEventIds = Collections.emptyMap();
   protected transient Object2IntMap<String> soundEventIndices = Object2IntMaps.emptyMap();
   @Nonnull
   protected SoundCategory category;
   private SoftReference<com.hypixel.hytale.protocol.SoundSet> cachedPacket;

   public static AssetStore<String, SoundSet, IndexedLookupTableAssetMap<String, SoundSet>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, SoundSet, IndexedLookupTableAssetMap<String, SoundSet>>getAssetStore(SoundSet.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, SoundSet> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   public SoundSet(String id, Map<String, String> soundEventIds, SoundCategory category) {
      this.category = SoundCategory.SFX;
      this.id = id;
      this.soundEventIds = soundEventIds;
      this.category = category;
   }

   public SoundSet(String id) {
      this.category = SoundCategory.SFX;
      this.id = id;
   }

   protected SoundSet() {
      this.category = SoundCategory.SFX;
   }

   public String getId() {
      return this.id;
   }

   public Map<String, String> getSoundEventIds() {
      return this.soundEventIds;
   }

   public Object2IntMap<String> getSoundEventIndices() {
      return this.soundEventIndices;
   }

   protected void processConfig() {
      if (!this.soundEventIds.isEmpty()) {
         this.soundEventIndices = new Object2IntOpenHashMap();

         for(Map.Entry<String, String> entry : this.soundEventIds.entrySet()) {
            this.soundEventIndices.put((String)entry.getKey(), SoundEvent.getAssetMap().getIndex((String)entry.getValue()));
         }
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "SoundSet{id='" + var10000 + "', soundEventIds=" + String.valueOf(this.soundEventIds) + ", soundEventIndices=" + String.valueOf(this.soundEventIndices) + ", category=" + String.valueOf(this.category) + "}";
   }

   @Nonnull
   public com.hypixel.hytale.protocol.SoundSet toPacket() {
      com.hypixel.hytale.protocol.SoundSet cached = this.cachedPacket == null ? null : (com.hypixel.hytale.protocol.SoundSet)this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.SoundSet packet = new com.hypixel.hytale.protocol.SoundSet();
         packet.id = this.id;
         packet.sounds = this.soundEventIndices;
         packet.category = this.category;
         this.cachedPacket = new SoftReference(packet);
         return packet;
      }
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(SoundSet.class, SoundSet::new, Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (asset, data) -> asset.data = data, (asset) -> asset.data).appendInherited(new KeyedCodec("SoundEvents", MapCodec.STRING_HASH_MAP_CODEC), (soundSet, s) -> soundSet.soundEventIds = s, (soundSet) -> soundSet.soundEventIds, (soundSet, parent) -> soundSet.soundEventIds = parent.soundEventIds).addValidator(Validators.nonNull()).addValidator(SoundEvent.VALIDATOR_CACHE.getMapValueValidator()).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("Category", new EnumCodec(SoundCategory.class)), (soundSet, s) -> soundSet.category = s, (soundSet) -> soundSet.category, (soundSet, parent) -> soundSet.category = parent.category).addValidator(Validators.nonNull()).add()).afterDecode(SoundSet::processConfig)).build();
      CHILD_ASSET_CODEC = new ContainedAssetCodec(SoundSet.class, CODEC);
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(SoundSet::getAssetStore));
   }
}
