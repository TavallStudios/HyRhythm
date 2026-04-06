package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.codec.Codec;
import javax.annotation.Nonnull;

public class PersistentMetaKey<T> extends MetaKey<T> {
   private final String key;
   private final Codec<T> codec;

   PersistentMetaKey(int id, String key, Codec<T> codec) {
      super(id);
      this.key = key;
      this.codec = codec;
   }

   public String getKey() {
      return this.key;
   }

   public Codec<T> getCodec() {
      return this.codec;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.key;
      return "PersistentMetaKey{key=" + var10000 + "codec=" + String.valueOf(this.codec) + "}";
   }
}
