package com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveAsset;
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

public class SetupObjective extends ObjectiveTypeSetup {
   @Nonnull
   public static final BuilderCodec<SetupObjective> CODEC;
   protected String objectiveId;

   public String getObjectiveIdToStart() {
      return this.objectiveId;
   }

   @Nullable
   public Objective setup(@Nonnull Set<UUID> playerUUIDs, @Nonnull UUID worldUUID, @Nullable UUID markerUUID, @Nonnull Store<EntityStore> store) {
      return ObjectivePlugin.get().startObjective(this.objectiveId, playerUUIDs, worldUUID, markerUUID, store);
   }

   @Nonnull
   public String toString() {
      String var10000 = this.objectiveId;
      return "SetupObjective{objectiveId='" + var10000 + "'} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(SetupObjective.class, SetupObjective::new).append(new KeyedCodec("ObjectiveId", Codec.STRING), (setupObjective, s) -> setupObjective.objectiveId = s, (setupObjective) -> setupObjective.objectiveId).addValidator(Validators.nonNull()).addValidatorLate(() -> ObjectiveAsset.VALIDATOR_CACHE.getValidator().late()).add()).build();
   }
}
