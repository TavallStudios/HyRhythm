package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.InvulnerableUpdate;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InvulnerableSystems {
   public static class QueueResource implements Resource<EntityStore> {
      private final Set<Ref<EntityStore>> queue = ConcurrentHashMap.newKeySet();

      public static ResourceType<EntityStore, QueueResource> getResourceType() {
         return EntityModule.get().getInvulnerableQueueResourceType();
      }

      @Nonnull
      public Resource<EntityStore> clone() {
         return new QueueResource();
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType) {
         this.componentType = componentType;
         this.query = Query.<EntityStore>and(componentType, Invulnerable.getComponentType());
      }

      @Nullable
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         super.tick(dt, systemIndex, store);
         ((QueueResource)store.getResource(InvulnerableSystems.QueueResource.getResourceType())).queue.clear();
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         EntityTrackerSystems.Visible visible = (EntityTrackerSystems.Visible)archetypeChunk.getComponent(index, this.componentType);
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (((QueueResource)commandBuffer.getResource(InvulnerableSystems.QueueResource.getResourceType())).queue.remove(ref)) {
            queueUpdatesFor(ref, visible.visibleTo);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, visible.newlyVisibleTo);
         }

      }

      private static void queueUpdatesFor(Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo) {
         InvulnerableUpdate update = new InvulnerableUpdate();

         for(EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }

      }
   }

   public static class EntityTrackerAddAndRemove extends RefChangeSystem<EntityStore, Invulnerable> {
      private final ComponentType<EntityStore, Invulnerable> invulnerableComponentType = Invulnerable.getComponentType();
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerAddAndRemove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.query = Query.<EntityStore>and(visibleComponentType, this.invulnerableComponentType);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public ComponentType<EntityStore, Invulnerable> componentType() {
         return this.invulnerableComponentType;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull Invulnerable component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         ((QueueResource)commandBuffer.getResource(InvulnerableSystems.QueueResource.getResourceType())).queue.add(ref);
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, Invulnerable oldComponent, @Nonnull Invulnerable newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull Invulnerable component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         for(EntityTrackerSystems.EntityViewer viewer : ((EntityTrackerSystems.Visible)store.getComponent(ref, this.visibleComponentType)).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.Invulnerable);
         }

      }
   }
}
