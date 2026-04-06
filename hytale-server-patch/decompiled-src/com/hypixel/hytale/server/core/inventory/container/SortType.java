package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemQuality;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import java.util.Comparator;
import java.util.function.Function;
import javax.annotation.Nonnull;

public enum SortType {
   NAME((i) -> i.getItem().getTranslationKey(), false, false),
   TYPE((i) -> SortType.Dummy.ItemType.getType(i.getItem()), false, true),
   RARITY((i) -> {
      int qualityIndex = i.getItem().getQualityIndex();
      ItemQuality itemQuality = (ItemQuality)ItemQuality.getAssetMap().getAsset(qualityIndex);
      int itemQualityValue = (itemQuality != null ? itemQuality : ItemQuality.DEFAULT_ITEM_QUALITY).getQualityValue();
      return itemQualityValue;
   }, true, true);

   @Nonnull
   public static SortType[] VALUES = values();
   @Nonnull
   private final Comparator<ItemStack> comparator;

   private <U extends Comparable<U>> SortType(@Nonnull final Function<ItemStack, U> key, final boolean inverted, final boolean thenName) {
      Comparator<ItemStack> comp = comparatorFor(key);
      if (inverted) {
         comp = comp.reversed();
      }

      if (thenName) {
         comp = comp.thenComparing(comparatorFor((i) -> i.getItem().getTranslationKey()));
      }

      this.comparator = Comparator.nullsLast(comp);
   }

   @Nonnull
   public Comparator<ItemStack> getComparator() {
      return this.comparator;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.SortType toPacket() {
      com.hypixel.hytale.protocol.SortType var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = com.hypixel.hytale.protocol.SortType.Name;
         case 1 -> var10000 = com.hypixel.hytale.protocol.SortType.Type;
         case 2 -> var10000 = com.hypixel.hytale.protocol.SortType.Rarity;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public static SortType fromPacket(@Nonnull com.hypixel.hytale.protocol.SortType sortType_) {
      SortType var10000;
      switch (sortType_) {
         case Type -> var10000 = TYPE;
         case Rarity -> var10000 = RARITY;
         case Name -> var10000 = NAME;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   private static <U extends Comparable<U>> Comparator<ItemStack> comparatorFor(@Nonnull Function<ItemStack, U> key) {
      return (a, b) -> {
         U akey = (U)((Comparable)key.apply(a));
         U bkey = (U)((Comparable)key.apply(b));
         if (akey == bkey) {
            return 0;
         } else if (akey == null) {
            return 1;
         } else {
            return bkey == null ? -1 : akey.compareTo(bkey);
         }
      };
   }

   static class Dummy {
      static enum ItemType {
         WEAPON,
         ARMOR,
         TOOL,
         ITEM,
         SPECIAL;

         @Nonnull
         private static ItemType getType(@Nonnull Item item) {
            if (item.getWeapon() != null) {
               return WEAPON;
            } else if (item.getArmor() != null) {
               return ARMOR;
            } else if (item.getTool() != null) {
               return TOOL;
            } else {
               return item.getBuilderToolData() != null ? SPECIAL : ITEM;
            }
         }
      }
   }
}
