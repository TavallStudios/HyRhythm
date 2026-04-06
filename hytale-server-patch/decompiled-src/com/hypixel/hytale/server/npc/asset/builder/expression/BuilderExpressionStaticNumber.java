package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionStaticNumber extends BuilderExpression {
   private final double number;

   public BuilderExpressionStaticNumber(double number) {
      this.number = number;
   }

   @Nonnull
   public ValueType getType() {
      return ValueType.NUMBER;
   }

   public boolean isStatic() {
      return true;
   }

   public double getNumber(ExecutionContext executionContext) {
      return this.number;
   }

   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.number);
   }

   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.number);
   }
}
