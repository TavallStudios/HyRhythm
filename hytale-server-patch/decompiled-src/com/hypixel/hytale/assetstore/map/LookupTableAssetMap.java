package com.hypixel.hytale.assetstore.map;

import com.hypixel.hytale.assetstore.codec.AssetCodec;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LookupTableAssetMap<K, T extends JsonAssetWithMap<K, LookupTableAssetMap<K, T>>> extends AssetMapWithIndexes<K, T> {
   @Nonnull
   private final IntFunction<T[]> arrayProvider;
   private final ToIntFunction<K> indexGetter;
   private final IntSupplier maxIndexGetter;
   private final ReentrantLock arrayLock = new ReentrantLock();
   private T[] array;

   public LookupTableAssetMap(@Nonnull IntFunction<T[]> arrayProvider, ToIntFunction<K> indexGetter, IntSupplier maxIndexGetter) {
      this.arrayProvider = arrayProvider;
      this.indexGetter = indexGetter;
      this.maxIndexGetter = maxIndexGetter;
      this.array = (JsonAssetWithMap[])arrayProvider.apply(0);
   }

   @Nullable
   public T getAsset(int index) {
      return (T)(index >= 0 && index < this.array.length ? this.array[index] : null);
   }

   public T getAssetOrDefault(int index, T def) {
      return (T)(index >= 0 && index < this.array.length ? this.array[index] : def);
   }

   protected void clear() {
      super.clear();
      this.arrayLock.lock();

      try {
         this.array = (JsonAssetWithMap[])this.arrayProvider.apply(0);
      } finally {
         this.arrayLock.unlock();
      }

   }

   protected void putAll(@Nonnull String packKey, @Nonnull AssetCodec<K, T> codec, @Nonnull Map<K, T> loadedAssets, @Nonnull Map<K, Path> loadedKeyToPathMap, @Nonnull Map<K, Set<K>> loadedAssetChildren) {
      super.putAll(packKey, codec, loadedAssets, loadedKeyToPathMap, loadedAssetChildren);
      this.arrayLock.lock();

      try {
         this.resize();

         for(Map.Entry<K, T> entry : loadedAssets.entrySet()) {
            K key = (K)entry.getKey();
            int index = this.indexGetter.applyAsInt(key);
            if (index < 0) {
               throw new IllegalArgumentException("Index can't be less than zero!");
            }

            if (index >= this.array.length) {
               throw new IllegalArgumentException("Index can't be higher than the max index!");
            }

            T value = (T)(entry.getValue());
            this.array[index] = value;
            this.putAssetTag(codec, key, index, value);
         }
      } finally {
         this.arrayLock.unlock();
      }

   }

   protected Set<K> remove(@Nonnull Set<K> keys) {
      Set<K> remove = super.remove(keys);
      this.remove0(keys);
      return remove;
   }

   protected Set<K> remove(@Nonnull String packKey, @Nonnull Set<K> keys, @Nonnull List<Map.Entry<String, Object>> pathsToReload) {
      Set<K> remove = super.remove(packKey, keys, pathsToReload);
      this.remove0(keys);
      return remove;
   }

   private void remove0(@Nonnull Set<K> keys) {
      this.arrayLock.lock();

      try {
         for(K key : keys) {
            int blockId = this.indexGetter.applyAsInt(key);
            if (blockId != -2147483648) {
               this.array[blockId] = null;
               this.indexedTagStorage.forEachWithInt((_k, value, id) -> value.remove(id), blockId);
            }
         }

         this.resize();
      } finally {
         this.arrayLock.unlock();
      }

   }

   private void resize() {
      int length = this.maxIndexGetter.getAsInt();
      if (length < 0) {
         throw new IllegalArgumentException("max index can't be less than zero!");
      } else {
         if (length > this.array.length) {
            T[] newArray = (JsonAssetWithMap[])this.arrayProvider.apply(length);
            System.arraycopy(this.array, 0, newArray, 0, this.array.length);
            this.array = newArray;
         } else if (length < this.array.length) {
            for(int i = length; i < this.array.length; ++i) {
               if (this.array[i] != null) {
                  throw new IllegalArgumentException("Assets exist in the array outside of the max index!");
               }
            }

            T[] newArray = (JsonAssetWithMap[])this.arrayProvider.apply(length);
            System.arraycopy(this.array, 0, newArray, 0, newArray.length);
            this.array = newArray;
         }

      }
   }

   public boolean requireReplaceOnRemove() {
      return false;
   }
}
