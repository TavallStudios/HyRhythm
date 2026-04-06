package com.hypixel.hytale.server.core.entity.movement;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.MovementStatesUpdate;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MovementStatesSystems {
   public static class AddSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentComponentType;

      public AddSystem(@Nonnull ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentComponentType) {
         this.movementStatesComponentComponentType = movementStatesComponentComponentType;
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.movementStatesComponentComponentType);
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return AllLegacyLivingEntityTypesQuery.INSTANCE;
      }
   }

   public static class PlayerInitSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType;

      public PlayerInitSystem(@Nonnull ComponentType<EntityStore, Player> playerComponentType, @Nonnull ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType) {
         this.playerComponentType = playerComponentType;
         this.movementStatesComponentType = movementStatesComponentType;
         this.query = Query.<EntityStore>and(playerComponentType, movementStatesComponentType);
      }

      public void onEntityAdded(@Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
         Player playerComponent = (Player)store.getComponent(ref, this.playerComponentType);

         assert playerComponent != null;

         MovementStatesComponent movementStatesComponent = (MovementStatesComponent)store.getComponent(ref, this.movementStatesComponentType);

         assert movementStatesComponent != null;

         PlayerWorldData perWorldData = playerComponent.getPlayerConfigData().getPerWorldData(world.getName());
         SavedMovementStates movementStates = perWorldData.getLastMovementStates();
         playerComponent.applyMovementStates(ref, movementStates != null ? movementStates : new SavedMovementStates(), movementStatesComponent.getMovementStates(), store);
      }

      public void onEntityRemove(@Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TickingSystem(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, @Nonnull ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.query = Query.<EntityStore>and(movementStatesComponentComponentType, visibleComponentType);
         this.movementStatesComponentComponentType = movementStatesComponentComponentType;
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

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         EntityTrackerSystems.Visible visibleComponent = (EntityTrackerSystems.Visible)archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         MovementStatesComponent movementStatesComponent = (MovementStatesComponent)archetypeChunk.getComponent(index, this.movementStatesComponentComponentType);

         assert movementStatesComponent != null;

         MovementStates newMovementStates = movementStatesComponent.getMovementStates();
         MovementStates sentMovementStates = movementStatesComponent.getSentMovementStates();
         if (!newMovementStates.equals(sentMovementStates)) {
            copyMovementStatesFrom(newMovementStates, sentMovementStates);
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), visibleComponent.visibleTo, movementStatesComponent);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), visibleComponent.newlyVisibleTo, movementStatesComponent);
         }

      }

      private static void queueUpdatesFor(@Nonnull Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo, @Nonnull MovementStatesComponent movementStatesComponent) {
         MovementStatesUpdate update = new MovementStatesUpdate();
         update.movementStates = movementStatesComponent.getMovementStates();

         for(Map.Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
            if (!ref.equals(entry.getKey())) {
               ((EntityTrackerSystems.EntityViewer)entry.getValue()).queueUpdate(ref, update);
            }
         }

      }

      public static void copyMovementStatesFrom(@Nonnull MovementStates from, @Nonnull MovementStates to) {
         to.idle = from.idle;
         to.horizontalIdle = from.horizontalIdle;
         to.jumping = from.jumping;
         to.flying = from.flying;
         to.walking = from.walking;
         to.running = from.running;
         to.sprinting = from.sprinting;
         to.crouching = from.crouching;
         to.forcedCrouching = from.forcedCrouching;
         to.falling = from.falling;
         to.climbing = from.climbing;
         to.inFluid = from.inFluid;
         to.swimming = from.swimming;
         to.swimJumping = from.swimJumping;
         to.onGround = from.onGround;
         to.mantling = from.mantling;
         to.sliding = from.sliding;
         to.mounting = from.mounting;
         to.rolling = from.rolling;
         to.sitting = from.sitting;
         to.gliding = from.gliding;
         to.sleeping = from.sleeping;
      }
   }
}
