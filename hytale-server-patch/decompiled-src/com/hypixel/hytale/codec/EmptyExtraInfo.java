package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ThrowingValidationResults;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** @deprecated */
@Deprecated
public class EmptyExtraInfo extends ExtraInfo {
   public static final EmptyExtraInfo EMPTY = new EmptyExtraInfo();

   private EmptyExtraInfo() {
      super(2147483647, ThrowingValidationResults::new);
   }

   public void pushKey(String key) {
   }

   public void pushIntKey(int i) {
   }

   public void pushKey(String key, RawJsonReader reader) {
   }

   public void pushIntKey(int key, RawJsonReader reader) {
   }

   public void popKey() {
   }

   public void addUnknownKey(@Nonnull String key) {
   }

   public void ignoreUnusedKey(String key) {
   }

   public void popIgnoredUnusedKey() {
   }

   @Nonnull
   public String peekKey() {
      return "<empty>";
   }

   @Nonnull
   public String peekKey(char separator) {
      return "<empty>";
   }

   @Nonnull
   public List<String> getUnknownKeys() {
      return Collections.emptyList();
   }

   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      sb.append("EmptyExtraInfo\n");
   }

   @Nonnull
   public String toString() {
      return "EmptyExtraInfo{} " + super.toString();
   }
}
