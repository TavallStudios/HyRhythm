package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

public class FixedTradeSlot extends TradeSlot {
   @Nonnull
   public static final BuilderCodec<FixedTradeSlot> CODEC;
   protected BarterTrade trade;

   public FixedTradeSlot(@Nonnull BarterTrade trade) {
      this.trade = trade;
   }

   protected FixedTradeSlot() {
   }

   @Nonnull
   public BarterTrade getTrade() {
      return this.trade;
   }

   @Nonnull
   public List<BarterTrade> resolve(@Nonnull Random random) {
      return Collections.singletonList(this.trade);
   }

   public int getSlotCount() {
      return 1;
   }

   @Nonnull
   public String toString() {
      return "FixedTradeSlot{trade=" + String.valueOf(this.trade) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(FixedTradeSlot.class, FixedTradeSlot::new).append(new KeyedCodec("Trade", BarterTrade.CODEC), (slot, trade) -> slot.trade = trade, (slot) -> slot.trade).addValidator(Validators.nonNull()).add()).build();
   }
}
