package com.hypixel.hytale.builtin.adventure.npcobjectives.assets;

import com.hypixel.hytale.builtin.adventure.objectives.config.task.ObjectiveTaskAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.taskcondition.TaskConditionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders.WorldLocationProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KillSpawnBeaconObjectiveTaskAsset extends KillObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<KillSpawnBeaconObjectiveTaskAsset> CODEC;
   protected ObjectiveSpawnBeacon[] spawnBeacons;

   public KillSpawnBeaconObjectiveTaskAsset(String descriptionId, TaskConditionAsset[] taskConditions, Vector3i[] mapMarkers, int count, String npcGroupId, ObjectiveSpawnBeacon[] spawnBeacons) {
      super(descriptionId, taskConditions, mapMarkers, count, npcGroupId);
      this.spawnBeacons = spawnBeacons;
   }

   protected KillSpawnBeaconObjectiveTaskAsset() {
   }

   public ObjectiveSpawnBeacon[] getSpawnBeacons() {
      return this.spawnBeacons;
   }

   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (!super.matchesAsset0(task)) {
         return false;
      } else if (task instanceof KillSpawnBeaconObjectiveTaskAsset) {
         KillSpawnBeaconObjectiveTaskAsset killSpawnBeaconObjectiveTaskAsset = (KillSpawnBeaconObjectiveTaskAsset)task;
         return Arrays.equals(killSpawnBeaconObjectiveTaskAsset.spawnBeacons, this.spawnBeacons);
      } else {
         return false;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.spawnBeacons);
      return "KillSpawnBeaconObjectiveTaskAsset{spawnBeacons=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(KillSpawnBeaconObjectiveTaskAsset.class, KillSpawnBeaconObjectiveTaskAsset::new, KillObjectiveTaskAsset.CODEC).append(new KeyedCodec("SpawnBeacons", new ArrayCodec(KillSpawnBeaconObjectiveTaskAsset.ObjectiveSpawnBeacon.CODEC, (x$0) -> new ObjectiveSpawnBeacon[x$0])), (killSpawnBeaconObjectiveTaskAsset, objectiveSpawnBeacons) -> killSpawnBeaconObjectiveTaskAsset.spawnBeacons = objectiveSpawnBeacons, (killSpawnBeaconObjectiveTaskAsset) -> killSpawnBeaconObjectiveTaskAsset.spawnBeacons).addValidator(Validators.nonEmptyArray()).add()).build();
   }

   public static class ObjectiveSpawnBeacon {
      @Nonnull
      public static final BuilderCodec<ObjectiveSpawnBeacon> CODEC;
      protected String spawnBeaconId;
      protected Vector3d offset;
      protected WorldLocationProvider worldLocationProvider;

      public String getSpawnBeaconId() {
         return this.spawnBeaconId;
      }

      public Vector3d getOffset() {
         return this.offset;
      }

      public WorldLocationProvider getWorldLocationProvider() {
         return this.worldLocationProvider;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ObjectiveSpawnBeacon that = (ObjectiveSpawnBeacon)o;
            if (!this.spawnBeaconId.equals(that.spawnBeaconId)) {
               return false;
            } else {
               if (this.offset != null) {
                  if (!this.offset.equals(that.offset)) {
                     return false;
                  }
               } else if (that.offset != null) {
                  return false;
               }

               return this.worldLocationProvider != null ? this.worldLocationProvider.equals(that.worldLocationProvider) : that.worldLocationProvider == null;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.spawnBeaconId.hashCode();
         result = 31 * result + (this.offset != null ? this.offset.hashCode() : 0);
         result = 31 * result + (this.worldLocationProvider != null ? this.worldLocationProvider.hashCode() : 0);
         return result;
      }

      @Nonnull
      public String toString() {
         String var10000 = this.spawnBeaconId;
         return "ObjectiveSpawnBeacon{spawnBeaconId='" + var10000 + "', offset=" + String.valueOf(this.offset) + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ObjectiveSpawnBeacon.class, ObjectiveSpawnBeacon::new).append(new KeyedCodec("SpawnBeaconId", Codec.STRING), (objectiveSpawnBeacon, s) -> objectiveSpawnBeacon.spawnBeaconId = s, (objectiveSpawnBeacon) -> objectiveSpawnBeacon.spawnBeaconId).addValidator(Validators.nonNull()).addValidator(BeaconNPCSpawn.VALIDATOR_CACHE.getValidator()).add()).append(new KeyedCodec("Offset", Vector3d.CODEC), (objectiveSpawnBeacon, vector3d) -> objectiveSpawnBeacon.offset = vector3d, (objectiveSpawnBeacon) -> objectiveSpawnBeacon.offset).add()).append(new KeyedCodec("WorldLocationCondition", WorldLocationProvider.CODEC), (objectiveSpawnBeacon, worldLocationCondition) -> objectiveSpawnBeacon.worldLocationProvider = worldLocationCondition, (objectiveSpawnBeacon) -> objectiveSpawnBeacon.worldLocationProvider).add()).build();
      }
   }
}
