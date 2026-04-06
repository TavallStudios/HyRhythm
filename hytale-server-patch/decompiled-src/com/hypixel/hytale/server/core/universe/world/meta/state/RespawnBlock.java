package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RespawnBlock implements Component<ChunkStore> {
   public static final BuilderCodec<RespawnBlock> CODEC;
   private UUID ownerUUID;

   public static ComponentType<ChunkStore, RespawnBlock> getComponentType() {
      return BlockModule.get().getRespawnBlockComponentType();
   }

   public RespawnBlock() {
   }

   public RespawnBlock(UUID ownerUUID) {
      this.ownerUUID = ownerUUID;
   }

   public UUID getOwnerUUID() {
      return this.ownerUUID;
   }

   public void setOwnerUUID(UUID ownerUUID) {
      this.ownerUUID = ownerUUID;
   }

   @Nullable
   public Component<ChunkStore> clone() {
      return new RespawnBlock(this.ownerUUID);
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(RespawnBlock.class, RespawnBlock::new).append(new KeyedCodec("OwnerUUID", Codec.UUID_BINARY), (respawnBlockState, uuid) -> respawnBlockState.ownerUUID = uuid, (respawnBlockState) -> respawnBlockState.ownerUUID).add()).build();
   }

   public static class OnRemove extends RefSystem<ChunkStore> {
      @Nonnull
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      public static final ComponentType<ChunkStore, RespawnBlock> COMPONENT_TYPE_RESPAWN_BLOCK = RespawnBlock.getComponentType();
      public static final ComponentType<ChunkStore, BlockModule.BlockStateInfo> COMPONENT_TYPE_BLOCK_STATE_INFO = BlockModule.BlockStateInfo.getComponentType();
      @Nonnull
      public static final Query<ChunkStore> QUERY;

      public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
      }

      public void onEntityRemove(@Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
         if (reason != RemoveReason.UNLOAD) {
            RespawnBlock respawnState = (RespawnBlock)commandBuffer.getComponent(ref, COMPONENT_TYPE_RESPAWN_BLOCK);

            assert respawnState != null;

            if (respawnState.ownerUUID != null) {
               BlockModule.BlockStateInfo blockStateInfoComponent = (BlockModule.BlockStateInfo)commandBuffer.getComponent(ref, COMPONENT_TYPE_BLOCK_STATE_INFO);

               assert blockStateInfoComponent != null;

               PlayerRef playerRef = Universe.get().getPlayer(respawnState.ownerUUID);
               if (playerRef == null) {
                  LOGGER.at(Level.WARNING).log("Failed to fetch player ref during removal of respawn block entity.");
               } else {
                  Player playerComponent = (Player)playerRef.getComponent(Player.getComponentType());
                  if (playerComponent == null) {
                     LOGGER.at(Level.WARNING).log("Failed to fetch player component during removal of respawn block entity.");
                  } else {
                     Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
                     if (chunkRef.isValid()) {
                        World world = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
                        PlayerWorldData playerWorldData = playerComponent.getPlayerConfigData().getPerWorldData(world.getName());
                        PlayerRespawnPointData[] respawnPoints = playerWorldData.getRespawnPoints();
                        if (respawnPoints == null) {
                           LOGGER.at(Level.WARNING).log("Failed to find valid respawn points for player " + String.valueOf(respawnState.ownerUUID) + " during removal of respawn block entity.");
                        } else {
                           WorldChunk worldChunkComponent = (WorldChunk)commandBuffer.getComponent(chunkRef, WorldChunk.getComponentType());

                           assert worldChunkComponent != null;

                           Vector3i blockPosition = new Vector3i(ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getX(), ChunkUtil.xFromBlockInColumn(blockStateInfoComponent.getIndex())), ChunkUtil.yFromBlockInColumn(blockStateInfoComponent.getIndex()), ChunkUtil.worldCoordFromLocalCoord(worldChunkComponent.getZ(), ChunkUtil.zFromBlockInColumn(blockStateInfoComponent.getIndex())));

                           for(int i = 0; i < respawnPoints.length; ++i) {
                              PlayerRespawnPointData respawnPoint = respawnPoints[i];
                              if (respawnPoint.getBlockPosition().equals(blockPosition)) {
                                 HytaleLogger.Api var10000 = LOGGER.at(Level.INFO);
                                 String var10001 = String.valueOf(respawnState.ownerUUID);
                                 var10000.log("Removing respawn point for player " + var10001 + " at position " + String.valueOf(blockPosition) + " due to respawn block removal.");
                                 playerWorldData.setRespawnPoints((PlayerRespawnPointData[])ArrayUtil.remove(respawnPoints, i));
                                 return;
                              }
                           }

                        }
                     }
                  }
               }
            }
         }
      }

      @Nullable
      public Query<ChunkStore> getQuery() {
         return QUERY;
      }

      static {
         QUERY = Query.<ChunkStore>and(COMPONENT_TYPE_RESPAWN_BLOCK, COMPONENT_TYPE_BLOCK_STATE_INFO);
      }
   }
}
