package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public abstract class DoubleValidator extends Validator {
   public abstract boolean test(double var1);

   public static boolean compare(double value, @Nonnull RelationalOperator predicate, double c) {
      boolean var10000;
      switch (predicate) {
         case NotEqual -> var10000 = value != c;
         case Less -> var10000 = value < c;
         case LessEqual -> var10000 = value <= c;
         case Greater -> var10000 = value > c;
         case GreaterEqual -> var10000 = value >= c;
         case Equal -> var10000 = value == c;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public abstract String errorMessage(double var1);

   public abstract String errorMessage(double var1, String var3);
}
