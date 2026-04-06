package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.SnapshotBuffer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class SnapshotSystems {
   public static long HISTORY_LENGTH_NS;
   private static final HytaleLogger LOGGER;

   static {
      HISTORY_LENGTH_NS = TimeUnit.MILLISECONDS.toNanos(500L);
      LOGGER = HytaleLogger.getLogger();
   }

   public static class Resize extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }

      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         World world = ((EntityStore)store.getExternalData()).getWorld();
         int tickLength = world.getTickStepNanos();
         SnapshotWorldInfo info = (SnapshotWorldInfo)store.getResource(SnapshotSystems.SnapshotWorldInfo.getResourceType());
         if (tickLength != info.tickLengthNanos || SnapshotSystems.HISTORY_LENGTH_NS != info.historyLength) {
            info.historyLength = SnapshotSystems.HISTORY_LENGTH_NS;
            info.tickLengthNanos = tickLength;
            int previousHistorySize = info.historySize;
            info.historySize = Math.max(1, (int)((info.historyLength + (long)tickLength - 1L) / (long)tickLength));
            super.tick(dt, systemIndex, store);
         }
      }

      public Query<EntityStore> getQuery() {
         return SnapshotBuffer.getComponentType();
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         SnapshotWorldInfo info = (SnapshotWorldInfo)store.getResource(SnapshotSystems.SnapshotWorldInfo.getResourceType());
         ((SnapshotBuffer)archetypeChunk.getComponent(index, SnapshotBuffer.getComponentType())).resize(info.historySize);
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }
   }

   public static class Add extends HolderSystem<EntityStore> {
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         SnapshotBuffer buffer = (SnapshotBuffer)holder.ensureAndGetComponent(SnapshotBuffer.getComponentType());
         buffer.resize(((SnapshotWorldInfo)store.getResource(SnapshotSystems.SnapshotWorldInfo.getResourceType())).historySize);
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      public Query<EntityStore> getQuery() {
         return TransformComponent.getComponentType();
      }
   }

   public static class Capture extends EntityTickingSystem<EntityStore> {
      private static final Set<Dependency<EntityStore>> DEPENDENCIES;
      @Nonnull
      private final Query<EntityStore> query = Query.<EntityStore>and(TransformComponent.getComponentType(), SnapshotBuffer.getComponentType());

      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         SnapshotWorldInfo info = (SnapshotWorldInfo)store.getResource(SnapshotSystems.SnapshotWorldInfo.getResourceType());
         ++info.currentTick;
         super.tick(dt, systemIndex, store);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         SnapshotBuffer buffer = (SnapshotBuffer)archetypeChunk.getComponent(index, SnapshotBuffer.getComponentType());
         TransformComponent transform = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());
         SnapshotWorldInfo info = (SnapshotWorldInfo)store.getResource(SnapshotSystems.SnapshotWorldInfo.getResourceType());
         buffer.storeSnapshot(info.currentTick, transform.getPosition(), transform.getRotation());
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      static {
         DEPENDENCIES = Set.of(new SystemDependency(Order.AFTER, Resize.class), new RootDependency(OrderPriority.CLOSEST));
      }
   }

   public static class SnapshotWorldInfo implements Resource<EntityStore> {
      private int tickLengthNanos = -1;
      private long historyLength = -1L;
      private int historySize = 1;
      private int currentTick = -1;

      public static ResourceType<EntityStore, SnapshotWorldInfo> getResourceType() {
         return EntityModule.get().getSnapshotWorldInfoResourceType();
      }

      public SnapshotWorldInfo() {
      }

      public SnapshotWorldInfo(int tickLengthNanos, long historyLength, int historySize, int currentTick) {
         this.tickLengthNanos = tickLengthNanos;
         this.historyLength = historyLength;
         this.historySize = historySize;
         this.currentTick = currentTick;
      }

      @Nonnull
      public Resource<EntityStore> clone() {
         return new SnapshotWorldInfo(this.tickLengthNanos, this.historyLength, this.historySize, this.currentTick);
      }
   }
}
