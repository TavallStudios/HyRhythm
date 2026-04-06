package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperatorTuple extends ASTOperator {
   public ASTOperatorTuple(@Nonnull ValueType arrayType, @Nonnull Token token, int tokenPosition) {
      super(arrayType, token, tokenPosition);
      this.codeGen = (scope) -> ExecutionContext.genPACK(this.getValueType(), this.getArguments().size());
   }

   public boolean isConstant() {
      return false;
   }

   public static void fromParsedTuple(@Nonnull Parser.ParsedToken openingToken, int argumentCount, @Nonnull CompileContext compileContext) {
      Token token = openingToken.token;
      if (token != Token.OPEN_SQUARE_BRACKET) {
         throw new IllegalStateException("Bad opening bracket for tuple: " + token.get());
      } else {
         int tokenPosition = openingToken.tokenPosition;
         Stack<AST> operandStack = compileContext.getOperandStack();
         if (argumentCount == 0) {
            operandStack.push(new ASTOperandEmptyArray(token, tokenPosition));
         } else {
            int len = operandStack.size();
            int firstArgument = len - argumentCount;
            ValueType argumentType = ((AST)operandStack.get(firstArgument)).getValueType();
            ValueType var10000;
            switch (argumentType) {
               case NUMBER -> var10000 = ValueType.NUMBER_ARRAY;
               case STRING -> var10000 = ValueType.STRING_ARRAY;
               case BOOLEAN -> var10000 = ValueType.BOOLEAN_ARRAY;
               default -> throw new IllegalStateException("Invalid type in array: " + String.valueOf(argumentType));
            }

            ValueType arrayType = var10000;
            boolean isConstant = true;

            for(int i = firstArgument; i < len; ++i) {
               AST ast = (AST)operandStack.get(i);
               isConstant &= ast.isConstant();
               if (ast.getValueType() != argumentType) {
                  String var10002 = String.valueOf(argumentType);
                  throw new IllegalStateException("Mismatching types in array. Expected " + var10002 + ", found " + String.valueOf(ast.getValueType()));
               }
            }

            if (isConstant) {
               Object var16;
               switch (arrayType) {
                  case NUMBER_ARRAY -> var16 = new ASTOperandNumberArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  case STRING_ARRAY -> var16 = new ASTOperandStringArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  case BOOLEAN_ARRAY -> var16 = new ASTOperandBooleanArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  default -> throw new IllegalStateException("Unexpected array type when creating constant array: " + String.valueOf(arrayType));
               }

               ASTOperand item = (ASTOperand)var16;
               operandStack.setSize(firstArgument);
               operandStack.push(item);
            } else {
               ASTOperatorTuple ast = new ASTOperatorTuple(arrayType, token, tokenPosition);

               for(int i = firstArgument; i < len; ++i) {
                  ast.addArgument((AST)operandStack.get(i));
               }

               operandStack.setSize(firstArgument);
               operandStack.push(ast);
            }
         }
      }
   }
}
