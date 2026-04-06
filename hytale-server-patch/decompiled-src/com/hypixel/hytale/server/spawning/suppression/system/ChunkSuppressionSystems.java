package com.hypixel.hytale.server.spawning.suppression.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionEntry;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionQueue;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionController;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkSuppressionSystems {
   public static class ChunkAdded extends RefSystem<ChunkStore> {
      private static final ComponentType<ChunkStore, BlockChunk> COMPONENT_TYPE = BlockChunk.getComponentType();
      private final ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType;
      private final ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType;

      public ChunkAdded(ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType, ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType) {
         this.chunkSuppressionEntryComponentType = chunkSuppressionEntryComponentType;
         this.spawnSuppressionControllerResourceType = spawnSuppressionControllerResourceType;
      }

      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE;
      }

      public void onEntityAdded(@Nonnull Ref<ChunkStore> reference, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
         World world = ((ChunkStore)store.getExternalData()).getWorld();
         SpawnSuppressionController spawnSuppressionController = (SpawnSuppressionController)world.getEntityStore().getStore().getResource(this.spawnSuppressionControllerResourceType);
         BlockChunk blockChunk = (BlockChunk)commandBuffer.getComponent(reference, COMPONENT_TYPE);
         long index = blockChunk.getIndex();
         ChunkSuppressionEntry entry = (ChunkSuppressionEntry)spawnSuppressionController.getChunkSuppressionMap().get(index);
         if (entry != null) {
            commandBuffer.addComponent(reference, this.chunkSuppressionEntryComponentType, entry);
            SpawningPlugin.get().getLogger().at(Level.FINEST).log("Annotated chunk index %s on load", index);
         }

      }

      public void onEntityRemove(@Nonnull Ref<ChunkStore> reference, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
      }
   }

   public static class Ticking extends TickingSystem<ChunkStore> {
      private final ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType;
      private final ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType;

      public Ticking(ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType, ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType) {
         this.chunkSuppressionEntryComponentType = chunkSuppressionEntryComponentType;
         this.chunkSuppressionQueueResourceType = chunkSuppressionQueueResourceType;
      }

      public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
         ChunkSuppressionQueue queue = (ChunkSuppressionQueue)store.getResource(this.chunkSuppressionQueueResourceType);
         List<Map.Entry<Ref<ChunkStore>, ChunkSuppressionEntry>> addQueue = queue.getToAdd();
         if (!addQueue.isEmpty()) {
            for(int i = 0; i < addQueue.size(); ++i) {
               Map.Entry<Ref<ChunkStore>, ChunkSuppressionEntry> entry = (Map.Entry)addQueue.get(i);
               Ref<ChunkStore> ref = (Ref)entry.getKey();
               if (ref.isValid()) {
                  store.putComponent(ref, this.chunkSuppressionEntryComponentType, (ChunkSuppressionEntry)entry.getValue());
                  SpawningPlugin.get().getLogger().at(Level.FINEST).log("Annotated chunk %s from queue", ref);
               }
            }

            addQueue.clear();
         }

         List<Ref<ChunkStore>> removeQueue = queue.getToRemove();
         if (!removeQueue.isEmpty()) {
            for(int i = 0; i < removeQueue.size(); ++i) {
               Ref<ChunkStore> ref = (Ref)removeQueue.get(i);
               if (ref.isValid()) {
                  store.tryRemoveComponent(ref, this.chunkSuppressionEntryComponentType);
                  SpawningPlugin.get().getLogger().at(Level.FINEST).log("Removed annotation from chunk %s from queue", ref);
               }
            }

            removeQueue.clear();
         }

      }
   }
}
