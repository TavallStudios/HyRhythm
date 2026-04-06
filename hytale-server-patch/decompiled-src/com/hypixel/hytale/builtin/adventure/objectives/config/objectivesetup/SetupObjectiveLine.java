package com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLineAsset;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetupObjectiveLine extends ObjectiveTypeSetup {
   @Nonnull
   public static final BuilderCodec<SetupObjectiveLine> CODEC;
   protected String objectiveLineId;

   @Nullable
   public String getObjectiveIdToStart() {
      ObjectiveLineAsset objectiveLineAsset = (ObjectiveLineAsset)ObjectiveLineAsset.getAssetMap().getAsset(this.objectiveLineId);
      return objectiveLineAsset != null ? objectiveLineAsset.getObjectiveIds()[0] : null;
   }

   @Nullable
   public Objective setup(@Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID, @Nonnull Store<EntityStore> store) {
      return ObjectivePlugin.get().startObjectiveLine(store, this.objectiveLineId, playerUUIDs, worldUUID, markerUUID);
   }

   @Nonnull
   public String toString() {
      String var10000 = this.objectiveLineId;
      return "SetupObjectiveLine{objectiveLineId='" + var10000 + "'} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(SetupObjectiveLine.class, SetupObjectiveLine::new).append(new KeyedCodec("ObjectiveLineId", Codec.STRING), (setupObjectiveLine, s) -> setupObjectiveLine.objectiveLineId = s, (setupObjectiveLine) -> setupObjectiveLine.objectiveLineId).addValidator(Validators.nonNull()).addValidatorLate(() -> ObjectiveLineAsset.VALIDATOR_CACHE.getValidator().late()).add()).build();
   }
}
