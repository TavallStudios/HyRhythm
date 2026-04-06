package com.hypixel.hytale.builtin.portals.systems.curse;

import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.utils.CursedItems;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DiedInPortalSystem extends DeathSystems.OnDeathSystem {
   public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      PortalWorld portalWorld = (PortalWorld)commandBuffer.getResource(PortalWorld.getResourceType());
      if (portalWorld.exists()) {
         UUID playerId = ((UUIDComponent)commandBuffer.getComponent(ref, UUIDComponent.getComponentType())).getUuid();
         portalWorld.getDiedInWorld().add(playerId);
         Player player = (Player)store.getComponent(ref, Player.getComponentType());
         CursedItems.deleteAll(player);
      }
   }

   @Nullable
   public Query<EntityStore> getQuery() {
      return Query.<EntityStore>and(Player.getComponentType(), UUIDComponent.getComponentType());
   }
}
