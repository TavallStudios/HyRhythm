package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventType;
import com.hypixel.hytale.server.npc.blackboard.view.event.entity.EntityEventView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import javax.annotation.Nonnull;

public class NPCDeathSystems {
   public static class NPCKillsEntitySystem extends DeathSystems.OnDeathSystem {
      @Nonnull
      public Query<EntityStore> getQuery() {
         return Query.<EntityStore>and(AllLegacyLivingEntityTypesQuery.INSTANCE, TransformComponent.getComponentType());
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
         if (npcComponentType != null) {
            Damage deathInfo = component.getDeathInfo();
            if (deathInfo != null) {
               Damage.Source var8 = deathInfo.getSource();
               if (var8 instanceof Damage.EntitySource) {
                  Damage.EntitySource entitySource = (Damage.EntitySource)var8;
                  Ref var11 = entitySource.getRef();
                  NPCEntity sourceNpcComponent = (NPCEntity)store.getComponent(var11, npcComponentType);
                  if (sourceNpcComponent == null) {
                     return;
                  }

                  TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());

                  assert transformComponent != null;

                  sourceNpcComponent.getDamageData().onKill(ref, transformComponent.getPosition().clone());
                  return;
               }
            }

         }
      }
   }

   public static class EntityViewSystem extends DeathSystems.OnDeathSystem {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      @Nonnull
      private final ResourceType<EntityStore, Blackboard> blackboardResourceType = Blackboard.getResourceType();
      @Nonnull
      private final Query<EntityStore> query;

      public EntityViewSystem() {
         this.query = Query.<EntityStore>and(Query.or(NPCEntity.getComponentType(), this.playerComponentType), this.transformComponentType);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Damage deathInfo = component.getDeathInfo();
         if (deathInfo != null) {
            Damage.Source var7 = deathInfo.getSource();
            if (var7 instanceof Damage.EntitySource) {
               Damage.EntitySource entitySource = (Damage.EntitySource)var7;
               Ref<EntityStore> sourceRef = entitySource.getRef();
               if (!sourceRef.isValid()) {
                  return;
               }

               Player sourcePlayerComponent = (Player)commandBuffer.getComponent(sourceRef, Player.getComponentType());
               if (sourcePlayerComponent != null && sourcePlayerComponent.getGameMode() == GameMode.Creative) {
                  PlayerSettings playerSettingsComponent = (PlayerSettings)commandBuffer.getComponent(sourceRef, PlayerSettings.getComponentType());
                  if (playerSettingsComponent == null || !playerSettingsComponent.creativeSettings().allowNPCDetection()) {
                     return;
                  }
               }

               TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, this.transformComponentType);

               assert transformComponent != null;

               Vector3d position = transformComponent.getPosition();
               Blackboard blackboardResource = (Blackboard)store.getResource(this.blackboardResourceType);
               EntityEventView entityEventView = (EntityEventView)blackboardResource.getView(EntityEventView.class, ChunkUtil.chunkCoordinate(position.x), ChunkUtil.chunkCoordinate(position.z));
               entityEventView.processAttackedEvent(ref, sourceRef, commandBuffer, EntityEventType.DEATH);
               return;
            }
         }

      }
   }
}
