package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringArrayNoEmptyStringsValidator extends StringArrayValidator {
   private static final StringArrayNoEmptyStringsValidator INSTANCE = new StringArrayNoEmptyStringsValidator();

   private StringArrayNoEmptyStringsValidator() {
   }

   public boolean test(@Nullable String[] list) {
      if (list == null) {
         return true;
      } else {
         for(String s : list) {
            if (s == null || s.isEmpty()) {
               return false;
            }
         }

         return true;
      }
   }

   @Nonnull
   public String errorMessage(@Nullable String name, String[] list) {
      if (name == null) {
         name = "StringList";
      } else {
         name = "'" + name + "'";
      }

      return name + " must not contain empty strings";
   }

   @Nonnull
   public String errorMessage(String[] list) {
      return this.errorMessage((String)null, list);
   }

   public static StringArrayNoEmptyStringsValidator get() {
      return INSTANCE;
   }
}
