package com.hypixel.hytale.server.flock;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class FlockDeathSystems {
   public static class PlayerDeath extends DeathSystems.OnDeathSystem {
      @Nonnull
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         commandBuffer.tryRemoveComponent(ref, FlockMembership.getComponentType());
      }
   }

   public static class EntityDeath extends DeathSystems.OnDeathSystem {
      @Nonnull
      private final Query<EntityStore> query;

      public EntityDeath() {
         this.query = Query.<EntityStore>and(AllLegacyLivingEntityTypesQuery.INSTANCE, Query.not(Player.getComponentType()));
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         NPCEntity npcComponent = (NPCEntity)commandBuffer.getComponent(ref, NPCEntity.getComponentType());
         if (npcComponent != null) {
            Role role = npcComponent.getRole();
            if (role != null && !role.isCorpseStaysInFlock()) {
               commandBuffer.tryRemoveComponent(ref, FlockMembership.getComponentType());
            }
         }

         Damage damageInfo = component.getDeathInfo();
         Ref<EntityStore> sourceRef = null;
         if (damageInfo != null) {
            Damage.Source var9 = damageInfo.getSource();
            if (var9 instanceof Damage.EntitySource) {
               Damage.EntitySource entitySource = (Damage.EntitySource)var9;
               sourceRef = entitySource.getRef();
            }
         }

         if (sourceRef != null) {
            Flock attackerFlock = FlockPlugin.getFlock(commandBuffer, sourceRef);
            if (attackerFlock != null) {
               attackerFlock.onTargetKilled(commandBuffer, ref);
            }

         }
      }
   }
}
