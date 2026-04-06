package com.hypixel.hytale.server.core.event.events;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import javax.annotation.Nonnull;

/** @deprecated */
@Deprecated
public class PrepareUniverseEvent implements IEvent<Void> {
   private WorldConfigProvider worldConfigProvider;

   public PrepareUniverseEvent(WorldConfigProvider worldConfigProvider) {
      this.worldConfigProvider = worldConfigProvider;
   }

   public WorldConfigProvider getWorldConfigProvider() {
      return this.worldConfigProvider;
   }

   public void setWorldConfigProvider(WorldConfigProvider worldConfigProvider) {
      this.worldConfigProvider = worldConfigProvider;
   }

   @Nonnull
   public String toString() {
      return "PrepareUniverseEvent{worldConfigProvider=" + String.valueOf(this.worldConfigProvider) + "}";
   }
}
