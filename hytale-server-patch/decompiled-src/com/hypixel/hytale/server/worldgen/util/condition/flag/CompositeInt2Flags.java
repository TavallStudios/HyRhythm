package com.hypixel.hytale.server.worldgen.util.condition.flag;

import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import javax.annotation.Nonnull;

public class CompositeInt2Flags implements Int2FlagsCondition {
   private final FlagCondition[] flags;
   private final int defaultResult;

   public CompositeInt2Flags(int defaultResult, FlagCondition[] flags) {
      this.flags = flags;
      this.defaultResult = defaultResult;
   }

   public int eval(int input) {
      int output = this.defaultResult;

      for(FlagCondition flag : this.flags) {
         output = flag.eval(input, output);
      }

      return output;
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.flags);
      return "CompositeInt2Flags{flags=" + var10000 + ", defaultResult=" + this.defaultResult + "}";
   }

   public static class FlagCondition implements IntBinaryOperator {
      private final IIntCondition condition;
      private final FlagOperator operator;
      private final int flags;

      public FlagCondition(IIntCondition condition, FlagOperator operator, int flags) {
         this.condition = condition;
         this.operator = operator;
         this.flags = flags;
      }

      public int eval(int input, int output) {
         if (this.condition.eval(input)) {
            output = this.operator.apply(output, this.flags);
         }

         return output;
      }

      public int applyAsInt(int input, int output) {
         return this.eval(input, output);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.condition);
         return "FlagOperator{condition=" + var10000 + ", operator=" + String.valueOf(this.operator) + ", flags=" + this.flags + "}";
      }
   }
}
