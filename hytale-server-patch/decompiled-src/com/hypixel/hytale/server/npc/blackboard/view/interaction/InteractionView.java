package com.hypixel.hytale.server.npc.blackboard.view.interaction;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.PrioritisedProviderView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.UUID;
import javax.annotation.Nonnull;

public class InteractionView extends PrioritisedProviderView<ReservationProvider, InteractionView> {
   private final World world;

   public InteractionView(World world) {
      this.world = world;
      this.registerProvider(2147483647, (ReservationProvider)(npcRef, playerRef, componentAccessor) -> {
         NPCEntity npcComponent = (NPCEntity)componentAccessor.getComponent(npcRef, NPCEntity.getComponentType());

         assert npcComponent != null;

         if (!npcComponent.isReserved()) {
            return ReservationStatus.NOT_RESERVED;
         } else {
            UUIDComponent playerUUIDComponent = (UUIDComponent)componentAccessor.getComponent(playerRef, UUIDComponent.getComponentType());

            assert playerUUIDComponent != null;

            UUID playerUUID = playerUUIDComponent.getUuid();
            return npcComponent.isReservedBy(playerUUID) ? ReservationStatus.RESERVED_THIS : ReservationStatus.RESERVED_OTHER;
         }
      });
   }

   public boolean isOutdated(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      return false;
   }

   public InteractionView getUpdatedView(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World entityWorld = ((EntityStore)componentAccessor.getExternalData()).getWorld();
      if (!entityWorld.equals(this.world)) {
         Blackboard blackboardResource = (Blackboard)componentAccessor.getResource(Blackboard.getResourceType());
         return (InteractionView)blackboardResource.getView(InteractionView.class, ref, componentAccessor);
      } else {
         return this;
      }
   }

   public void initialiseEntity(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent) {
   }

   public void cleanup() {
   }

   public void onWorldRemoved() {
   }

   @Nonnull
   public ReservationStatus getReservationStatus(@Nonnull Ref<EntityStore> npcRef, @Nonnull Ref<EntityStore> playerRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      for(int i = 0; i < this.providers.size(); ++i) {
         ReservationStatus status = ((ReservationProvider)((PrioritisedProviderView.PrioritisedProvider)this.providers.get(i)).getProvider()).getReservationStatus(npcRef, playerRef, componentAccessor);
         if (status != ReservationStatus.NOT_RESERVED) {
            return status;
         }
      }

      return ReservationStatus.NOT_RESERVED;
   }
}
