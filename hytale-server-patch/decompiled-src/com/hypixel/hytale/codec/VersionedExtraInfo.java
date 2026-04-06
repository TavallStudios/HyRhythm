package com.hypixel.hytale.codec;

import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.ValidationResults;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

public class VersionedExtraInfo extends ExtraInfo {
   private final int version;
   private final ExtraInfo delegate;

   public VersionedExtraInfo(int version, ExtraInfo delegate) {
      this.version = version;
      this.delegate = delegate;
   }

   public int getVersion() {
      return this.version;
   }

   public int getKeysSize() {
      return this.delegate.getKeysSize();
   }

   public CodecStore getCodecStore() {
      return this.delegate.getCodecStore();
   }

   public void pushKey(String key) {
      this.delegate.pushKey(key);
   }

   public void pushIntKey(int key) {
      this.delegate.pushIntKey(key);
   }

   public void pushKey(String key, RawJsonReader reader) {
      this.delegate.pushKey(key, reader);
   }

   public void pushIntKey(int key, RawJsonReader reader) {
      this.delegate.pushIntKey(key, reader);
   }

   public void popKey() {
      this.delegate.popKey();
   }

   public void ignoreUnusedKey(String key) {
      this.delegate.ignoreUnusedKey(key);
   }

   public void popIgnoredUnusedKey() {
      this.delegate.popIgnoredUnusedKey();
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      return this.delegate.consumeIgnoredUnknownKey(reader);
   }

   public boolean consumeIgnoredUnknownKey(@Nonnull String key) {
      return this.delegate.consumeIgnoredUnknownKey(key);
   }

   public void readUnknownKey(@Nonnull RawJsonReader reader) throws IOException {
      this.delegate.readUnknownKey(reader);
   }

   public void addUnknownKey(@Nonnull String key) {
      this.delegate.addUnknownKey(key);
   }

   public String peekKey() {
      return this.delegate.peekKey();
   }

   public String peekKey(char separator) {
      return this.delegate.peekKey(separator);
   }

   public List<String> getUnknownKeys() {
      return this.delegate.getUnknownKeys();
   }

   public ValidationResults getValidationResults() {
      return this.delegate.getValidationResults();
   }

   public Map<String, Object> getMetadata() {
      return this.delegate.getMetadata();
   }

   public void appendDetailsTo(@Nonnull StringBuilder sb) {
      this.delegate.appendDetailsTo(sb);
   }

   public int getLegacyVersion() {
      return this.delegate.getLegacyVersion();
   }
}
