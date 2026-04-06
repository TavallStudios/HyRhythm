package com.hypixel.hytale.component.system;

import javax.annotation.Nonnull;

public abstract class EventSystem<EventType extends EcsEvent> {
   @Nonnull
   private final Class<EventType> eventType;

   protected EventSystem(@Nonnull Class<EventType> eventType) {
      this.eventType = eventType;
   }

   protected boolean shouldProcessEvent(@Nonnull EventType event) {
      boolean var10000;
      if (event instanceof ICancellableEcsEvent cancellable) {
         if (cancellable.isCancelled()) {
            var10000 = false;
            return var10000;
         }
      }

      var10000 = true;
      return var10000;
   }

   @Nonnull
   public Class<EventType> getEventType() {
      return this.eventType;
   }
}
