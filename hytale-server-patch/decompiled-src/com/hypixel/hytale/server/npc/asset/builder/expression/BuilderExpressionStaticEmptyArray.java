package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionStaticEmptyArray extends BuilderExpression {
   public static final BuilderExpressionStaticEmptyArray INSTANCE = new BuilderExpressionStaticEmptyArray();

   @Nonnull
   public ValueType getType() {
      return ValueType.EMPTY_ARRAY;
   }

   public boolean isStatic() {
      return true;
   }

   public double[] getNumberArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_DOUBLE_ARRAY;
   }

   public int[] getIntegerArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_INT_ARRAY;
   }

   @Nonnull
   public String[] getStringArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
   }

   public boolean[] getBooleanArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_BOOLEAN_ARRAY;
   }

   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addConstEmptyArray(name);
   }

   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValueToEmptyArray(name);
   }
}
