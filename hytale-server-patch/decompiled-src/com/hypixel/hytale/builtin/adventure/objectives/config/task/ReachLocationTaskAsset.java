package com.hypixel.hytale.builtin.adventure.objectives.config.task;

import com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation.ReachLocationMarkerAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ReachLocationTaskAsset extends ObjectiveTaskAsset {
   @Nonnull
   public static final BuilderCodec<ReachLocationTaskAsset> CODEC;
   protected String targetLocationId;

   @Nonnull
   public ObjectiveTaskAsset.TaskScope getTaskScope() {
      return ObjectiveTaskAsset.TaskScope.PLAYER;
   }

   public String getTargetLocationId() {
      return this.targetLocationId;
   }

   protected boolean matchesAsset0(ObjectiveTaskAsset task) {
      if (task instanceof ReachLocationTaskAsset asset) {
         return Objects.equals(asset.targetLocationId, this.targetLocationId);
      } else {
         return false;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = this.targetLocationId;
      return "ReachLocationTaskAsset{targetLocationId=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ReachLocationTaskAsset.class, ReachLocationTaskAsset::new, BASE_CODEC).append(new KeyedCodec("TargetLocation", Codec.STRING), (reachLocationTaskAsset, vector3i) -> reachLocationTaskAsset.targetLocationId = vector3i, (reachLocationTaskAsset) -> reachLocationTaskAsset.targetLocationId).addValidator(Validators.nonNull()).addValidator(ReachLocationMarkerAsset.VALIDATOR_CACHE.getValidator()).add()).build();
   }
}
