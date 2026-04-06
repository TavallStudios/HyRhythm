package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public class ASTOperandEmptyArray extends ASTOperand {
   public ASTOperandEmptyArray(@Nonnull Token token, int tokenPosition) {
      super(ValueType.EMPTY_ARRAY, token, tokenPosition);
      this.codeGen = (scope) -> ExecutionContext.genPUSHEmptyArray();
   }

   public boolean isConstant() {
      return true;
   }

   @Nonnull
   public ExecutionContext.Operand asOperand() {
      ExecutionContext.Operand op = new ExecutionContext.Operand();
      op.setEmptyArray();
      return op;
   }
}
