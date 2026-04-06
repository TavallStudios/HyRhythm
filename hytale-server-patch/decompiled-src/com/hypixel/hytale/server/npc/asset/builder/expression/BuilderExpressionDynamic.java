package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.codec.schema.config.ObjectSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class BuilderExpressionDynamic extends BuilderExpression {
   public static final String KEY_COMPUTE = "Compute";
   private final String expression;
   private final ExecutionContext.Instruction[] instructionSequence;

   public BuilderExpressionDynamic(String expression, ExecutionContext.Instruction[] instructionSequence) {
      this.expression = expression;
      this.instructionSequence = instructionSequence;
   }

   public boolean isStatic() {
      return false;
   }

   public String getExpression() {
      return this.expression;
   }

   protected void execute(@Nonnull ExecutionContext executionContext) {
      Objects.requireNonNull(executionContext, "ExecutionContext not initialised");
      if (executionContext.execute(this.instructionSequence) != this.getType()) {
         String var10002 = String.valueOf(executionContext.getType());
         throw new IllegalStateException("Expression returned wrong type " + var10002 + " but expected " + String.valueOf(this.getType()) + ": " + this.expression);
      }
   }

   @Nonnull
   public static BuilderExpression fromJSON(@Nonnull JsonElement jsonElement, @Nonnull BuilderParameters builderParameters) {
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonElement computeValue = jsonObject.get("Compute");
      if (computeValue == null) {
         throw new IllegalArgumentException("JSON expression missing 'Compute' member: " + String.valueOf(jsonElement));
      } else {
         String expression = BuilderBase.expectStringElement(computeValue, "Compute");
         ValueType type = builderParameters.compile(expression);
         ExecutionContext.Operand operand = builderParameters.getConstantOperand();
         if (operand != null) {
            return BuilderExpression.fromOperand(operand);
         } else {
            ExecutionContext.Instruction[] instructionSequence = (ExecutionContext.Instruction[])builderParameters.getInstructions().toArray((x$0) -> new ExecutionContext.Instruction[x$0]);
            Object var10000;
            switch (type) {
               case NUMBER -> var10000 = new BuilderExpressionDynamicNumber(expression, instructionSequence);
               case STRING -> var10000 = new BuilderExpressionDynamicString(expression, instructionSequence);
               case BOOLEAN -> var10000 = new BuilderExpressionDynamicBoolean(expression, instructionSequence);
               case NUMBER_ARRAY -> var10000 = new BuilderExpressionDynamicNumberArray(expression, instructionSequence);
               case STRING_ARRAY -> var10000 = new BuilderExpressionDynamicStringArray(expression, instructionSequence);
               case BOOLEAN_ARRAY -> var10000 = new BuilderExpressionDynamicBooleanArray(expression, instructionSequence);
               default -> throw new IllegalStateException("Unable to create dynamic expression from type " + String.valueOf(type));
            }

            return (BuilderExpression)var10000;
         }
      }
   }

   @Nonnull
   public static Schema toSchema() {
      ObjectSchema s = new ObjectSchema();
      s.setTitle("ExpressionDynamic");
      s.setProperties(Map.of("Compute", new StringSchema()));
      s.setRequired(new String[]{"Compute"});
      s.setAdditionalProperties(false);
      return s;
   }

   @Nonnull
   public static Schema computableSchema(Schema toWrap) {
      return Schema.anyOf(toWrap, toSchema());
   }
}
