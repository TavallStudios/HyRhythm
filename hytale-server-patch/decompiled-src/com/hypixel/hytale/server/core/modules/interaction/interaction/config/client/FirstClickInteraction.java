package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.IInteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FirstClickInteraction extends Interaction {
   @Nonnull
   public static final BuilderCodec<FirstClickInteraction> CODEC;
   @Nonnull
   public static final StringTag TAG_CLICK;
   @Nonnull
   public static final StringTag TAG_HELD;
   private static final int HELD_LABEL_INDEX = 0;
   @Nullable
   protected String click;
   @Nullable
   protected String held;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      InteractionSyncData clientState = context.getClientState();

      assert clientState != null;

      if (clientState.state == InteractionState.Failed && context.hasLabels()) {
         context.getState().state = InteractionState.Failed;
         context.jump(context.getLabel(0));
      } else {
         context.getState().state = InteractionState.Finished;
      }
   }

   protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      InteractionManager interactionManager = context.getInteractionManager();

      assert interactionManager != null;

      IInteractionSimulationHandler simulationHandler = interactionManager.getInteractionSimulationHandler();
      if (!simulationHandler.isCharging(firstRun, time, type, context, ref, cooldownHandler)) {
         context.getState().state = InteractionState.Finished;
      } else {
         context.getState().state = InteractionState.Failed;
      }

   }

   public void compile(@Nonnull OperationsBuilder builder) {
      if (this.click == null && this.held == null) {
         builder.addOperation(this);
      } else {
         Label failedLabel = builder.createUnresolvedLabel();
         Label endLabel = builder.createUnresolvedLabel();
         builder.addOperation(this, failedLabel);
         if (this.click != null) {
            Interaction nextInteraction = Interaction.getInteractionOrUnknown(this.click);
            nextInteraction.compile(builder);
         }

         if (this.held != null) {
            builder.jump(endLabel);
         }

         builder.resolveLabel(failedLabel);
         if (this.held != null) {
            Interaction failedInteraction = Interaction.getInteractionOrUnknown(this.held);
            failedInteraction.compile(builder);
         }

         builder.resolveLabel(endLabel);
      }
   }

   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      if (this.click != null && InteractionManager.walkInteraction(collector, context, TAG_CLICK, this.click)) {
         return true;
      } else {
         return this.held != null && InteractionManager.walkInteraction(collector, context, TAG_HELD, this.held);
      }
   }

   @Nonnull
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.FirstClickInteraction();
   }

   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.FirstClickInteraction p = (com.hypixel.hytale.protocol.FirstClickInteraction)packet;
      p.click = Interaction.getInteractionIdOrUnknown(this.click);
      p.held = Interaction.getInteractionIdOrUnknown(this.held);
   }

   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.click;
      return "FirstClickInteraction{click='" + var10000 + "', held='" + this.held + "'} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(FirstClickInteraction.class, FirstClickInteraction::new, Interaction.ABSTRACT_CODEC).documentation("An interaction that runs a different interaction based on if this chain was from a click or due to the key being held down.")).appendInherited(new KeyedCodec("Click", Interaction.CHILD_ASSET_CODEC), (interaction, s) -> interaction.click = s, (interaction) -> interaction.click, (interaction, parent) -> interaction.click = parent.click).documentation("The interaction to run if this chain was initiated by a click.").addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late()).add()).appendInherited(new KeyedCodec("Held", Interaction.CHILD_ASSET_CODEC), (interaction, s) -> interaction.held = s, (interaction) -> interaction.held, (interaction, parent) -> interaction.held = parent.held).documentation("The interaction to run if this chain was initiated by holding down the key.").addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late()).add()).build();
      TAG_CLICK = StringTag.of("Click");
      TAG_HELD = StringTag.of("Held");
   }
}
