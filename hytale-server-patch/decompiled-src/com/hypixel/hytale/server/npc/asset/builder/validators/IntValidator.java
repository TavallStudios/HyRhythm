package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public abstract class IntValidator extends Validator {
   public abstract boolean test(int var1);

   public static boolean compare(int value, @Nonnull RelationalOperator op, int c) {
      boolean var10000;
      switch (op) {
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

   public abstract String errorMessage(int var1);

   public abstract String errorMessage(int var1, String var2);
}
