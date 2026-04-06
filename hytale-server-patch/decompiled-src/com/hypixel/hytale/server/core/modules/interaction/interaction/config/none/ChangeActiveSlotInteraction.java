package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionCooldown;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ChangeActiveSlotInteraction extends Interaction {
   @Nonnull
   public static final ChangeActiveSlotInteraction DEFAULT_INTERACTION = new ChangeActiveSlotInteraction("*Change_Active_Slot");
   @Nonnull
   public static final RootInteraction DEFAULT_ROOT;
   /** @deprecated */
   @Deprecated
   public static final MetaKey<Runnable> PLACE_MOVED_ITEM;
   private static final int UNSET_INT = -2147483648;
   @Nonnull
   public static final BuilderCodec<ChangeActiveSlotInteraction> CODEC;
   protected int targetSlot = -2147483648;

   public ChangeActiveSlotInteraction() {
   }

   private ChangeActiveSlotInteraction(@Nonnull String id) {
      super(id);
      this.cancelOnItemChange = false;
   }

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      if (!firstRun) {
         context.getState().state = InteractionState.Finished;
      } else {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         Ref<EntityStore> ref = context.getEntity();
         Entity var9 = EntityUtils.getEntity(ref, commandBuffer);
         if (var9 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)var9;
            DynamicMetaStore var15 = context.getMetaStore();
            byte slot;
            if (this.targetSlot == -2147483648) {
               slot = ((Integer)var15.getMetaObject(TARGET_SLOT)).byteValue();
            } else {
               if (livingEntity.getInventory().getActiveHotbarSlot() == this.targetSlot) {
                  context.getState().state = InteractionState.Finished;
                  return;
               }

               slot = (byte)this.targetSlot;
               var15.putMetaObject(TARGET_SLOT, Integer.valueOf(slot));
            }

            livingEntity.getInventory().setActiveHotbarSlot(slot);
            Runnable action = (Runnable)var15.removeMetaObject(PLACE_MOVED_ITEM);
            if (action != null) {
               action.run();
            }

            InteractionManager interactionManager = context.getInteractionManager();

            assert interactionManager != null;

            InteractionContext forkContext = InteractionContext.forInteraction(interactionManager, ref, InteractionType.SwapTo, commandBuffer);
            String forkInteractions = forkContext.getRootInteractionId(InteractionType.SwapTo);
            if (forkInteractions != null) {
               if (this.targetSlot != -2147483648) {
                  forkContext.getMetaStore().putMetaObject(TARGET_SLOT, Integer.valueOf(slot));
               }

               context.fork(InteractionType.SwapTo, forkContext, RootInteraction.getRootInteractionOrUnknown(forkInteractions), action == null);
            }

            context.getState().state = InteractionState.Finished;
         }
      }
   }

   protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      context.getState().state = context.getServerState().state;
   }

   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      return false;
   }

   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ChangeActiveSlotInteraction();
   }

   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChangeActiveSlotInteraction p = (com.hypixel.hytale.protocol.ChangeActiveSlotInteraction)packet;
      p.targetSlot = this.targetSlot;
   }

   @Nonnull
   public String toString() {
      int var10000 = this.targetSlot;
      return "ChangeActiveSlotInteraction{targetSlot=" + var10000 + "} " + super.toString();
   }

   static {
      DEFAULT_ROOT = new RootInteraction("*Default_Swap", new InteractionCooldown("ChangeActiveSlot", 0.0F, false, InteractionManager.DEFAULT_CHARGE_TIMES, true, false), new String[]{DEFAULT_INTERACTION.getId()});
      PLACE_MOVED_ITEM = CONTEXT_META_REGISTRY.registerMetaObject((i) -> null);
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ChangeActiveSlotInteraction.class, ChangeActiveSlotInteraction::new, Interaction.ABSTRACT_CODEC).documentation("Changes the active hotbar slot for the user of the interaction.")).appendInherited(new KeyedCodec("TargetSlot", Codec.INTEGER), (o, i) -> o.targetSlot = i == null ? -2147483648 : i, (o) -> o.targetSlot == -2147483648 ? null : o.targetSlot, (o, p) -> o.targetSlot = p.targetSlot).addValidator(Validators.range(0, 8)).add()).afterDecode((i) -> i.cancelOnItemChange = false)).build();
   }
}
