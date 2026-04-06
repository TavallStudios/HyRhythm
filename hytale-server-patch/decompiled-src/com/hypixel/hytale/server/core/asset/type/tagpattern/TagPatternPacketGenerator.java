package com.hypixel.hytale.server.core.asset.type.tagpattern;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTagPatterns;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.tagpattern.config.TagPattern;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class TagPatternPacketGenerator extends SimpleAssetPacketGenerator<String, TagPattern, IndexedLookupTableAssetMap<String, TagPattern>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, TagPattern> assetMap, @Nonnull Map<String, TagPattern> assets) {
      UpdateTagPatterns packet = new UpdateTagPatterns();
      packet.type = UpdateType.Init;
      packet.patterns = new Int2ObjectOpenHashMap(assets.size());

      for(Map.Entry<String, TagPattern> entry : assets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.patterns.put(index, (com.hypixel.hytale.protocol.TagPattern)((TagPattern)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, TagPattern> assetMap, @Nonnull Map<String, TagPattern> loadedAssets) {
      UpdateTagPatterns packet = new UpdateTagPatterns();
      packet.type = UpdateType.AddOrUpdate;
      packet.patterns = new Int2ObjectOpenHashMap(loadedAssets.size());

      for(Map.Entry<String, TagPattern> entry : loadedAssets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.patterns.put(index, (com.hypixel.hytale.protocol.TagPattern)((TagPattern)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, TagPattern> assetMap, @Nonnull Set<String> removed) {
      UpdateTagPatterns packet = new UpdateTagPatterns();
      packet.type = UpdateType.Remove;
      packet.patterns = new Int2ObjectOpenHashMap(removed.size());

      for(String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.patterns.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
