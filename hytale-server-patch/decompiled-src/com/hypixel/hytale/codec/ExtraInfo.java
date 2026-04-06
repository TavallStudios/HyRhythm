package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ThrowingValidationResults;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.logger.util.GithubMessageUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class ExtraInfo {
   public static final ThreadLocal<ExtraInfo> THREAD_LOCAL = ThreadLocal.withInitial(ExtraInfo::new);
   public static final String GENERATED_ID_PREFIX = "*";
   public static final int UNSET_VERSION = 2147483647;
   private final int legacyVersion;
   private final int keysInitialSize = this instanceof EmptyExtraInfo ? 0 : 128;
   @Nonnull
   private String[] stringKeys;
   @Nonnull
   private int[] intKeys;
   private int[] lineNumbers;
   private int[] columnNumbers;
   private int keysSize;
   @Nonnull
   private String[] ignoredUnknownKeys;
   private int ignoredUnknownSize;
   private final List<String> unknownKeys;
   private final ValidationResults validationResults;
   private final CodecStore codecStore;
   /** @deprecated */
   @Deprecated
   private final Map<String, Object> metadata;

   public ExtraInfo() {
      this.stringKeys = new String[this.keysInitialSize];
      this.intKeys = new int[this.keysInitialSize];
      this.lineNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.columnNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.ignoredUnknownKeys = new String[this.keysInitialSize];
      this.unknownKeys = new ObjectArrayList();
      this.metadata = new Object2ObjectOpenHashMap();
      this.legacyVersion = 2147483647;
      this.validationResults = new ThrowingValidationResults(this);
      this.codecStore = CodecStore.STATIC;
   }

   /** @deprecated */
   @Deprecated
   public ExtraInfo(int version) {
      this.stringKeys = new String[this.keysInitialSize];
      this.intKeys = new int[this.keysInitialSize];
      this.lineNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.columnNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.ignoredUnknownKeys = new String[this.keysInitialSize];
      this.unknownKeys = new ObjectArrayList();
      this.metadata = new Object2ObjectOpenHashMap();
      this.legacyVersion = version;
      this.validationResults = new ThrowingValidationResults(this);
      this.codecStore = CodecStore.STATIC;
   }

   /** @deprecated */
   @Deprecated
   public ExtraInfo(int version, @Nonnull Function<ExtraInfo, ValidationResults> validationResultsSupplier) {
      this.stringKeys = new String[this.keysInitialSize];
      this.intKeys = new int[this.keysInitialSize];
      this.lineNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.columnNumbers = GithubMessageUtil.isGithub() ? new int[this.keysInitialSize] : null;
      this.ignoredUnknownKeys = new String[this.keysInitialSize];
      this.unknownKeys = new ObjectArrayList();
      this.metadata = new Object2ObjectOpenHashMap();
      this.legacyVersion = version;
      this.validationResults = (ValidationResults)validationResultsSupplier.apply(this);
      this.codecStore = CodecStore.STATIC;
   }

   public int getVersion() {
      return 2147483647;
   }

   /** @deprecated */
   @Deprecated
   public int getLegacyVersion() {
      return this.legacyVersion;
   }

   public int getKeysSize() {
      return this.keysSize;
   }

   public CodecStore getCodecStore() {
      return this.codecStore;
   }

   private int nextKeyIndex() {
      int index = this.keysSize++;
      if (this.stringKeys.length <= index) {
         int newLength = grow(index);
         this.stringKeys = (String[])Arrays.copyOf(this.stringKeys, newLength);
         this.intKeys = Arrays.copyOf(this.intKeys, newLength);
         if (GithubMessageUtil.isGithub()) {
            this.lineNumbers = Arrays.copyOf(this.lineNumbers, newLength);
            this.columnNumbers = Arrays.copyOf(this.columnNumbers, newLength);
         }
      }

      return index;
   }

   public void pushKey(String key) {
      int index = this.nextKeyIndex();
      this.stringKeys[index] = key;
   }

   public void pushIntKey(int key) {
      int index = this.nextKeyIndex();
      this.intKeys[index] = key;
   }

   public void pushKey(String key, RawJsonReader reader) {
      int index = this.nextKeyIndex();
      this.stringKeys[index] = key;
      if (GithubMessageUtil.isGithub()) {
         this.lineNumbers[index] = reader.getLine();
         this.columnNumbers[index] = reader.getColumn();
      }

   }

   public void pushIntKey(int key, RawJsonReader reader) {
      int index = this.nextKeyIndex();
      this.intKeys[index] = key;
      if (GithubMessageUtil.isGithub()) {
         this.lineNumbers[index] = reader.getLine();
         this.columnNumbers[index] = reader.getColumn();
      }

   }

   public void popKey() {
      this.stringKeys[this.keysSize] = null;
      --this.keysSize;
   }

   private int nextIgnoredUnknownIndex() {
      int index = this.ignoredUnknownSize++;
      if (this.ignoredUnknownKeys.length <= index) {
         this.ignoredUnknownKeys = (String[])Arrays.copyOf(this.ignoredUnknownKeys, grow(index));
      }

      return index;
   }

   public void ignoreUnusedKey(String key) {
      int index = this.nextIgnoredUnknownIndex();
      this.ignoredUnknownKeys[index] = key;
   }

   public void popIgnoredUnusedKey() {
      this.ignoredUnknownKeys[this.ignoredUnknownSize] = null;
      --this.ignoredUnknownSize;
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      if (this.ignoredUnknownSize <= 0) {
         return false;
      } else {
         int lastIndex = this.ignoredUnknownSize - 1;
         String ignoredUnknownKey = this.ignoredUnknownKeys[lastIndex];
         if (ignoredUnknownKey == null) {
            return false;
         } else if (!reader.tryConsumeString(ignoredUnknownKey)) {
            return false;
         } else {
            this.ignoredUnknownKeys[lastIndex] = null;
            return true;
         }
      }
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull String key) {
      if (this.ignoredUnknownSize <= 0) {
         return false;
      } else {
         int lastIndex = this.ignoredUnknownSize - 1;
         if (!key.equals(this.ignoredUnknownKeys[lastIndex])) {
            return false;
         } else {
            this.ignoredUnknownKeys[lastIndex] = null;
            return true;
         }
      }
   }

   public void readUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      if (!this.consumeIgnoredUnknownKey(reader)) {
         String key = reader.readString();
         if (this.keysSize == 0) {
            this.unknownKeys.add(key);
         } else {
            List var10000 = this.unknownKeys;
            String var10001 = this.peekKey();
            var10000.add(var10001 + "." + key);
         }

      }
   }

   public void addUnknownKey(@Nonnull String key) {
      switch (key) {
         case "$Title":
         case "$Comment":
         case "$TODO":
         case "$Author":
         case "$Position":
         case "$FloatingFunctionNodes":
         case "$Groups":
         case "$WorkspaceID":
         case "$NodeEditorMetadata":
         case "$NodeId":
            return;
         default:
            if (!this.consumeIgnoredUnknownKey(key)) {
               if (this.keysSize == 0) {
                  if ("Parent".equals(key)) {
                     return;
                  }

                  this.unknownKeys.add(key);
               } else {
                  List var10000 = this.unknownKeys;
                  String var10001 = this.peekKey();
                  var10000.add(var10001 + "." + key);
               }

            }
      }
   }

   public String peekKey() {
      return this.peekKey('.');
   }

   public String peekKey(char separator) {
      if (this.keysSize == 0) {
         return "";
      } else if (this.keysSize == 1) {
         String str = this.stringKeys[0];
         return str != null ? str : String.valueOf(this.intKeys[0]);
      } else {
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < this.keysSize; ++i) {
            if (i > 0) {
               sb.append(separator);
            }

            String str = this.stringKeys[i];
            if (str != null) {
               sb.append(str);
            } else {
               sb.append(this.intKeys[i]);
            }
         }

         return sb.toString();
      }
   }

   public int peekLine() {
      return GithubMessageUtil.isGithub() && this.keysSize > 0 ? this.lineNumbers[this.keysSize - 1] : -1;
   }

   public int peekColumn() {
      return GithubMessageUtil.isGithub() && this.keysSize > 0 ? this.columnNumbers[this.keysSize - 1] : -1;
   }

   public List<String> getUnknownKeys() {
      return this.unknownKeys;
   }

   public ValidationResults getValidationResults() {
      return this.validationResults;
   }

   /** @deprecated */
   @Deprecated
   public Map<String, Object> getMetadata() {
      return this.metadata;
   }

   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      sb.append("ExtraInfo\n");
   }

   @Nonnull
   public String toString() {
      int var10000 = this.legacyVersion;
      return "ExtraInfo{version=" + var10000 + ", stringKeys=" + Arrays.toString(this.stringKeys) + ", intKeys=" + Arrays.toString(this.intKeys) + ", keysSize=" + this.keysSize + ", ignoredUnknownKeys=" + Arrays.toString(this.ignoredUnknownKeys) + ", ignoredUnknownSize=" + this.ignoredUnknownSize + ", unknownKeys=" + String.valueOf(this.unknownKeys) + ", validationResults=" + String.valueOf(this.validationResults) + "}";
   }

   private static int grow(int oldSize) {
      return oldSize + (oldSize >> 1);
   }
}
