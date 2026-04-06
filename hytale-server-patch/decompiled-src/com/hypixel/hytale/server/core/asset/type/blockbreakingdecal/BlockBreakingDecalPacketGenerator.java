package com.hypixel.hytale.server.core.asset.type.blockbreakingdecal;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateBlockBreakingDecals;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.blockbreakingdecal.config.BlockBreakingDecal;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockBreakingDecalPacketGenerator extends DefaultAssetPacketGenerator<String, BlockBreakingDecal> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, BlockBreakingDecal> assetMap, Map<String, BlockBreakingDecal> assets) {
      UpdateBlockBreakingDecals packet = new UpdateBlockBreakingDecals();
      packet.type = UpdateType.Init;
      packet.blockBreakingDecals = (Map)assetMap.getAssetMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> ((BlockBreakingDecal)entry.getValue()).toPacket()));
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, BlockBreakingDecal> loadedAssets) {
      UpdateBlockBreakingDecals packet = new UpdateBlockBreakingDecals();
      packet.type = UpdateType.AddOrUpdate;
      packet.blockBreakingDecals = (Map)loadedAssets.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> ((BlockBreakingDecal)entry.getValue()).toPacket()));
      return packet;
   }

   @Nullable
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateBlockBreakingDecals packet = new UpdateBlockBreakingDecals();
      packet.type = UpdateType.Remove;
      packet.blockBreakingDecals = new Object2ObjectOpenHashMap();

      for(String string : removed) {
         packet.blockBreakingDecals.put(string, (Object)null);
      }

      return null;
   }
}
