package com.hypixel.hytale.builtin.hytalegenerator.datastructures.bicoordinatecache;

import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class HashedBiCoordinateCache<T> implements BiCoordinateCache<T> {
   @Nonnull
   private final ConcurrentHashMap<Long, T> values = new ConcurrentHashMap();

   public static long hash(int x, int z) {
      long hash = (long)x;
      hash <<= 32;
      hash += (long)z;
      return hash;
   }

   public T get(int x, int z) {
      long key = hash(x, z);
      if (!this.values.containsKey(key)) {
         throw new IllegalStateException("doesn't contain coordinates");
      } else {
         return (T)this.values.get(key);
      }
   }

   public boolean isCached(int x, int z) {
      return this.values.containsKey(hash(x, z));
   }

   @Nonnull
   public T save(int x, int z, @Nonnull T value) {
      long key = hash(x, z);
      this.values.put(key, value);
      return value;
   }

   public void flush(int x, int z) {
      long key = hash(x, z);
      if (this.values.containsKey(key)) {
         this.values.remove(key);
      }
   }

   public void flush() {
      for(long key : this.values.keySet()) {
         this.values.remove(key);
      }

   }

   public int size() {
      return this.values.size();
   }

   @Nonnull
   public String toString() {
      return "HashedBiCoordinateCache{values=" + String.valueOf(this.values) + "}";
   }
}
