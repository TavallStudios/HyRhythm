package com.hypixel.hytale.builtin.adventure.objectives.interactions;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup.ObjectiveTypeSetup;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

public class StartObjectiveInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<StartObjectiveInteraction> CODEC;
   @Nonnull
   public static final KeyedCodec<UUID> OBJECTIVE_UUID;
   protected ObjectiveTypeSetup objectiveTypeSetup;

   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Ref<EntityStore> ref = context.getEntity();
      PlayerRef playerRefComponent = (PlayerRef)commandBuffer.getComponent(ref, PlayerRef.getComponentType());
      if (playerRefComponent != null) {
         ItemStack itemStack = context.getHeldItem();
         Store<EntityStore> store = commandBuffer.getStore();
         UUID objectiveUUID = (UUID)itemStack.getFromMetadataOrNull(OBJECTIVE_UUID);
         if (objectiveUUID == null) {
            this.startObjective(playerRefComponent, context, itemStack, store);
         } else {
            ObjectivePlugin.get().addPlayerToExistingObjective(store, playerRefComponent.getUuid(), objectiveUUID);
         }

      }
   }

   private void startObjective(@Nonnull PlayerRef player, @Nonnull InteractionContext context, @Nonnull ItemStack itemStack, @Nonnull Store<EntityStore> store) {
      BsonDocument itemStackMetadata = itemStack.getMetadata();
      if (itemStackMetadata == null) {
         itemStackMetadata = new BsonDocument();
      }

      World world = ((EntityStore)store.getExternalData()).getWorld();
      Objective objective = this.objectiveTypeSetup.setup(Set.of(player.getUuid()), world.getWorldConfig().getUuid(), (UUID)null, store);
      if (objective == null) {
         ObjectivePlugin.get().getLogger().at(Level.WARNING).log("Failed to start objective '%s' from item: %s", this.objectiveTypeSetup.getObjectiveIdToStart(), itemStack);
      } else {
         OBJECTIVE_UUID.put(itemStackMetadata, objective.getObjectiveUUID());
         ItemStack clonedItemStack = itemStack.withMetadata(itemStackMetadata);
         objective.setObjectiveItemStarter(clonedItemStack);
         context.setHeldItem(clonedItemStack);
         context.getHeldItemContainer().replaceItemStackInSlot((short)context.getHeldItemSlot(), itemStack, clonedItemStack);
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.objectiveTypeSetup);
      return "StartObjectiveInteraction{objectiveTypeSetup=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(StartObjectiveInteraction.class, StartObjectiveInteraction::new, SimpleInstantInteraction.CODEC).documentation("Starts the given objective or adds the player to an existing one.")).appendInherited(new KeyedCodec("Setup", ObjectiveTypeSetup.CODEC), (startObjectiveInteraction, objectiveTypeSetup) -> startObjectiveInteraction.objectiveTypeSetup = objectiveTypeSetup, (startObjectiveInteraction) -> startObjectiveInteraction.objectiveTypeSetup, (startObjectiveInteraction, parent) -> startObjectiveInteraction.objectiveTypeSetup = parent.objectiveTypeSetup).addValidator(Validators.nonNull()).add()).build();
      OBJECTIVE_UUID = new KeyedCodec<UUID>("ObjectiveUUID", Codec.UUID_BINARY);
   }
}
