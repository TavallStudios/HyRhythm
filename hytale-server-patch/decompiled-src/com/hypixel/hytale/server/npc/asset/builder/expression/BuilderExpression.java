package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.codec.schema.NamedSchema;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.SchemaConvertable;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.BooleanSchema;
import com.hypixel.hytale.codec.schema.config.NumberSchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderExpression {
   public static final String STATIC = "<STATIC>";

   public abstract ValueType getType();

   public abstract boolean isStatic();

   public double getNumber(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading number is not supported");
   }

   public String getString(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading string is not supported");
   }

   public boolean getBoolean(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading boolean is not supported");
   }

   public double[] getNumberArray(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading number array is not supported");
   }

   public int[] getIntegerArray(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading integer is not supported");
   }

   @Nullable
   public String[] getStringArray(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading string array is not supported");
   }

   public boolean[] getBooleanArray(ExecutionContext executionContext) {
      throw new IllegalStateException("BuilderExpression: Reading boolean array is not supported");
   }

   public void addToScope(String name, StdScope scope) {
      throw new IllegalStateException("This type of builder expression cannot be added to a scope");
   }

   public void updateScope(StdScope scope, String name, ExecutionContext executionContext) {
      throw new IllegalStateException("This type of builder expression cannot update a scope");
   }

   public String getExpression() {
      return "<STATIC>";
   }

   @Nonnull
   public static BuilderExpression fromOperand(@Nonnull ExecutionContext.Operand operand) {
      Object var10000;
      switch (operand.type) {
         case NUMBER -> var10000 = new BuilderExpressionStaticNumber(operand.number);
         case STRING -> var10000 = new BuilderExpressionStaticString(operand.string);
         case BOOLEAN -> var10000 = new BuilderExpressionStaticBoolean(operand.bool);
         case EMPTY_ARRAY -> var10000 = BuilderExpressionStaticEmptyArray.INSTANCE;
         case NUMBER_ARRAY -> var10000 = new BuilderExpressionStaticNumberArray(operand.numberArray);
         case STRING_ARRAY -> var10000 = new BuilderExpressionStaticStringArray(operand.stringArray);
         case BOOLEAN_ARRAY -> var10000 = new BuilderExpressionStaticBooleanArray(operand.boolArray);
         default -> throw new IllegalStateException("Operand cannot be converted to builder expression");
      }

      return (BuilderExpression)var10000;
   }

   @Nonnull
   public static BuilderExpression fromJSON(@Nonnull JsonElement jsonElement, @Nonnull BuilderParameters builderParameters, boolean constantsOnly) {
      BuilderExpression builderExpression = fromJSON(jsonElement, builderParameters);
      if (constantsOnly && !builderExpression.isStatic()) {
         throw new IllegalArgumentException("Only constant string, number or boolean or arrays allowed, found: " + String.valueOf(jsonElement));
      } else {
         return builderExpression;
      }
   }

   @Nonnull
   public static BuilderExpression fromJSON(@Nonnull JsonElement jsonElement, @Nonnull BuilderParameters builderParameters, ValueType expectedType) {
      BuilderExpression builderExpression = fromJSON(jsonElement, builderParameters);
      if (!ValueType.isAssignableType(builderExpression.getType(), expectedType)) {
         String var10002 = String.valueOf(builderExpression.getType());
         throw new IllegalStateException("Expression type mismatch. Got " + var10002 + " but expected " + String.valueOf(expectedType) + " from: " + String.valueOf(jsonElement));
      } else {
         return builderExpression;
      }
   }

   @Nonnull
   public static BuilderExpression fromJSON(@Nonnull JsonElement jsonElement, @Nonnull BuilderParameters builderParameters) {
      if (jsonElement.isJsonObject()) {
         return BuilderExpressionDynamic.fromJSON(jsonElement, builderParameters);
      } else {
         if (jsonElement.isJsonPrimitive()) {
            BuilderExpression jsonPrimitive = readJSONPrimitive(jsonElement);
            if (jsonPrimitive != null) {
               return jsonPrimitive;
            }
         } else if (jsonElement.isJsonArray()) {
            BuilderExpression result = readStaticArray(jsonElement);
            if (result != null) {
               return result;
            }
         }

         throw new IllegalArgumentException("Illegal JSON value for expression: " + String.valueOf(jsonElement));
      }
   }

   @Nullable
   private static BuilderExpression readJSONPrimitive(@Nonnull JsonElement jsonElement) {
      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      if (jsonPrimitive.isString()) {
         return new BuilderExpressionStaticString(jsonPrimitive.getAsString());
      } else if (jsonPrimitive.isBoolean()) {
         return new BuilderExpressionStaticBoolean(jsonPrimitive.getAsBoolean());
      } else {
         return jsonPrimitive.isNumber() ? new BuilderExpressionStaticNumber(jsonPrimitive.getAsDouble()) : null;
      }
   }

   @Nullable
   private static BuilderExpression readStaticArray(@Nonnull JsonElement jsonElement) {
      JsonArray jsonArray = jsonElement.getAsJsonArray();
      if (jsonArray.isEmpty()) {
         return BuilderExpressionStaticEmptyArray.INSTANCE;
      } else {
         JsonElement firstElement = jsonArray.get(0);
         BuilderExpression result = null;
         if (firstElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = firstElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
               result = BuilderExpressionStaticStringArray.fromJSON(jsonArray);
            } else if (jsonPrimitive.isBoolean()) {
               result = BuilderExpressionStaticBooleanArray.fromJSON(jsonArray);
            } else if (jsonPrimitive.isNumber()) {
               result = BuilderExpressionStaticNumberArray.fromJSON(jsonArray);
            }
         }

         return result;
      }
   }

   public void compile(BuilderParameters builderParameters) {
   }

   @Nonnull
   public static Schema toSchema(@Nonnull SchemaContext context) {
      return context.refDefinition(BuilderExpression.SchemaGenerator.INSTANCE);
   }

   private static class SchemaGenerator implements SchemaConvertable<Void>, NamedSchema {
      @Nonnull
      public static SchemaGenerator INSTANCE = new SchemaGenerator();

      @Nonnull
      public String getSchemaName() {
         return "NPC:Type:BuilderExpression";
      }

      @Nonnull
      public Schema toSchema(@Nonnull SchemaContext context) {
         Schema s = new Schema();
         s.setTitle("Expression");
         s.setAnyOf(new ArraySchema(), new NumberSchema(), new StringSchema(), new BooleanSchema(), BuilderExpressionDynamic.toSchema());
         return s;
      }
   }
}
