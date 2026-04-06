package com.hypixel.hytale.server.npc.util.expression;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Scope {
   Supplier<String> getStringSupplier(String var1);

   DoubleSupplier getNumberSupplier(String var1);

   BooleanSupplier getBooleanSupplier(String var1);

   Supplier<String[]> getStringArraySupplier(String var1);

   Supplier<double[]> getNumberArraySupplier(String var1);

   Supplier<boolean[]> getBooleanArraySupplier(String var1);

   Function getFunction(String var1);

   default String getString(String name) {
      return (String)this.getStringSupplier(name).get();
   }

   default double getNumber(String name) {
      return this.getNumberSupplier(name).getAsDouble();
   }

   default boolean getBoolean(String name) {
      return this.getBooleanSupplier(name).getAsBoolean();
   }

   default String[] getStringArray(String name) {
      return (String[])this.getStringArraySupplier(name).get();
   }

   default double[] getNumberArray(String name) {
      return (double[])this.getNumberArraySupplier(name).get();
   }

   default boolean[] getBooleanArray(String name) {
      return (boolean[])this.getBooleanArraySupplier(name).get();
   }

   boolean isConstant(String var1);

   @Nullable
   ValueType getType(String var1);

   @Nonnull
   static String encodeFunctionName(@Nonnull String name, @Nonnull ValueType[] values) {
      StringBuilder stringBuilder = (new StringBuilder(name)).append('@');

      for(int i = 0; i < values.length; ++i) {
         stringBuilder.append(encodeType(values[i]));
      }

      return stringBuilder.toString();
   }

   static char encodeType(@Nonnull ValueType type) {
      char var10000;
      switch (type) {
         case NUMBER -> var10000 = 'n';
         case STRING -> var10000 = 's';
         case BOOLEAN -> var10000 = 'b';
         case NUMBER_ARRAY -> var10000 = 'N';
         case STRING_ARRAY -> var10000 = 'S';
         case BOOLEAN_ARRAY -> var10000 = 'B';
         default -> throw new IllegalStateException("Type cannot be encoded for function name: " + String.valueOf(type));
      }

      return var10000;
   }

   @FunctionalInterface
   public interface Function {
      void call(ExecutionContext var1, int var2);
   }
}
