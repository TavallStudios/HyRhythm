package com.hypixel.hytale.server.core.asset.type.ambiencefx;

import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateAmbienceFX;
import com.hypixel.hytale.server.core.asset.packet.SimpleAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.ambiencefx.config.AmbienceFX;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class AmbienceFXPacketGenerator extends SimpleAssetPacketGenerator<String, AmbienceFX, IndexedAssetMap<String, AmbienceFX>> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Map<String, AmbienceFX> assets) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.Init;
      packet.ambienceFX = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, AmbienceFX> entry : assets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, ((AmbienceFX)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Map<String, AmbienceFX> loadedAssets) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.AddOrUpdate;
      packet.ambienceFX = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, AmbienceFX> entry : loadedAssets.entrySet()) {
         String key = (String)entry.getKey();
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, ((AmbienceFX)entry.getValue()).toPacket());
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull IndexedAssetMap<String, AmbienceFX> assetMap, @Nonnull Set<String> removed) {
      UpdateAmbienceFX packet = new UpdateAmbienceFX();
      packet.type = UpdateType.Remove;
      packet.ambienceFX = new Object2ObjectOpenHashMap();

      for(String key : removed) {
         int index = assetMap.getIndex(key);
         if (index == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         packet.ambienceFX.put(index, (Object)null);
      }

      packet.maxId = assetMap.getNextIndex();
      return packet;
   }
}
