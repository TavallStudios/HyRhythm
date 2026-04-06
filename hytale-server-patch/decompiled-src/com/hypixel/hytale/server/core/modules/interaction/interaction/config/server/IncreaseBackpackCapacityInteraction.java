package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class IncreaseBackpackCapacityInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<IncreaseBackpackCapacityInteraction> CODEC;
   private short capacity = 1;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Server;
   }

   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      Player playerComponent = (Player)context.getCommandBuffer().getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Inventory inventory = playerComponent.getInventory();
         short newBackpackCapacity = (short)(inventory.getBackpack().getCapacity() + this.capacity);
         inventory.resizeBackpack(newBackpackCapacity, (List)null);
         playerComponent.sendMessage(Message.translation("server.commands.inventory.backpack.size").param("capacity", inventory.getBackpack().getCapacity()));
         context.getHeldItemContainer().removeItemStackFromSlot((short)context.getHeldItemSlot(), context.getHeldItem(), 1);
      }
   }

   public String toString() {
      short var10000 = this.capacity;
      return "IncreaseBackpackCapacityInteraction{capacity=" + var10000 + "}" + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(IncreaseBackpackCapacityInteraction.class, IncreaseBackpackCapacityInteraction::new, SimpleInstantInteraction.CODEC).documentation("Increase the player's backpack capacity.")).appendInherited(new KeyedCodec("Capacity", Codec.SHORT), (i, s) -> i.capacity = s, (i) -> i.capacity, (i, parent) -> i.capacity = parent.capacity).documentation("Defines the amount by which the backpack capacity needs to be increased.").addValidator(Validators.min(Short.valueOf((short)1))).add()).build();
   }
}
