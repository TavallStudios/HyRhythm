package com.hypixel.hytale.builtin.instances.removal;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class TimeoutCondition implements RemovalCondition {
   @Nonnull
   public static final BuilderCodec<TimeoutCondition> CODEC;
   private double timeoutSeconds;

   public TimeoutCondition() {
      this.timeoutSeconds = (double)TimeUnit.MINUTES.toSeconds(5L);
   }

   public TimeoutCondition(double timeoutSeconds) {
      this.timeoutSeconds = (double)TimeUnit.MINUTES.toSeconds(5L);
      this.timeoutSeconds = timeoutSeconds;
   }

   public double getTimeoutSeconds() {
      return this.timeoutSeconds;
   }

   public boolean shouldRemoveWorld(@Nonnull Store<ChunkStore> store) {
      InstanceDataResource data = (InstanceDataResource)store.getResource(InstanceDataResource.getResourceType());
      World world = ((ChunkStore)store.getExternalData()).getWorld();
      TimeResource timeResource = (TimeResource)world.getEntityStore().getStore().getResource(TimeResource.getResourceType());
      if (data.getTimeoutTimer() == null) {
         data.setTimeoutTimer(timeResource.getNow().plusNanos((long)(this.timeoutSeconds * 1.0E9)));
      }

      return timeResource.getNow().isAfter(data.getTimeoutTimer());
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(TimeoutCondition.class, TimeoutCondition::new).documentation("A condition that triggers after a set time limit.")).append(new KeyedCodec("TimeoutSeconds", Codec.DOUBLE), (o, i) -> o.timeoutSeconds = i, (o) -> o.timeoutSeconds).documentation("How long to wait (in seconds) before closing the world.").add()).build();
   }
}
