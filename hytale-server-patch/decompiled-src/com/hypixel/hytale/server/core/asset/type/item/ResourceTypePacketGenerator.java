package com.hypixel.hytale.server.core.asset.type.item;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateResourceTypes;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.item.config.ResourceType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class ResourceTypePacketGenerator extends DefaultAssetPacketGenerator<String, ResourceType> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, ResourceType> assetMap, @Nonnull Map<String, ResourceType> assets) {
      Map<String, ResourceType> assetsFromMap = assetMap.getAssetMap();
      if (assets.size() != assetsFromMap.size()) {
         throw new UnsupportedOperationException("Resource types can not handle partial init packets!!!");
      } else {
         UpdateResourceTypes packet = new UpdateResourceTypes();
         packet.type = UpdateType.Init;
         packet.resourceTypes = new Object2ObjectOpenHashMap();

         for(Map.Entry<String, ResourceType> entry : assets.entrySet()) {
            packet.resourceTypes.put((String)entry.getKey(), ((ResourceType)entry.getValue()).toPacket());
         }

         return packet;
      }
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, ResourceType> loadedAssets) {
      UpdateResourceTypes packet = new UpdateResourceTypes();
      packet.type = UpdateType.AddOrUpdate;
      packet.resourceTypes = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, ResourceType> entry : loadedAssets.entrySet()) {
         packet.resourceTypes.put((String)entry.getKey(), ((ResourceType)entry.getValue()).toPacket());
      }

      return packet;
   }

   @Nonnull
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateResourceTypes packet = new UpdateResourceTypes();
      packet.type = UpdateType.Remove;
      packet.resourceTypes = new Object2ObjectOpenHashMap();

      for(String key : removed) {
         packet.resourceTypes.put(key, (Object)null);
      }

      return packet;
   }
}
