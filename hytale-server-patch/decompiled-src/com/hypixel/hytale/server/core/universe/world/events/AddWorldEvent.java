package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class AddWorldEvent extends WorldEvent implements ICancellable {
   private boolean cancelled = false;

   public AddWorldEvent(@Nonnull World world) {
      super(world);
   }

   @Nonnull
   public String toString() {
      boolean var10000 = this.cancelled;
      return "AddWorldEvent{cancelled=" + var10000 + "} " + super.toString();
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
