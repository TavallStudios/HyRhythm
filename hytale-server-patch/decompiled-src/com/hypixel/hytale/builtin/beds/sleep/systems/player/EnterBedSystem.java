package com.hypixel.hytale.builtin.beds.sleep.systems.player;

import com.hypixel.hytale.builtin.beds.sleep.systems.world.CanSleepInWorld;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnterBedSystem extends RefChangeSystem<EntityStore, MountedComponent> {
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_GAME_TIME_PAUSED = Message.translation("server.interactions.sleep.gameTimePaused");
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_NOT_WITHIN_HOURS = Message.translation("server.interactions.sleep.notWithinHours");
   @Nonnull
   private static final Message MESSAGE_SERVER_INTERACTIONS_SLEEP_DISABLED = Message.translation("server.interactions.sleep.disabled");
   @Nonnull
   private final ComponentType<EntityStore, MountedComponent> mountedComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public EnterBedSystem(@Nonnull ComponentType<EntityStore, MountedComponent> mountedComponentType, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType) {
      this.mountedComponentType = mountedComponentType;
      this.playerRefComponentType = playerRefComponentType;
      this.query = Query.<EntityStore>and(mountedComponentType, playerRefComponentType);
   }

   @Nonnull
   public ComponentType<EntityStore, MountedComponent> componentType() {
      return this.mountedComponentType;
   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull MountedComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      check(ref, component, store, this.playerRefComponentType);
   }

   public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable MountedComponent oldComponent, @Nonnull MountedComponent newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      check(ref, newComponent, store, this.playerRefComponentType);
   }

   public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull MountedComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
   }

   private static void check(@Nonnull Ref<EntityStore> ref, @Nonnull MountedComponent component, @Nonnull Store<EntityStore> store, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType) {
      if (component.getBlockMountType() == BlockMountType.Bed) {
         onEnterBed(ref, store, playerRefComponentType);
      }

   }

   private static void onEnterBed(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      CanSleepInWorld.Result canSleepResult = CanSleepInWorld.check(world);
      if (canSleepResult.isNegative()) {
         PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, playerRefComponentType);

         assert playerRefComponent != null;

         if (canSleepResult instanceof CanSleepInWorld.NotDuringSleepHoursRange) {
            CanSleepInWorld.NotDuringSleepHoursRange var6 = (CanSleepInWorld.NotDuringSleepHoursRange)canSleepResult;
            CanSleepInWorld.NotDuringSleepHoursRange var10000 = var6;

            try {
               var17 = var10000.worldTime();
            } catch (Throwable var13) {
               throw new MatchException(var13.toString(), var13);
            }

            LocalDateTime var9 = var17;
            LocalDateTime worldTime = var9;
            var10000 = var6;

            try {
               var19 = var10000.sleepConfig();
            } catch (Throwable var12) {
               throw new MatchException(var12.toString(), var12);
            }

            SleepConfig var14 = var19;
            SleepConfig sleepConfig = var14;
            LocalTime startTime = var14.getSleepStartTime();
            Duration untilSleep = sleepConfig.computeDurationUntilSleep(worldTime);
            Message msg = Message.translation("server.interactions.sleep.sleepAtTheseHours").param("timeValue", startTime.toString()).param("until", formatDuration(untilSleep));
            playerRefComponent.sendMessage(msg.color("#F2D729"));
            SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, sleepConfig.getSounds().getFailIndex(), SoundCategory.UI);
         } else {
            Message msg = getMessage(canSleepResult);
            playerRefComponent.sendMessage(msg);
         }
      }

   }

   @Nonnull
   private static Message getMessage(@Nonnull CanSleepInWorld.Result result) {
      Objects.requireNonNull(result);
      byte var2 = 0;
      Message var10000;
      //$FF: var2->value
      //1->com/hypixel/hytale/builtin/beds/sleep/systems/world/CanSleepInWorld$NotDuringSleepHoursRange
      switch (result.typeSwitch<invokedynamic>(result, var2)) {
         case 0:
            var10000 = MESSAGE_SERVER_INTERACTIONS_SLEEP_GAME_TIME_PAUSED;
            break;
         case 1:
            CanSleepInWorld.NotDuringSleepHoursRange ignored = (CanSleepInWorld.NotDuringSleepHoursRange)result;
            var10000 = MESSAGE_SERVER_INTERACTIONS_SLEEP_NOT_WITHIN_HOURS;
            break;
         default:
            var10000 = MESSAGE_SERVER_INTERACTIONS_SLEEP_DISABLED;
      }

      return var10000;
   }

   @Nonnull
   private static Message formatDuration(@Nonnull Duration duration) {
      long totalMinutes = duration.toMinutes();
      long hours = totalMinutes / 60L;
      long minutes = totalMinutes % 60L;
      String msgKey = hours > 0L ? "server.interactions.sleep.durationHours" : "server.interactions.sleep.durationMins";
      return Message.translation(msgKey).param("hours", hours).param("mins", hours == 0L ? String.valueOf(minutes) : String.format("%02d", minutes));
   }
}
