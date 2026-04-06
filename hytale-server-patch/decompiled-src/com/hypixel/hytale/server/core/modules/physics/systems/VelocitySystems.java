package com.hypixel.hytale.server.core.modules.physics.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class VelocitySystems {
   public static class AddSystem extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, Velocity> velocityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public AddSystem(ComponentType<EntityStore, Velocity> velocityComponentType) {
         this.velocityComponentType = velocityComponentType;
         this.query = Query.<EntityStore>and(AllLegacyEntityTypesQuery.INSTANCE, Query.not(velocityComponentType));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.velocityComponentType);
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
