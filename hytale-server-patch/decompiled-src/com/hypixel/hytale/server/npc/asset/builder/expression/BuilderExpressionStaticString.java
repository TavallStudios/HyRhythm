package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionStaticString extends BuilderExpression {
   private final String string;

   public BuilderExpressionStaticString(String string) {
      this.string = string;
   }

   @Nonnull
   public ValueType getType() {
      return ValueType.STRING;
   }

   public boolean isStatic() {
      return true;
   }

   public String getString(ExecutionContext executionContext) {
      return this.string;
   }

   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.string);
   }

   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.string);
   }
}
