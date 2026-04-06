package com.hypixel.hytale.server.npc.util.expression;

import java.util.concurrent.ThreadLocalRandom;

public class StdLib extends StdScope {
   private static final StdLib instance = new StdLib();

   private StdLib() {
      super((Scope)null);
      this.addConst("true", true);
      this.addConst("false", false);
      this.addConst("PI", 3.1415927410125732);
      this.addInvariant("max", (context, numArgs) -> context.popPush(Math.max(context.getNumber(0), context.getNumber(1)), 2), ValueType.NUMBER, new ValueType[]{ValueType.NUMBER, ValueType.NUMBER});
      this.addInvariant("min", (context, numArgs) -> context.popPush(Math.min(context.getNumber(0), context.getNumber(1)), 2), ValueType.NUMBER, new ValueType[]{ValueType.NUMBER, ValueType.NUMBER});
      this.addInvariant("isEmpty", (context, numArgs) -> {
         String string = context.getString(0);
         context.popPush(string == null || string.isEmpty(), 1);
      }, ValueType.BOOLEAN, new ValueType[]{ValueType.STRING});
      this.addInvariant("isEmptyStringArray", (context, numArgs) -> context.popPush(context.getStringArray(0).length == 0, 1), ValueType.BOOLEAN, new ValueType[]{ValueType.STRING_ARRAY});
      this.addInvariant("isEmptyNumberArray", (context, numArgs) -> context.popPush(context.getNumberArray(0).length == 0, 1), ValueType.BOOLEAN, new ValueType[]{ValueType.NUMBER_ARRAY});
      this.addVariant("random", (context, numArgs) -> context.push(ThreadLocalRandom.current().nextDouble()), ValueType.NUMBER, new ValueType[0]);
      this.addVariant("randomInRange", (context, numArgs) -> context.popPush(ThreadLocalRandom.current().nextDouble(context.getNumber(1), context.getNumber(0)), 2), ValueType.NUMBER, new ValueType[]{ValueType.NUMBER, ValueType.NUMBER});
      this.addInvariant("makeRange", (context, numArgs) -> {
         double value = context.getNumber(0);
         context.popPush((double[])(new double[]{value, value}), 1);
      }, ValueType.NUMBER_ARRAY, new ValueType[]{ValueType.NUMBER});
   }

   public static StdScope getInstance() {
      return instance;
   }
}
