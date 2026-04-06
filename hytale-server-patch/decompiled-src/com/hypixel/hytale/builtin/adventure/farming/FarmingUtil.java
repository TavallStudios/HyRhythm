package com.hypixel.hytale.builtin.adventure.farming;

import com.hypixel.hytale.builtin.adventure.farming.states.FarmingBlock;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.GrowthModifierAsset;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.metadata.CapturedNPCMetadata;
import java.time.Instant;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FarmingUtil {
   private static final int MAX_SECONDS_BETWEEN_TICKS = 15;
   private static final int BETWEEN_RANDOM = 10;

   public static void tickFarming(@Nonnull CommandBuffer<ChunkStore> commandBuffer, @Nonnull BlockChunk blockChunk, @Nonnull BlockSection blockSection, @Nonnull Ref<ChunkStore> sectionRef, @Nonnull Ref<ChunkStore> blockRef, @Nonnull FarmingBlock farmingBlock, int x, int y, int z, boolean initialTick) {
      World world = ((ChunkStore)commandBuffer.getExternalData()).getWorld();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      WorldTimeResource worldTimeResource = (WorldTimeResource)entityStore.getResource(WorldTimeResource.getResourceType());
      Instant currentTime = worldTimeResource.getGameTime();
      BlockType blockType = farmingBlock.getPreviousBlockType() != null ? (BlockType)BlockType.getAssetMap().getAsset(farmingBlock.getPreviousBlockType()) : (BlockType)BlockType.getAssetMap().getAsset(blockSection.get(x, y, z));
      if (blockType != null) {
         if (blockType.getFarming() != null) {
            FarmingData farmingConfig = blockType.getFarming();
            if (farmingConfig.getStages() != null) {
               float currentProgress = farmingBlock.getGrowthProgress();
               int currentStage = (int)currentProgress;
               String currentStageSet = farmingBlock.getCurrentStageSet();
               FarmingStageData[] stages = currentStageSet != null ? (FarmingStageData[])farmingConfig.getStages().get(currentStageSet) : null;
               if (stages == null) {
                  currentStageSet = farmingConfig.getStartingStageSet();
                  if (currentStageSet == null) {
                     return;
                  }

                  farmingBlock.setCurrentStageSet(currentStageSet);
                  stages = (FarmingStageData[])farmingConfig.getStages().get(currentStageSet);
                  blockChunk.markNeedsSaving();
               }

               if (stages != null) {
                  if (currentStage < 0) {
                     currentStage = 0;
                     currentProgress = 0.0F;
                     farmingBlock.setGrowthProgress(0.0F);
                  }

                  if (currentStage >= stages.length) {
                     commandBuffer.removeEntity(blockRef, RemoveReason.REMOVE);
                  } else {
                     long remainingTimeSeconds = currentTime.getEpochSecond() - farmingBlock.getLastTickGameTime().getEpochSecond();
                     ChunkSection chunkSectionComponent = (ChunkSection)commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
                     if (chunkSectionComponent != null) {
                        int worldX = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getX(), x);
                        int worldY = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getY(), y);
                        int worldZ = ChunkUtil.worldCoordFromLocalCoord(chunkSectionComponent.getZ(), z);

                        while(currentStage < stages.length) {
                           FarmingStageData stage = stages[currentStage];
                           if (stage.shouldStop(commandBuffer, sectionRef, blockRef, x, y, z)) {
                              blockChunk.markNeedsSaving();
                              farmingBlock.setGrowthProgress((float)stages.length);
                              commandBuffer.removeEntity(blockRef, RemoveReason.REMOVE);
                              break;
                           }

                           Rangef range = stage.getDuration();
                           if (range == null) {
                              blockChunk.markNeedsSaving();
                              commandBuffer.removeEntity(blockRef, RemoveReason.REMOVE);
                              break;
                           }

                           double rand = HashUtil.random((long)farmingBlock.getGeneration(), (long)worldX, (long)worldY, (long)worldZ);
                           double baseDuration = (double)range.min + (double)(range.max - range.min) * rand;
                           long remainingDurationSeconds = Math.round(baseDuration * (1.0 - (double)currentProgress % 1.0));
                           double growthMultiplier = 1.0;
                           if (farmingConfig.getGrowthModifiers() != null) {
                              for(String modifierName : farmingConfig.getGrowthModifiers()) {
                                 GrowthModifierAsset modifierAsset = (GrowthModifierAsset)GrowthModifierAsset.getAssetMap().getAsset(modifierName);
                                 if (modifierAsset != null) {
                                    growthMultiplier *= modifierAsset.getCurrentGrowthMultiplier(commandBuffer, sectionRef, blockRef, x, y, z, initialTick);
                                 }
                              }
                           }

                           remainingDurationSeconds = Math.round((double)remainingDurationSeconds / growthMultiplier);
                           if (remainingTimeSeconds < remainingDurationSeconds) {
                              currentProgress += (float)((double)remainingTimeSeconds / (baseDuration / growthMultiplier));
                              farmingBlock.setGrowthProgress(currentProgress);
                              long nextGrowthInNanos = (remainingDurationSeconds - remainingTimeSeconds) * 1000000000L;
                              long randCap = (long)((15.0 + 10.0 * HashUtil.random((long)farmingBlock.getGeneration() ^ 3405692655L, (long)worldX, (long)worldY, (long)worldZ)) * (double)world.getTps() * WorldTimeResource.getSecondsPerTick(world) * 1.0E9);
                              long cappedNextGrowthInNanos = Math.min(nextGrowthInNanos, randCap);
                              blockSection.scheduleTick(ChunkUtil.indexBlock(x, y, z), currentTime.plusNanos(cappedNextGrowthInNanos));
                              break;
                           }

                           remainingTimeSeconds -= remainingDurationSeconds;
                           ++currentStage;
                           currentProgress = (float)currentStage;
                           farmingBlock.setGrowthProgress(currentProgress);
                           blockChunk.markNeedsSaving();
                           farmingBlock.setGeneration(farmingBlock.getGeneration() + 1);
                           if (currentStage >= stages.length) {
                              if (stages[currentStage - 1].implementsShouldStop()) {
                                 currentStage = stages.length - 1;
                                 farmingBlock.setGrowthProgress((float)currentStage);
                                 stages[currentStage].apply(commandBuffer, sectionRef, blockRef, x, y, z, stages[currentStage]);
                              } else {
                                 farmingBlock.setGrowthProgress((float)stages.length);
                                 commandBuffer.removeEntity(blockRef, RemoveReason.REMOVE);
                              }
                           } else {
                              farmingBlock.setExecutions(0);
                              stages[currentStage].apply(commandBuffer, sectionRef, blockRef, x, y, z, stages[currentStage - 1]);
                           }
                        }

                        farmingBlock.setLastTickGameTime(currentTime);
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean harvest(@Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull BlockType blockType, int rotationIndex, @Nonnull Vector3i blockPosition) {
      return world.getGameplayConfig().getWorldConfig().isBlockGatheringAllowed() ? harvest0(componentAccessor, ref, blockType, rotationIndex, blockPosition) : false;
   }

   @Nullable
   public static CapturedNPCMetadata generateCapturedNPCMetadata(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> entityRef, String npcNameKey) {
      PersistentModel persistentModel = (PersistentModel)componentAccessor.getComponent(entityRef, PersistentModel.getComponentType());
      if (persistentModel == null) {
         return null;
      } else {
         ModelAsset modelAsset = (ModelAsset)ModelAsset.getAssetMap().getAsset(persistentModel.getModelReference().getModelAssetId());
         CapturedNPCMetadata meta = new CapturedNPCMetadata();
         if (modelAsset != null) {
            meta.setIconPath(modelAsset.getIcon());
         }

         meta.setNpcNameKey(npcNameKey);
         return meta;
      }
   }

   protected static boolean harvest0(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull BlockType blockType, int rotationIndex, @Nonnull Vector3i blockPosition) {
      FarmingData farmingConfig = blockType.getFarming();
      boolean isFarmable = true;
      if (farmingConfig == null || farmingConfig.getStages() == null) {
         isFarmable = false;
      }

      if (blockType.getGathering().getHarvest() == null) {
         return false;
      } else {
         World world = ((EntityStore)store.getExternalData()).getWorld();
         Vector3d centerPosition = new Vector3d();
         blockType.getBlockCenter(rotationIndex, centerPosition);
         centerPosition.add(blockPosition);
         if (isFarmable && farmingConfig.getStageSetAfterHarvest() != null) {
            giveDrops(store, ref, centerPosition, blockType);
            Map<String, FarmingStageData[]> stageSets = farmingConfig.getStages();
            FarmingStageData[] stages = (FarmingStageData[])stageSets.get(farmingConfig.getStartingStageSet());
            if (stages == null) {
               return false;
            } else {
               int currentStageIndex = stages.length - 1;
               FarmingStageData previousStage = stages[currentStageIndex];
               String newStageSet = farmingConfig.getStageSetAfterHarvest();
               FarmingStageData[] newStages = (FarmingStageData[])stageSets.get(newStageSet);
               if (newStages != null && newStages.length != 0) {
                  Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
                  Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z));
                  if (chunkRef == null) {
                     return false;
                  } else {
                     BlockComponentChunk blockComponentChunk = (BlockComponentChunk)chunkStore.getComponent(chunkRef, BlockComponentChunk.getComponentType());
                     if (blockComponentChunk == null) {
                        return false;
                     } else {
                        Instant now = ((WorldTimeResource)((EntityStore)store.getExternalData()).getWorld().getEntityStore().getStore().getResource(WorldTimeResource.getResourceType())).getGameTime();
                        int blockIndexColumn = ChunkUtil.indexBlockInColumn(blockPosition.x, blockPosition.y, blockPosition.z);
                        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndexColumn);
                        FarmingBlock farmingBlock;
                        if (blockRef == null) {
                           Holder<ChunkStore> blockEntity = ChunkStore.REGISTRY.newHolder();
                           blockEntity.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(blockIndexColumn, chunkRef));
                           farmingBlock = new FarmingBlock();
                           farmingBlock.setLastTickGameTime(now);
                           farmingBlock.setCurrentStageSet(newStageSet);
                           blockEntity.addComponent(FarmingBlock.getComponentType(), farmingBlock);
                           blockRef = chunkStore.addEntity(blockEntity, AddReason.SPAWN);
                        } else {
                           farmingBlock = (FarmingBlock)chunkStore.ensureAndGetComponent(blockRef, FarmingBlock.getComponentType());
                        }

                        farmingBlock.setCurrentStageSet(newStageSet);
                        farmingBlock.setGrowthProgress(0.0F);
                        farmingBlock.setExecutions(0);
                        farmingBlock.setGeneration(farmingBlock.getGeneration() + 1);
                        farmingBlock.setLastTickGameTime(now);
                        Ref<ChunkStore> sectionRef = world.getChunkStore().getChunkSectionReference(ChunkUtil.chunkCoordinate(blockPosition.x), ChunkUtil.chunkCoordinate(blockPosition.y), ChunkUtil.chunkCoordinate(blockPosition.z));
                        if (sectionRef == null) {
                           return false;
                        } else if (blockRef == null) {
                           return false;
                        } else {
                           BlockSection section = (BlockSection)chunkStore.getComponent(sectionRef, BlockSection.getComponentType());
                           if (section != null) {
                              int blockIndex = ChunkUtil.indexBlock(blockPosition.x, blockPosition.y, blockPosition.z);
                              section.scheduleTick(blockIndex, now);
                           }

                           newStages[0].apply(chunkStore, sectionRef, blockRef, blockPosition.x, blockPosition.y, blockPosition.z, previousStage);
                           return true;
                        }
                     }
                  }
               } else {
                  WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z));
                  if (chunk != null) {
                     chunk.breakBlock(blockPosition.x, blockPosition.y, blockPosition.z);
                  }

                  return false;
               }
            }
         } else {
            giveDrops(store, ref, centerPosition, blockType);
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z));
            if (chunk != null) {
               chunk.breakBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            }

            return true;
         }
      }
   }

   protected static void giveDrops(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull Vector3d origin, @Nonnull BlockType blockType) {
      HarvestingDropType harvest = blockType.getGathering().getHarvest();
      String itemId = harvest.getItemId();
      String dropListId = harvest.getDropListId();

      for(ItemStack itemStack : BlockHarvestUtils.getDrops(blockType, 1, itemId, dropListId)) {
         ItemUtils.interactivelyPickupItem(ref, itemStack, origin, store);
      }

   }
}
