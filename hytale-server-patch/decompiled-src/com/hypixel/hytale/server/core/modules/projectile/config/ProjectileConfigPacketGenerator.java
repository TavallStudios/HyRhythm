package com.hypixel.hytale.server.core.modules.projectile.config;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateProjectileConfigs;
import com.hypixel.hytale.server.core.asset.packet.DefaultAssetPacketGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProjectileConfigPacketGenerator extends DefaultAssetPacketGenerator<String, ProjectileConfig> {
   @Nonnull
   public ToClientPacket generateInitPacket(@Nonnull DefaultAssetMap<String, ProjectileConfig> assetMap, Map<String, ProjectileConfig> assets) {
      UpdateProjectileConfigs packet = new UpdateProjectileConfigs();
      packet.type = UpdateType.Init;
      Map<String, com.hypixel.hytale.protocol.ProjectileConfig> map = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, ProjectileConfig> entry : assetMap.getAssetMap().entrySet()) {
         if (map.put((String)entry.getKey(), ((ProjectileConfig)entry.getValue()).toPacket()) != null) {
            throw new IllegalStateException("Duplicate key");
         }
      }

      packet.configs = map;
      return packet;
   }

   @Nonnull
   public ToClientPacket generateUpdatePacket(@Nonnull Map<String, ProjectileConfig> loadedAssets) {
      UpdateProjectileConfigs packet = new UpdateProjectileConfigs();
      packet.type = UpdateType.AddOrUpdate;
      Map<String, com.hypixel.hytale.protocol.ProjectileConfig> map = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, ProjectileConfig> entry : loadedAssets.entrySet()) {
         if (map.put((String)entry.getKey(), ((ProjectileConfig)entry.getValue()).toPacket()) != null) {
            throw new IllegalStateException("Duplicate key");
         }
      }

      packet.configs = map;
      return packet;
   }

   @Nullable
   public ToClientPacket generateRemovePacket(@Nonnull Set<String> removed) {
      UpdateProjectileConfigs packet = new UpdateProjectileConfigs();
      packet.type = UpdateType.Remove;
      packet.removedConfigs = (String[])removed.toArray((x$0) -> new String[x$0]);
      return packet;
   }
}
