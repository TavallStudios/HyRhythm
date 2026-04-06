package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringNullOrNotEmptyValidator extends StringValidator {
   private static final StringNullOrNotEmptyValidator INSTANCE = new StringNullOrNotEmptyValidator();

   private StringNullOrNotEmptyValidator() {
   }

   public boolean test(@Nullable String value) {
      return value == null || !value.isEmpty();
   }

   @Nonnull
   public String errorMessage(String value) {
      return this.errorMessage0(value, "Value");
   }

   @Nonnull
   public String errorMessage(String value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(String value, String name) {
      return name + " must be null or not be an empty string and is '" + value + "'";
   }

   public static StringNullOrNotEmptyValidator get() {
      return INSTANCE;
   }
}
