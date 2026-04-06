package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

public class PoolTradeSlot extends TradeSlot {
   @Nonnull
   public static final BuilderCodec<PoolTradeSlot> CODEC;
   protected int slotCount = 1;
   protected WeightedTrade[] trades;

   public PoolTradeSlot(int slotCount, @Nonnull WeightedTrade[] trades) {
      this.trades = WeightedTrade.EMPTY_ARRAY;
      this.slotCount = slotCount;
      this.trades = trades;
   }

   protected PoolTradeSlot() {
      this.trades = WeightedTrade.EMPTY_ARRAY;
   }

   public int getPoolSlotCount() {
      return this.slotCount;
   }

   @Nonnull
   public WeightedTrade[] getTrades() {
      return this.trades;
   }

   @Nonnull
   public List<BarterTrade> resolve(@Nonnull Random random) {
      List<BarterTrade> result = new ObjectArrayList(this.slotCount);
      if (this.trades.length == 0) {
         return result;
      } else {
         ObjectArrayList<WeightedTrade> available = new ObjectArrayList(this.trades.length);
         available.addAll(Arrays.asList(this.trades));
         int toSelect = Math.min(this.slotCount, available.size());

         for(int i = 0; i < toSelect; ++i) {
            int selectedIndex = selectWeightedIndex(available, random);
            if (selectedIndex >= 0) {
               WeightedTrade selected = (WeightedTrade)available.remove(selectedIndex);
               result.add(selected.toBarterTrade(random));
            }
         }

         return result;
      }
   }

   public int getSlotCount() {
      return this.slotCount;
   }

   private static int selectWeightedIndex(@Nonnull List<WeightedTrade> trades, @Nonnull Random random) {
      if (trades.isEmpty()) {
         return -1;
      } else {
         double totalWeight = 0.0;

         for(WeightedTrade trade : trades) {
            totalWeight += trade.getWeight();
         }

         if (totalWeight <= 0.0) {
            return random.nextInt(trades.size());
         } else {
            double roll = random.nextDouble() * totalWeight;
            double cumulative = 0.0;

            for(int i = 0; i < trades.size(); ++i) {
               cumulative += ((WeightedTrade)trades.get(i)).getWeight();
               if (roll < cumulative) {
                  return i;
               }
            }

            return trades.size() - 1;
         }
      }
   }

   @Nonnull
   public String toString() {
      int var10000 = this.slotCount;
      return "PoolTradeSlot{slotCount=" + var10000 + ", trades=" + Arrays.toString(this.trades) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PoolTradeSlot.class, PoolTradeSlot::new).append(new KeyedCodec("SlotCount", Codec.INTEGER), (slot, count) -> slot.slotCount = count, (slot) -> slot.slotCount).addValidator(Validators.greaterThanOrEqual(1)).add()).append(new KeyedCodec("Trades", new ArrayCodec(WeightedTrade.CODEC, (x$0) -> new WeightedTrade[x$0])), (slot, trades) -> slot.trades = trades, (slot) -> slot.trades).addValidator(Validators.nonNull()).add()).build();
   }
}
