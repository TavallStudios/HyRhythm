package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public abstract class ASTOperand extends AST {
   public ASTOperand(@Nonnull ValueType valueType, @Nonnull Token token, int tokenPosition) {
      super(valueType, token, tokenPosition);
   }

   @Nonnull
   public static ASTOperand createFromParsedToken(@Nonnull Parser.ParsedToken operand, @Nonnull CompileContext compileContext) {
      Token token = operand.token;
      int tokenPosition = operand.tokenPosition;
      String tokenString = operand.tokenString;
      switch (token) {
         case STRING:
            return new ASTOperandString(token, tokenPosition, tokenString);
         case NUMBER:
            return new ASTOperandNumber(token, tokenPosition, operand.tokenNumber);
         case IDENTIFIER:
            Scope scope = compileContext.getScope();
            if (scope.isConstant(tokenString)) {
               return createFromScopeConstant(token, tokenPosition, scope, tokenString);
            }

            return new ASTOperandIdentifier(scope.getType(tokenString), token, tokenPosition, tokenString);
         default:
            throw new IllegalStateException("Unknown parser operand type in AST" + String.valueOf(operand.token));
      }
   }

   @Nonnull
   private static ASTOperand createFromScopeConstant(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      ValueType type = scope.getType(identifier);
      byte var6 = 0;
      Object var10000;
      //$FF: var6->value
      switch (type.typeSwitch<invokedynamic>(type, var6)) {
         case -1:
         default:
            throw new IllegalStateException("Illegal constant type encountered" + String.valueOf(type));
         case 0:
            var10000 = new ASTOperandNumber(token, tokenPosition, scope, identifier);
            break;
         case 1:
            var10000 = new ASTOperandString(token, tokenPosition, scope, identifier);
            break;
         case 2:
            var10000 = new ASTOperandBoolean(token, tokenPosition, scope, identifier);
            break;
         case 3:
            var10000 = new ASTOperandNumberArray(token, tokenPosition, scope, identifier);
            break;
         case 4:
            var10000 = new ASTOperandStringArray(token, tokenPosition, scope, identifier);
            break;
         case 5:
            var10000 = new ASTOperandBooleanArray(token, tokenPosition, scope, identifier);
            break;
         case 6:
            var10000 = new ASTOperandEmptyArray(token, tokenPosition);
      }

      return (ASTOperand)var10000;
   }

   @Nonnull
   public static ASTOperand createFromOperand(@Nonnull Token token, int tokenPosition, @Nonnull ExecutionContext.Operand operand) {
      ValueType type = operand.type;
      byte var5 = 0;
      Object var10000;
      //$FF: var5->value
      switch (type.typeSwitch<invokedynamic>(type, var5)) {
         case -1:
         default:
            throw new IllegalStateException("Illegal operand type encountered" + String.valueOf(type));
         case 0:
            var10000 = new ASTOperandNumber(token, tokenPosition, operand.number);
            break;
         case 1:
            var10000 = new ASTOperandString(token, tokenPosition, operand.string);
            break;
         case 2:
            var10000 = new ASTOperandBoolean(token, tokenPosition, operand.bool);
            break;
         case 3:
            var10000 = new ASTOperandNumberArray(token, tokenPosition, operand.numberArray);
            break;
         case 4:
            var10000 = new ASTOperandStringArray(token, tokenPosition, operand.stringArray);
            break;
         case 5:
            var10000 = new ASTOperandBooleanArray(token, tokenPosition, operand.boolArray);
            break;
         case 6:
            var10000 = new ASTOperandEmptyArray(token, tokenPosition);
      }

      return (ASTOperand)var10000;
   }
}
