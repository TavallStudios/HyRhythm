package com.hypixel.hytale.builtin.randomtick;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.RandomTickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RandomTickSystem extends EntityTickingSystem<ChunkStore> {
   private final ComponentType<ChunkStore, BlockSection> blockSelectionComponentType = BlockSection.getComponentType();
   private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType = ChunkSection.getComponentType();
   private final Query<ChunkStore> query;

   public RandomTickSystem() {
      this.query = Query.<ChunkStore>and(this.blockSelectionComponentType, this.chunkSectionComponentType);
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
      BlockSection blockSection = (BlockSection)archetypeChunk.getComponent(index, this.blockSelectionComponentType);

      assert blockSection != null;

      if (!blockSection.isSolidAir()) {
         ChunkSection chunkSection = (ChunkSection)archetypeChunk.getComponent(index, this.chunkSectionComponentType);

         assert chunkSection != null;

         RandomTick config = (RandomTick)commandBuffer.getResource(RandomTick.getResourceType());
         World world = ((ChunkStore)store.getExternalData()).getWorld();
         int interval = '耀' / config.getBlocksPerSectionPerTickStable();
         long baseSeed = HashUtil.hash(world.getTick() / (long)interval, (long)chunkSection.getX(), (long)chunkSection.getY(), (long)chunkSection.getZ());
         long randomSeed = (baseSeed << 1 | 1L) & 32767L;
         long randomSeed2 = baseSeed >> 16 & 32767L;
         long startIndex = world.getTick() % (long)interval * (long)config.getBlocksPerSectionPerTickStable();
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

         for(int i = 0; i < config.getBlocksPerSectionPerTickStable(); ++i) {
            int blockIndex = (int)((startIndex + (long)i) * randomSeed + randomSeed2 & 32767L);
            int localX = ChunkUtil.xFromIndex(blockIndex);
            int localY = ChunkUtil.yFromIndex(blockIndex);
            int localZ = ChunkUtil.zFromIndex(blockIndex);
            int blockId = blockSection.get(blockIndex);
            if (blockId != 0) {
               BlockType blockType = assetMap.getAsset(blockId);
               if (blockType != null) {
                  RandomTickProcedure randomTickProcedure = blockType.getRandomTickProcedure();
                  if (randomTickProcedure != null) {
                     randomTickProcedure.onRandomTick(store, commandBuffer, blockSection, ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), localX), ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), localY), ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), localZ), blockId, blockType);
                  }
               }
            }
         }

         Random rng = config.getRandom();

         for(int i = 0; i < config.getBlocksPerSectionPerTickUnstable(); ++i) {
            int blockIndex = rng.nextInt(32768);
            int localX = ChunkUtil.xFromIndex(blockIndex);
            int localY = ChunkUtil.yFromIndex(blockIndex);
            int localZ = ChunkUtil.zFromIndex(blockIndex);
            int blockId = blockSection.get(blockIndex);
            if (blockId != 0) {
               BlockType blockType = assetMap.getAsset(blockId);
               if (blockType != null) {
                  RandomTickProcedure randomTickProcedure = blockType.getRandomTickProcedure();
                  if (randomTickProcedure != null) {
                     randomTickProcedure.onRandomTick(store, commandBuffer, blockSection, ChunkUtil.worldCoordFromLocalCoord(chunkSection.getX(), localX), ChunkUtil.worldCoordFromLocalCoord(chunkSection.getY(), localY), ChunkUtil.worldCoordFromLocalCoord(chunkSection.getZ(), localZ), blockId, blockType);
                  }
               }
            }
         }

      }
   }

   @Nullable
   public Query<ChunkStore> getQuery() {
      return this.query;
   }
}
