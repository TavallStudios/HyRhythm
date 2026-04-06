package com.hypixel.hytale.assetstore.map;

import com.hypixel.hytale.assetstore.codec.AssetCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IndexedLookupTableAssetMap<K, T extends JsonAssetWithMap<K, IndexedLookupTableAssetMap<K, T>>> extends AssetMapWithIndexes<K, T> {
   private final AtomicInteger nextIndex = new AtomicInteger();
   private final StampedLock keyToIndexLock = new StampedLock();
   private final Object2IntMap<K> keyToIndex = new Object2IntOpenCustomHashMap(CaseInsensitiveHashStrategy.getInstance());
   @Nonnull
   private final IntFunction<T[]> arrayProvider;
   private final ReentrantLock arrayLock = new ReentrantLock();
   private T[] array;

   public IndexedLookupTableAssetMap(@Nonnull IntFunction<T[]> arrayProvider) {
      this.arrayProvider = arrayProvider;
      this.array = (JsonAssetWithMap[])arrayProvider.apply(0);
      this.keyToIndex.defaultReturnValue(-2147483648);
   }

   public int getIndex(K key) {
      long stamp = this.keyToIndexLock.tryOptimisticRead();
      int value = this.keyToIndex.getInt(key);
      if (this.keyToIndexLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.keyToIndexLock.readLock();

         int var5;
         try {
            var5 = this.keyToIndex.getInt(key);
         } finally {
            this.keyToIndexLock.unlockRead(stamp);
         }

         return var5;
      }
   }

   public int getIndexOrDefault(K key, int def) {
      long stamp = this.keyToIndexLock.tryOptimisticRead();
      int value = this.keyToIndex.getOrDefault(key, def);
      if (this.keyToIndexLock.validate(stamp)) {
         return value;
      } else {
         stamp = this.keyToIndexLock.readLock();

         int var6;
         try {
            var6 = this.keyToIndex.getOrDefault(key, def);
         } finally {
            this.keyToIndexLock.unlockRead(stamp);
         }

         return var6;
      }
   }

   public int getNextIndex() {
      return this.nextIndex.get();
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
      long stamp = this.keyToIndexLock.writeLock();
      this.arrayLock.lock();

      try {
         this.keyToIndex.clear();
         this.array = (JsonAssetWithMap[])this.arrayProvider.apply(0);
      } finally {
         this.arrayLock.unlock();
         this.keyToIndexLock.unlockWrite(stamp);
      }

   }

   protected void putAll(@Nonnull String packKey, @Nonnull AssetCodec<K, T> codec, @Nonnull Map<K, T> loadedAssets, @Nonnull Map<K, Path> loadedKeyToPathMap, @Nonnull Map<K, Set<K>> loadedAssetChildren) {
      super.putAll(packKey, codec, loadedAssets, loadedKeyToPathMap, loadedAssetChildren);
      this.putAll0(codec, loadedAssets);
   }

   private void putAll0(@Nonnull AssetCodec<K, T> codec, @Nonnull Map<K, T> loadedAssets) {
      long stamp = this.keyToIndexLock.writeLock();
      this.arrayLock.lock();

      try {
         int highestIndex = 0;

         for(K key : loadedAssets.keySet()) {
            int index = this.keyToIndex.getInt(key);
            if (index == -2147483648) {
               this.keyToIndex.put(key, index = this.nextIndex.getAndIncrement());
            }

            if (index < 0) {
               throw new IllegalArgumentException("Index can't be less than zero!");
            }

            if (index > highestIndex) {
               highestIndex = index;
            }
         }

         int length = highestIndex + 1;
         if (length < 0) {
            throw new IllegalArgumentException("Highest index can't be less than zero!");
         }

         if (length > this.array.length) {
            T[] newArray = (JsonAssetWithMap[])this.arrayProvider.apply(length);
            System.arraycopy(this.array, 0, newArray, 0, this.array.length);
            this.array = newArray;
         }

         for(Map.Entry<K, T> entry : loadedAssets.entrySet()) {
            K key = (K)entry.getKey();
            int index = this.keyToIndex.getInt(key);
            if (index < 0) {
               throw new IllegalArgumentException("Index can't be less than zero!");
            }

            T value = (T)(entry.getValue());
            this.array[index] = value;
            this.putAssetTag(codec, key, index, value);
         }
      } finally {
         this.arrayLock.unlock();
         this.keyToIndexLock.unlockWrite(stamp);
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
      long stamp = this.keyToIndexLock.writeLock();
      this.arrayLock.lock();

      try {
         for(K key : keys) {
            int blockId = this.keyToIndex.getInt(key);
            if (blockId != -2147483648) {
               this.array[blockId] = null;
               this.indexedTagStorage.forEachWithInt((_k, value, id) -> value.remove(id), blockId);
            }
         }

         int i;
         for(i = this.array.length - 1; i > 0 && this.array[i] == null; --i) {
         }

         int length = i + 1;
         if (length != this.array.length) {
            T[] newArray = (JsonAssetWithMap[])this.arrayProvider.apply(length);
            System.arraycopy(this.array, 0, newArray, 0, newArray.length);
            this.array = newArray;
         }
      } finally {
         this.arrayLock.unlock();
         this.keyToIndexLock.unlockWrite(stamp);
      }

   }
}
