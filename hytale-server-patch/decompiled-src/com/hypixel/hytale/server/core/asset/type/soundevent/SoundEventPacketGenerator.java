package com.hypixel.hytale.server.core.asset.type.soundevent;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateSoundEvents;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class SoundEventPacketGenerator extends SimpleAssetPacketGenerator<String, SoundEvent, IndexedLookupTableAssetMap<String, SoundEvent>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedLookupTableAssetMap<String, SoundEvent> assetMap, @Nonnull Map<String, SoundEvent> assets) {
      UpdateSoundEvents packet = new UpdateSoundEvents();
      packet.type = UpdateType.Init;
      packet.soundEvents = new Int2ObjectOpenHashMap(assets.size());

      for(Map.Entry<String, SoundEvent> entry : assets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.soundEvents.put(index, ((SoundEvent)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedLookupTableAssetMap<String, SoundEvent> assetMap, @Nonnull Map<String, SoundEvent> loadedAssets) {
      UpdateSoundEvents packet = new UpdateSoundEvents();
      packet.type = UpdateType.AddOrUpdate;
      packet.soundEvents = new Int2ObjectOpenHashMap(loadedAssets.size());

      for(Map.Entry<String, SoundEvent> entry : loadedAssets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.soundEvents.put(index, ((SoundEvent)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedLookupTableAssetMap<String, SoundEvent> assetMap, @Nonnull Set<String> removed) {
      UpdateSoundEvents packet = new UpdateSoundEvents();
      packet.type = UpdateType.Remove;
      packet.soundEvents = new Int2ObjectOpenHashMap(removed.size());

      for(String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.soundEvents.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
