package com.hypixel.hytale.server.core.asset.type.audiocategory;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAudioCategories;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.audiocategory.config.AudioCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class AudioCategoryPacketGenerator extends SimpleAssetPacketGenerator<String, AudioCategory, IndexedLookupTableAssetMap<String, AudioCategory>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Map<String, AudioCategory> assets) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.Init;
      packet.categories = new Int2ObjectOpenHashMap(assets.size());

      for(Map.Entry<String, AudioCategory> entry : assets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, ((AudioCategory)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Map<String, AudioCategory> loadedAssets) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.AddOrUpdate;
      packet.categories = new Int2ObjectOpenHashMap(loadedAssets.size());

      for(Map.Entry<String, AudioCategory> entry : loadedAssets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, ((AudioCategory)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, AudioCategory> assetMap, @Nonnull Set<String> removed) {
      UpdateAudioCategories packet = new UpdateAudioCategories();
      packet.type = UpdateType.Remove;
      packet.categories = new Int2ObjectOpenHashMap(removed.size());

      for(String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.categories.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
