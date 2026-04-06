package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ItemDurabilityConfig {
   @Nonnull
   public static final BuilderCodec<ItemDurabilityConfig> CODEC;
   @Nonnull
   protected BrokenPenalties brokenPenalties;

   public ItemDurabilityConfig() {
      this.brokenPenalties = BrokenPenalties.DEFAULT;
   }

   @Nonnull
   public BrokenPenalties getBrokenPenalties() {
      return this.brokenPenalties;
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ItemDurabilityConfig.class, ItemDurabilityConfig::new).appendInherited(new KeyedCodec("BrokenPenalties", BrokenPenalties.CODEC), (itemDurabilityConfig, itemTypeDoubleMap) -> itemDurabilityConfig.brokenPenalties = itemTypeDoubleMap, (itemDurabilityConfig) -> itemDurabilityConfig.brokenPenalties, (o, p) -> o.brokenPenalties = p.brokenPenalties).addValidator(Validators.nonNull()).add()).build();
   }
}
