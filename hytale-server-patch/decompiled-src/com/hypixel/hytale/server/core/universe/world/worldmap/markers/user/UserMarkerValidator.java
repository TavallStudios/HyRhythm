package com.hypixel.hytale.server.core.universe.world.worldmap.markers.user;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.CreateUserMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.UserMapMarkerConfig;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import java.util.Collection;
import java.util.UUID;

public final class UserMarkerValidator {
   private static final int NAME_LENGTH_LIMIT = 24;

   public static PlaceResult validatePlacing(Ref<EntityStore> ref, CreateUserMarker packet) {
      boolean shared = packet.shared;
      Store<EntityStore> store = ref.getStore();
      World world = ((EntityStore)store.getExternalData()).getWorld();
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      if (isPlayerTooFarFromMarker(ref, (double)packet.x, (double)packet.z)) {
         return new Fail("server.worldmap.markers.edit.tooFar");
      } else if (packet.name != null && packet.name.length() > 24) {
         return new Fail("server.worldmap.markers.create.nameTooLong");
      } else {
         UserMapMarkersStore markersStore = (UserMapMarkersStore)(shared ? (UserMapMarkersStore)world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType()) : player.getPlayerConfigData().getPerWorldData(world.getName()));
         UUID playerUuid = ((UUIDComponent)store.getComponent(ref, UUIDComponent.getComponentType())).getUuid();
         Collection<? extends UserMapMarker> markersByPlayer = markersStore.getUserMapMarkers(playerUuid);
         UserMapMarkerConfig markersConfig = world.getGameplayConfig().getWorldMapConfig().getUserMapMarkerConfig();
         if (!markersConfig.isAllowCreatingMarkers()) {
            return new Fail("server.worldmap.markers.create.creationDisabled");
         } else {
            int limit = shared ? markersConfig.getMaxSharedMarkersPerPlayer() : markersConfig.getMaxPersonalMarkersPerPlayer();
            if (markersByPlayer.size() + 1 >= limit) {
               String msg = shared ? "server.worldmap.markers.create.tooManyShared" : "server.worldmap.markers.create.tooManyPersonal";
               return new Fail(Message.translation(msg).param("limit", limit));
            } else {
               return new CanSpawn(player, markersStore);
            }
         }
      }
   }

   public static RemoveResult validateRemove(Ref<EntityStore> ref, UserMapMarker marker) {
      Store<EntityStore> store = ref.getStore();
      World world = ((EntityStore)store.getExternalData()).getWorld();
      if (isPlayerTooFarFromMarker(ref, (double)marker.getX(), (double)marker.getZ())) {
         return new Fail("server.worldmap.markers.edit.tooFar");
      } else {
         UserMapMarkerConfig markersConfig = world.getGameplayConfig().getWorldMapConfig().getUserMapMarkerConfig();
         UUID playerUuid = ((UUIDComponent)store.getComponent(ref, UUIDComponent.getComponentType())).getUuid();
         UUID createdBy = marker.getCreatedByUuid();
         boolean isOwner = playerUuid.equals(createdBy) || createdBy == null;
         boolean hasPermission = isOwner || markersConfig.isAllowDeleteOtherPlayersSharedMarkers();
         return (RemoveResult)(!hasPermission ? new Fail("server.worldmap.markers.edit.notOwner") : new CanRemove());
      }
   }

   private static boolean isPlayerTooFarFromMarker(Ref<EntityStore> ref, double markerX, double markerZ) {
      Store<EntityStore> store = ref.getStore();
      Player player = (Player)store.getComponent(ref, Player.getComponentType());
      Transform transform = ((TransformComponent)store.getComponent(ref, TransformComponent.getComponentType())).getTransform();
      Vector3d playerPosition = transform.getPosition();
      double distanceToMarker = playerPosition.distanceSquaredTo(markerX, playerPosition.y, markerZ);
      return distanceToMarker > getMaxRemovalDistanceSquared(player);
   }

   private static double getMaxRemovalDistanceSquared(Player player) {
      double maxDistance = (double)player.getViewRadius() * 1.5 * 32.0;
      return maxDistance * maxDistance;
   }

   public static record Fail(Message errorMsg) implements PlaceResult, RemoveResult {
      public Fail(String messageKey) {
         this(Message.translation(messageKey));
      }
   }

   public static record CanSpawn(Player player, UserMapMarkersStore markersStore) implements PlaceResult {
   }

   public static record CanRemove() implements RemoveResult {
   }

   public sealed interface PlaceResult permits UserMarkerValidator.Fail, UserMarkerValidator.CanSpawn {
   }

   public sealed interface RemoveResult permits UserMarkerValidator.Fail, UserMarkerValidator.CanRemove {
   }
}
