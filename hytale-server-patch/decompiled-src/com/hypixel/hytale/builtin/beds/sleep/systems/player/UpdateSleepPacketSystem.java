package com.hypixel.hytale.builtin.beds.sleep.systems.player;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.components.SleepTracker;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSleep;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.CanSleepInWorld;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.StartSlumberSystem;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.world.SleepClock;
import com.hypixel.hytale.protocol.packets.world.SleepMultiplayer;
import com.hypixel.hytale.protocol.packets.world.UpdateSleepState;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateSleepPacketSystem extends DelayedEntitySystem<EntityStore> {
   private static final int MAX_SAMPLE_COUNT = 5;
   private static final float SYSTEM_INTERVAL_S = 0.25F;
   @Nonnull
   private static final Duration SPAN_BEFORE_BLACK_SCREEN = Duration.ofMillis(1200L);
   @Nonnull
   private static final UUID[] EMPTY_UUIDS = new UUID[0];
   @Nonnull
   private static final UpdateSleepState PACKET_NO_SLEEP_UI = new UpdateSleepState(false, false, (SleepClock)null, (SleepMultiplayer)null);
   @Nonnull
   private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType;
   @Nonnull
   private final ComponentType<EntityStore, SleepTracker> sleepTrackerComponentType;
   @Nonnull
   private final ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType;
   @Nonnull
   private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType;
   @Nonnull
   private final Query<EntityStore> query;

   public UpdateSleepPacketSystem(@Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType, @Nonnull ComponentType<EntityStore, PlayerSomnolence> playerSomnolenceComponentType, @Nonnull ComponentType<EntityStore, SleepTracker> sleepTrackerComponentType, @Nonnull ResourceType<EntityStore, WorldSomnolence> worldSomnolenceResourceType, @Nonnull ResourceType<EntityStore, WorldTimeResource> worldTimeResourceType) {
      super(0.25F);
      this.playerRefComponentType = playerRefComponentType;
      this.playerSomnolenceComponentType = playerSomnolenceComponentType;
      this.sleepTrackerComponentType = sleepTrackerComponentType;
      this.worldSomnolenceResourceType = worldSomnolenceResourceType;
      this.worldTimeResourceType = worldTimeResourceType;
      this.query = Query.<EntityStore>and(playerRefComponentType, playerSomnolenceComponentType, sleepTrackerComponentType);
   }

   @Nonnull
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      UpdateSleepState packet = this.createSleepPacket(store, index, archetypeChunk);
      SleepTracker sleepTrackerComponent = (SleepTracker)archetypeChunk.getComponent(index, this.sleepTrackerComponentType);

      assert sleepTrackerComponent != null;

      packet = sleepTrackerComponent.generatePacketToSend(packet);
      if (packet != null) {
         PlayerRef playerRefComponent = (PlayerRef)archetypeChunk.getComponent(index, this.playerRefComponentType);

         assert playerRefComponent != null;

         playerRefComponent.getPacketHandler().write((ToClientPacket)packet);
      }

   }

   @Nonnull
   private UpdateSleepState createSleepPacket(@Nonnull Store<EntityStore> store, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      WorldSomnolence worldSomnolenceResource = (WorldSomnolence)store.getResource(this.worldSomnolenceResourceType);
      WorldSleep worldSleepState = worldSomnolenceResource.getState();
      PlayerSomnolence playerSomnolenceComponent = (PlayerSomnolence)archetypeChunk.getComponent(index, this.playerSomnolenceComponentType);

      assert playerSomnolenceComponent != null;

      PlayerSleep playerSleepState = playerSomnolenceComponent.getSleepState();
      SleepClock var10000;
      if (worldSleepState instanceof WorldSlumber) {
         WorldSlumber slumber = (WorldSlumber)worldSleepState;
         var10000 = slumber.createSleepClock();
      } else {
         var10000 = null;
      }

      SleepClock clock = var10000;
      Objects.requireNonNull(playerSleepState);
      byte var11 = 0;
      UpdateSleepState var21;
      //$FF: var11->value
      //0->com/hypixel/hytale/builtin/beds/sleep/components/PlayerSleep$FullyAwake
      //1->com/hypixel/hytale/builtin/beds/sleep/components/PlayerSleep$MorningWakeUp
      //2->com/hypixel/hytale/builtin/beds/sleep/components/PlayerSleep$NoddingOff
      //3->com/hypixel/hytale/builtin/beds/sleep/components/PlayerSleep$Slumber
      switch (playerSleepState.typeSwitch<invokedynamic>(playerSleepState, var11)) {
         case 0:
            PlayerSleep.FullyAwake ignored = (PlayerSleep.FullyAwake)playerSleepState;
            var21 = PACKET_NO_SLEEP_UI;
            break;
         case 1:
            PlayerSleep.MorningWakeUp ignored = (PlayerSleep.MorningWakeUp)playerSleepState;
            var21 = PACKET_NO_SLEEP_UI;
            break;
         case 2:
            PlayerSleep.NoddingOff noddingOff = (PlayerSleep.NoddingOff)playerSleepState;
            if (CanSleepInWorld.check(world).isNegative()) {
               var21 = PACKET_NO_SLEEP_UI;
            } else {
               long elapsedMs = Duration.between(noddingOff.realTimeStart(), Instant.now()).toMillis();
               boolean grayFade = elapsedMs > SPAN_BEFORE_BLACK_SCREEN.toMillis();
               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               boolean readyToSleep = StartSlumberSystem.isReadyToSleep(store, ref);
               var21 = new UpdateSleepState(grayFade, false, clock, readyToSleep ? this.createSleepMultiplayer(store) : null);
            }
            break;
         case 3:
            PlayerSleep.Slumber ignored = (PlayerSleep.Slumber)playerSleepState;
            var21 = new UpdateSleepState(true, true, clock, (SleepMultiplayer)null);
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var21;
   }

   @Nullable
   private SleepMultiplayer createSleepMultiplayer(@Nonnull Store<EntityStore> store) {
      World world = ((EntityStore)store.getExternalData()).getWorld();
      List<PlayerRef> playerRefs = new ObjectArrayList(world.getPlayerRefs());
      playerRefs.removeIf((playerRefx) -> playerRefx.getReference() == null);
      if (playerRefs.size() <= 1) {
         return null;
      } else {
         playerRefs.sort(Comparator.comparingLong((refx) -> (long)(refx.getUuid().hashCode() + world.hashCode())));
         int sleepersCount = 0;
         int awakeCount = 0;
         List<UUID> awakeSampleList = new ObjectArrayList(playerRefs.size());

         for(PlayerRef playerRef : playerRefs) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid()) {
               boolean readyToSleep = StartSlumberSystem.isReadyToSleep(store, ref);
               if (readyToSleep) {
                  ++sleepersCount;
               } else {
                  ++awakeCount;
                  awakeSampleList.add(playerRef.getUuid());
               }
            }
         }

         UUID[] awakeSample = awakeSampleList.size() > 5 ? EMPTY_UUIDS : (UUID[])awakeSampleList.toArray((x$0) -> new UUID[x$0]);
         return new SleepMultiplayer(sleepersCount, awakeCount, awakeSample);
      }
   }
}
