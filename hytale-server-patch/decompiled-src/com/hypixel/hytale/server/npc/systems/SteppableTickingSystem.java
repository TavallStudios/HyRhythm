package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.StepComponent;
import javax.annotation.Nonnull;

public abstract class SteppableTickingSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, StepComponent> stepComponentType = StepComponent.getComponentType();
   @Nonnull
   private final ComponentType<EntityStore, Frozen> frozenComponentType = Frozen.getComponentType();

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      Frozen frozenComponent = (Frozen)archetypeChunk.getComponent(index, this.frozenComponentType);
      float tickLength;
      if (frozenComponent == null && !world.getWorldConfig().isAllNPCFrozen()) {
         tickLength = dt;
      } else {
         StepComponent stepComponent = (StepComponent)archetypeChunk.getComponent(index, this.stepComponentType);
         if (stepComponent == null) {
            return;
         }

         tickLength = stepComponent.getTickLength();
      }

      this.steppedTick(tickLength, index, archetypeChunk, store, commandBuffer);
   }

   public abstract void steppedTick(float var1, int var2, @Nonnull ArchetypeChunk<EntityStore> var3, @Nonnull Store<EntityStore> var4, @Nonnull CommandBuffer<EntityStore> var5);
}
