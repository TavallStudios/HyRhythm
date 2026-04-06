package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ItemSlotFilter extends SlotFilter {
   default boolean test(@Nonnull FilterActionType actionType, @Nonnull ItemContainer container, short slot, @Nullable ItemStack itemStack) {
      boolean var10000;
      switch (actionType) {
         case ADD:
            var10000 = this.test(itemStack != null ? itemStack.getItem() : null);
            break;
         case REMOVE:
         case DROP:
            itemStack = container.getItemStack(slot);
            var10000 = this.test(itemStack != null ? itemStack.getItem() : null);
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   boolean test(@Nullable Item var1);
}
