package com.hypixel.hytale.server.core.modules.entity.player;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerSendInventorySystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, Player> componentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> refComponentType = PlayerRef.getComponentType();
   @Nonnull
   private final Query<EntityStore> query;

   public PlayerSendInventorySystem(ComponentType<EntityStore, Player> componentType) {
      this.componentType = componentType;
      this.query = Query.<EntityStore>and(componentType, this.refComponentType);
   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      Player playerComponent = (Player)archetypeChunk.getComponent(index, this.componentType);

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      if (inventory.consumeIsDirty()) {
         PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, this.refComponentType);

         assert playerRefComponent != null;

         playerRefComponent.getPacketHandler().write((ToClientPacket)inventory.toPacket());
      }

      playerComponent.getWindowManager().updateWindows();
   }
}
