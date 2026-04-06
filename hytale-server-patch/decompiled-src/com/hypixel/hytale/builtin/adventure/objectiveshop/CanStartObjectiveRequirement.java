package com.hypixel.hytale.builtin.adventure.objectiveshop;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceRequirement;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CanStartObjectiveRequirement extends ChoiceRequirement {
   @Nonnull
   public static final BuilderCodec<CanStartObjectiveRequirement> CODEC;
   protected String objectiveId;

   public CanStartObjectiveRequirement(String objectiveId) {
      this.objectiveId = objectiveId;
   }

   protected CanStartObjectiveRequirement() {
   }

   public boolean canFulfillRequirement(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
      return playerComponent == null ? false : ObjectivePlugin.get().canPlayerDoObjective(playerComponent, this.objectiveId);
   }

   @Nonnull
   public String toString() {
      String var10000 = this.objectiveId;
      return "CanStartObjectiveRequirement{objectiveId='" + var10000 + "'} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(CanStartObjectiveRequirement.class, CanStartObjectiveRequirement::new, ChoiceRequirement.BASE_CODEC).append(new KeyedCodec("ObjectiveId", Codec.STRING), (canStartObjectiveRequirement, s) -> canStartObjectiveRequirement.objectiveId = s, (canStartObjectiveRequirement) -> canStartObjectiveRequirement.objectiveId).add()).build();
   }
}
