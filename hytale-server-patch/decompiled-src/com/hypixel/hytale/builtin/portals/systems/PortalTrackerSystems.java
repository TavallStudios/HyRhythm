package com.hypixel.hytale.builtin.portals.systems;

import com.hypixel.hytale.builtin.instances.removal.InstanceDataResource;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.interface_.PortalDef;
import com.hypixel.hytale.protocol.packets.interface_.PortalState;
import com.hypixel.hytale.protocol.packets.interface_.UpdatePortal;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PortalTrackerSystems {
   private PortalTrackerSystems() {
   }

   public static class TrackerSystem extends RefSystem<EntityStore> {
      public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         PortalWorld portalWorld = (PortalWorld)store.getResource(PortalWorld.getResourceType());
         if (portalWorld.exists()) {
            World world = ((EntityStore)store.getExternalData()).getWorld();
            PlayerRef playerRef = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            if (portalWorld.getSeesUi().add(playerRef.getUuid())) {
               UpdatePortal packet = portalWorld.createFullPacket(world);
               playerRef.getPacketHandler().write((ToClientPacket)packet);
            }

         }
      }

      public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         PortalWorld portalWorld = (PortalWorld)store.getResource(PortalWorld.getResourceType());
         if (portalWorld.exists()) {
            PlayerRef playerRef = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());
            playerRef.getPacketHandler().write((ToClientPacket)(new UpdatePortal((PortalState)null, (PortalDef)null)));
            portalWorld.getSeesUi().remove(playerRef.getUuid());
         }
      }

      public Query<EntityStore> getQuery() {
         return Query.<EntityStore>and(Player.getComponentType(), PlayerRef.getComponentType());
      }
   }

   public static class UiTickingSystem extends DelayedEntitySystem<EntityStore> {
      public UiTickingSystem() {
         super(1.0F);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         PortalWorld portalWorld = (PortalWorld)store.getResource(PortalWorld.getResourceType());
         if (portalWorld.exists()) {
            World world = ((EntityStore)store.getExternalData()).getWorld();
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            InstanceDataResource instanceData = (InstanceDataResource)chunkStore.getResource(InstanceDataResource.getResourceType());
            Instant timeout = instanceData.getTimeoutTimer();
            if (timeout != null) {
               PlayerRef playerRef = (PlayerRef)archetypeChunk.getComponent(index, PlayerRef.getComponentType());
               UpdatePortal packet = portalWorld.getSeesUi().add(playerRef.getUuid()) ? portalWorld.createFullPacket(world) : portalWorld.createUpdatePacket(world);
               playerRef.getPacketHandler().write((ToClientPacket)packet);
            }
         }
      }

      @Nullable
      public Query<EntityStore> getQuery() {
         return Query.<EntityStore>and(Player.getComponentType(), PlayerRef.getComponentType());
      }
   }
}
