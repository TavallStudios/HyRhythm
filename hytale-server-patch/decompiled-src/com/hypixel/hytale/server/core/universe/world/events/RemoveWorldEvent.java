package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.ICancellable;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class RemoveWorldEvent extends WorldEvent implements ICancellable {
   private boolean cancelled;
   @Nonnull
   private final RemovalReason removalReason;

   public RemoveWorldEvent(@Nonnull World world, @Nonnull RemovalReason removalReason) {
      super(world);
      this.removalReason = removalReason;
   }

   @Nonnull
   public RemovalReason getRemovalReason() {
      return this.removalReason;
   }

   public boolean isCancelled() {
      return this.removalReason == RemoveWorldEvent.RemovalReason.EXCEPTIONAL ? false : this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Nonnull
   public String toString() {
      boolean var10000 = this.cancelled;
      return "RemoveWorldEvent{cancelled=" + var10000 + "} " + super.toString();
   }

   public static enum RemovalReason {
      GENERAL,
      EXCEPTIONAL;
   }
}
