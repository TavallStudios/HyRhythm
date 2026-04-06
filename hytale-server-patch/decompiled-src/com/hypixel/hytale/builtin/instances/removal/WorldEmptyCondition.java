package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class WorldEmptyCondition implements RemovalCondition {
   @Nonnull
   public static final WorldEmptyCondition INSTANCE = new WorldEmptyCondition();
   @Nonnull
   public static final RemovalCondition[] REMOVE_WHEN_EMPTY;
   @Nonnull
   public static final BuilderCodec<WorldEmptyCondition> CODEC;
   private double timeoutSeconds;

   public WorldEmptyCondition() {
      this.timeoutSeconds = (double)TimeUnit.MINUTES.toSeconds(5L);
   }

   public WorldEmptyCondition(double timeoutSeconds) {
      this.timeoutSeconds = (double)TimeUnit.MINUTES.toSeconds(5L);
      this.timeoutSeconds = timeoutSeconds;
   }

   public boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> store) {
      InstanceDataResource data = (InstanceDataResource)store.getResource(InstanceDataResource.getResourceType());
      World world = ((ChunkStore)store.getExternalData()).getWorld();
      TimeResource timeResource = (TimeResource)world.getEntityStore().getStore().getResource(TimeResource.getResourceType());
      boolean hasPlayer = world.getPlayerCount() > 0;
      boolean hadPlayer = data.hadPlayer();
      if (!hasPlayer && hadPlayer) {
         return true;
      } else {
         if (hasPlayer && !hadPlayer) {
            data.setHadPlayer(true);
            data.setWorldTimeoutTimer((Instant)null);
         }

         if (!hadPlayer && !hasPlayer) {
            if (data.getWorldTimeoutTimer() == null) {
               data.setWorldTimeoutTimer(timeResource.getNow().plusNanos((long)(this.timeoutSeconds * 1.0E9)));
            }

            return timeResource.getNow().isAfter(data.getWorldTimeoutTimer());
         } else {
            return false;
         }
      }
   }

   static {
      REMOVE_WHEN_EMPTY = new RemovalCondition[]{INSTANCE};
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(WorldEmptyCondition.class, WorldEmptyCondition::new).documentation("A condition that triggers when the world is empty.\n\nIt will only trigger after at least one player has joined. As a safety measure it provides a timeout for waiting for a player to join in case the player disconnected before entering the world.")).append(new KeyedCodec("TimeoutSeconds", Codec.DOUBLE), (o, i) -> o.timeoutSeconds = i, (o) -> o.timeoutSeconds).documentation("How long to wait (in seconds) for a player to join before closing the world.").add()).build();
   }
}
