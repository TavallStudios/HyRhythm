package com.hypixel.hytale.server.core.universe.system;

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
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.packets.entities.ChangeVelocity;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.systems.GenericVelocityInstructionSystem;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public class PlayerVelocityInstructionSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public PlayerVelocityInstructionSystem() {
      this.dependencies = Set.of(new SystemDependency(Order.BEFORE, GenericVelocityInstructionSystem.class), new SystemTypeDependency(Order.AFTER, EntityModule.get().getVelocityModifyingSystemType()));
      this.query = Query.<EntityStore>and(PlayerRef.getComponentType(), Velocity.getComponentType());
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      Velocity velocityComponent = (Velocity)archetypeChunk.getComponent(index, Velocity.getComponentType());

      assert velocityComponent != null;

      for(Velocity.Instruction instruction : velocityComponent.getInstructions()) {
         switch (instruction.getType()) {
            case Set:
               Vector3d velocity = instruction.getVelocity();
               VelocityConfig velocityConfig = instruction.getConfig();
               playerRefComponent.getPacketHandler().writeNoCache(new ChangeVelocity((float)velocity.x, (float)velocity.y, (float)velocity.z, ChangeVelocityType.Set, velocityConfig != null ? velocityConfig.toPacket() : null));
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
               playerRefComponent.getPacketHandler().writeNoCache(new ChangeVelocity((float)velocity.x, (float)velocity.y, (float)velocity.z, ChangeVelocityType.Add, velocityConfig != null ? velocityConfig.toPacket() : null));
               if (DebugUtils.DISPLAY_FORCES) {
                  TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType());
                  if (transformComponent != null) {
                     World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
                     DebugUtils.addForce(world, transformComponent.getPosition(), new Vector3d(velocity.x, velocity.y, velocity.z), velocityConfig);
                  }
               }
         }
      }

      velocityComponent.getInstructions().clear();
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
