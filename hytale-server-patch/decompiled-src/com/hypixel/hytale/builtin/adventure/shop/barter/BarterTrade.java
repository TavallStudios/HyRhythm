package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BarterTrade {
   @Nonnull
   public static final BuilderCodec<BarterTrade> CODEC;
   protected BarterItemStack output;
   protected BarterItemStack[] input;
   protected int maxStock = 10;

   public BarterTrade(BarterItemStack output, BarterItemStack[] input, int maxStock) {
      this.output = output;
      this.input = input;
      this.maxStock = maxStock;
   }

   protected BarterTrade() {
   }

   public BarterItemStack getOutput() {
      return this.output;
   }

   public BarterItemStack[] getInput() {
      return this.input;
   }

   public int getMaxStock() {
      return this.maxStock;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.output);
      return "BarterTrade{output=" + var10000 + ", input=" + Arrays.toString(this.input) + ", maxStock=" + this.maxStock + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BarterTrade.class, BarterTrade::new).append(new KeyedCodec("Output", BarterItemStack.CODEC), (trade, stack) -> trade.output = stack, (trade) -> trade.output).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Input", new ArrayCodec(BarterItemStack.CODEC, (x$0) -> new BarterItemStack[x$0])), (trade, stacks) -> trade.input = stacks, (trade) -> trade.input).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Stock", Codec.INTEGER), (trade, i) -> trade.maxStock = i, (trade) -> trade.maxStock).addValidator(Validators.greaterThanOrEqual(1)).add()).build();
   }
}
