package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;

public class ItemContext {
   @Nonnull
   private final ItemContainer container;
   private final short slot;
   @Nonnull
   private final ItemStack itemStack;

   public ItemContext(@Nonnull ItemContainer container, short slot, @Nonnull ItemStack itemStack) {
      this.container = container;
      this.slot = slot;
      this.itemStack = itemStack;
   }

   @Nonnull
   public ItemContainer getContainer() {
      return this.container;
   }

   public short getSlot() {
      return this.slot;
   }

   @Nonnull
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.container);
      return "ItemContext{container=" + var10000 + ", slot=" + this.slot + ", itemStack=" + String.valueOf(this.itemStack) + "}";
   }
}
