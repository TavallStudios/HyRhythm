package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

public class MovementStatesSystem extends SteppableTickingSystem {
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public MovementStatesSystem(@Nonnull ComponentType<EntityStore, NPCEntity> npcComponentType, @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType, @Nonnull ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType) {
      this.dependencies = Set.of(new SystemDependency(Order.AFTER, ComputeVelocitySystem.class));
      this.npcComponentType = npcComponentType;
      this.velocityComponentType = velocityComponentType;
      this.movementStatesComponentType = movementStatesComponentType;
      this.query = Query.<EntityStore>and(npcComponentType, velocityComponentType, movementStatesComponentType);
   }

   @Nonnull
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   public void steppedTick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      NPCEntity npcComponent = (NPCEntity)archetypeChunk.getComponent(index, this.npcComponentType);

      assert npcComponent != null;

      Velocity velocityComponent = (Velocity)archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      MovementStatesComponent movementStatesComponent = (MovementStatesComponent)archetypeChunk.getComponent(index, this.movementStatesComponentType);

      assert movementStatesComponent != null;

      try {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (Objects.equals(npcComponent.getRoleName(), "Empty_Role")) {
            return;
         }

         npcComponent.getRole().updateMovementState(ref, movementStatesComponent.getMovementStates(), velocityComponent.getVelocity(), commandBuffer);
      } catch (Exception e) {
         HytaleLogger.Api var10000 = (HytaleLogger.Api)((HytaleLogger.Api)NPCPlugin.get().getLogger().atSevere()).withCause(e);
         String var10001 = npcComponent.getRoleName();
         var10000.log("Failed to update movement states for " + var10001 + ", Archetype: " + String.valueOf(archetypeChunk.getArchetype()) + ", Spawn config index: " + npcComponent.getSpawnConfiguration() + ": ");
      }

   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
