package com.hypixel.hytale.server.core.asset.monitor;

import javax.annotation.Nonnull;

public class PathEvent {
   private final EventKind eventKind;
   private final long timestamp;

   public PathEvent(EventKind eventKind, long timestamp) {
      this.eventKind = eventKind;
      this.timestamp = timestamp;
   }

   public EventKind getEventKind() {
      return this.eventKind;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.eventKind);
      return "PathEvent{eventKind=" + var10000 + ", timestamp=" + this.timestamp + "}";
   }
}
