package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.event.IEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class LoadAssetEvent implements IEvent<Void> {
   public static final short PRIORITY_LOAD_COMMON = -32;
   public static final short PRIORITY_LOAD_REGISTRY = -16;
   public static final short PRIORITY_LOAD_LATE = 64;
   private final long bootStart;
   @Nonnull
   private final List<String> reasons = new ObjectArrayList();
   private boolean shouldShutdown = false;

   public LoadAssetEvent(long bootStart) {
      this.bootStart = bootStart;
   }

   public long getBootStart() {
      return this.bootStart;
   }

   public boolean isShouldShutdown() {
      return this.shouldShutdown;
   }

   @Nonnull
   public List<String> getReasons() {
      return this.reasons;
   }

   public void failed(boolean shouldShutdown, String reason) {
      this.shouldShutdown |= shouldShutdown;
      this.reasons.add(reason);
   }

   @Nonnull
   public String toString() {
      long var10000 = this.bootStart;
      return "LoadAssetEvent{bootStart=" + var10000 + ", shouldShutdown=" + this.shouldShutdown + ", reasons=" + String.valueOf(this.reasons) + "}";
   }
}
