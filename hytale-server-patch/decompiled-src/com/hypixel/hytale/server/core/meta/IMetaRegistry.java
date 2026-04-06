package com.hypixel.hytale.server.core.meta;

import com.hypixel.hytale.codec.Codec;
import java.util.function.Function;
import javax.annotation.Nullable;

public interface IMetaRegistry<K> {
   <T> T newMetaObject(MetaKey<T> var1, K var2);

   void forEachMetaEntry(IMetaStore<K> var1, MetaEntryConsumer var2);

   @Nullable
   PersistentMetaKey<?> getMetaKeyForCodecKey(String var1);

   <T> MetaKey<T> registerMetaObject(Function<K, T> var1, boolean var2, String var3, Codec<T> var4);

   default <T> MetaKey<T> registerMetaObject(Function<K, T> supplier, String keyName, Codec<T> codec) {
      return this.<T>registerMetaObject(supplier, true, keyName, codec);
   }

   default <T> MetaKey<T> registerMetaObject(Function<K, T> supplier) {
      return this.<T>registerMetaObject(supplier, false, (String)null, (Codec)null);
   }

   default <T> MetaKey<T> registerMetaObject() {
      return this.<T>registerMetaObject((parent) -> null);
   }

   @FunctionalInterface
   public interface MetaEntryConsumer {
      <T> void accept(MetaKey<T> var1, T var2);
   }
}
