package com.hypixel.hytale.builtin.adventure.shop;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class GiveItemInteraction extends ChoiceInteraction {
   @Nonnull
   public static final BuilderCodec<GiveItemInteraction> CODEC;
   protected String itemId;
   protected int quantity = 1;

   public GiveItemInteraction(String itemId, int quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
   }

   protected GiveItemInteraction() {
   }

   public String getItemId() {
      return this.itemId;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public void run(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         playerComponent.getInventory().getCombinedHotbarFirst().addItemStack(new ItemStack(this.itemId, this.quantity));
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = this.itemId;
      return "GiveItemInteraction{itemId=" + var10000 + ", quantity=" + this.quantity + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(GiveItemInteraction.class, GiveItemInteraction::new, ChoiceInteraction.BASE_CODEC).append(new KeyedCodec("ItemId", Codec.STRING), (giveItemInteraction, blockTypeKey) -> giveItemInteraction.itemId = blockTypeKey, (giveItemInteraction) -> giveItemInteraction.itemId).addValidator(Validators.nonNull()).addValidator(Item.VALIDATOR_CACHE.getValidator()).add()).append(new KeyedCodec("Quantity", Codec.INTEGER), (giveItemInteraction, integer) -> giveItemInteraction.quantity = integer, (giveItemInteraction) -> giveItemInteraction.quantity).addValidator(Validators.greaterThanOrEqual(1)).add()).build();
   }
}
