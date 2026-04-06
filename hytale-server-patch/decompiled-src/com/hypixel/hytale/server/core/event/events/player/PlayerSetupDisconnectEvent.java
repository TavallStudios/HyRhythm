package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PlayerSetupDisconnectEvent implements IEvent<Void> {
   private final String username;
   private final UUID uuid;
   private final PlayerAuthentication auth;
   private final PacketHandler.DisconnectReason disconnectReason;

   public PlayerSetupDisconnectEvent(String username, UUID uuid, PlayerAuthentication auth, PacketHandler.DisconnectReason disconnectReason) {
      this.username = username;
      this.uuid = uuid;
      this.auth = auth;
      this.disconnectReason = disconnectReason;
   }

   public String getUsername() {
      return this.username;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public PlayerAuthentication getAuth() {
      return this.auth;
   }

   public PacketHandler.DisconnectReason getDisconnectReason() {
      return this.disconnectReason;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.username;
      return "PlayerSetupDisconnectEvent{username='" + var10000 + "', uuid=" + String.valueOf(this.uuid) + ", auth=" + String.valueOf(this.auth) + ", disconnectReason=" + String.valueOf(this.disconnectReason) + "}";
   }
}
