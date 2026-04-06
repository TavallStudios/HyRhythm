package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemTypeDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.systems.GenericVelocityInstructionSystem;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import java.util.Set;
import javax.annotation.Nonnull;

public class NPCVelocityInstructionSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public NPCVelocityInstructionSystem() {
      this.dependencies = Set.of(new SystemDependency(Order.BEFORE, GenericVelocityInstructionSystem.class), new SystemTypeDependency(Order.AFTER, EntityModule.get().getVelocityModifyingSystemType()));
      this.query = Query.<EntityStore>and(NPCEntity.getComponentType(), Velocity.getComponentType());
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      NPCEntity npcComponent = (NPCEntity)archetypeChunk.getComponent(index, NPCEntity.getComponentType());

      assert npcComponent != null;

      Role role = npcComponent.getRole();
      if (role != null) {
         Velocity velocityComponent = (Velocity)archetypeChunk.getComponent(index, Velocity.getComponentType());

         assert velocityComponent != null;

         for(Velocity.Instruction instruction : velocityComponent.getInstructions()) {
            switch (instruction.getType()) {
               case Set:
                  Vector3d velocity = instruction.getVelocity();
                  VelocityConfig velocityConfig = instruction.getConfig();
                  role.processSetVelocityInstruction(velocity, velocityConfig);
                  if (DebugUtils.DISPLAY_FORCES) {
                     TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                     if (transformComponent != null) {
                        World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
                        DebugUtils.addForce(world, transformComponent.getPosition(), velocity, velocityConfig);
                     }
                  }
                  break;
               case Add:
                  Vector3d velocity = instruction.getVelocity();
                  VelocityConfig velocityConfig = instruction.getConfig();
                  role.processAddVelocityInstruction(velocity, velocityConfig);
                  if (DebugUtils.DISPLAY_FORCES) {
                     TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                     if (transformComponent != null) {
                        World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
                        DebugUtils.addForce(world, transformComponent.getPosition(), velocity, velocityConfig);
                     }
                  }
            }
         }

         velocityComponent.getInstructions().clear();
      }
   }

   @Nonnull
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
