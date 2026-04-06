package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import javax.annotation.Nonnull;

public class FilteredBlockFluidCondition implements IBlockFluidCondition {
   private final IBlockFluidCondition filter;
   private final IBlockFluidCondition condition;

   public FilteredBlockFluidCondition(int blockId, IBlockFluidCondition condition) {
      this((block, fluid) -> block == blockId && fluid == 0, condition);
   }

   public FilteredBlockFluidCondition(IBlockFluidCondition filter, IBlockFluidCondition condition) {
      this.filter = filter;
      this.condition = condition;
   }

   public boolean eval(int block, int fluid) {
      return this.filter.eval(block, fluid) ? false : this.condition.eval(block, fluid);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.filter);
      return "FilteredBlockFluidCondition{filter=" + var10000 + ", condition=" + String.valueOf(this.condition) + "}";
   }
}
