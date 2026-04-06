package com.hypixel.hytale.server.worldgen.util.cache;

import com.hypixel.hytale.server.core.HytaleServer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SizedTimeoutCache<K, V> implements Cache<K, V> {
   private final ArrayDeque<CacheEntry<K, V>> pool = new ArrayDeque();
   private final Object2ObjectLinkedOpenHashMap<K, CacheEntry<K, V>> map = new Object2ObjectLinkedOpenHashMap();
   private final long timeout;
   private final int maxSize;
   @Nullable
   private final Function<K, V> func;
   @Nullable
   private final BiConsumer<K, V> destroyer;
   @Nonnull
   private final ScheduledFuture<?> future;
   @Nonnull
   private final Cleaner.Cleanable cleanable;

   public SizedTimeoutCache(long expire, @Nonnull TimeUnit unit, int maxSize, @Nullable Function<K, V> func, @Nullable BiConsumer<K, V> destroyer) {
      this.timeout = unit.toNanos(expire);
      this.maxSize = maxSize;
      this.func = func;
      this.destroyer = destroyer;
      this.future = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new CleanupRunnable(new WeakReference(this)), expire, expire, unit);
      this.cleanable = CleanupFutureAction.CLEANER.register(this, new CleanupFutureAction(this.future));
   }

   public void cleanup() {
      this.reduceLength(this.maxSize);
      long expire = System.nanoTime() - this.timeout;

      while(true) {
         K key;
         V value;
         synchronized(this.map) {
            label49: {
               if (!this.map.isEmpty()) {
                  key = (K)this.map.lastKey();
                  CacheEntry<K, V> entry = (CacheEntry)this.map.get(key);
                  if (entry.timestamp <= expire) {
                     this.map.remove(key);
                     value = entry.value;
                     if (this.pool.size() < this.maxSize) {
                        entry.key = null;
                        entry.value = null;
                        entry.timestamp = 0L;
                        this.pool.addLast(entry);
                     }
                     break label49;
                  }
               }

               return;
            }
         }

         if (this.destroyer != null) {
            this.destroyer.accept(key, value);
         }
      }
   }

   private void reduceLength(int targetSize) {
      while(true) {
         K key;
         V value;
         synchronized(this.map) {
            if (this.map.size() <= targetSize) {
               return;
            }

            CacheEntry<K, V> entry = (CacheEntry)this.map.removeLast();
            key = entry.key;
            value = entry.value;
            if (this.pool.size() < this.maxSize) {
               entry.key = null;
               entry.value = null;
               entry.timestamp = 0L;
               this.pool.addLast(entry);
            }
         }

         if (this.destroyer != null) {
            this.destroyer.accept(key, value);
         }
      }
   }

   public void shutdown() {
      this.cleanable.clean();
      if (this.destroyer != null) {
         this.reduceLength(0);
      } else {
         synchronized(this.map) {
            this.map.clear();
         }
      }

   }

   @Nullable
   public V get(K key) {
      if (this.future.isCancelled()) {
         throw new IllegalStateException("Cache has been shutdown!");
      } else {
         long timestamp = System.nanoTime();
         synchronized(this.map) {
            CacheEntry<K, V> entry = (CacheEntry)this.map.getAndMoveToFirst(key);
            if (entry != null) {
               entry.timestamp = timestamp;
               return entry.value;
            }
         }

         if (this.func == null) {
            return null;
         } else {
            V value = (V)this.func.apply(key);
            timestamp = System.nanoTime();
            CacheEntry<K, V> resultEntry;
            V resultValue;
            CacheEntry<K, V> newEntry;
            synchronized(this.map) {
               newEntry = this.pool.isEmpty() ? new CacheEntry() : (CacheEntry)this.pool.removeLast();
               newEntry.key = key;
               newEntry.value = value;
               newEntry.timestamp = timestamp;
               resultEntry = (CacheEntry)this.map.getAndMoveToFirst(key);
               if (resultEntry != null) {
                  resultEntry.timestamp = timestamp;
               } else {
                  resultEntry = newEntry;
                  this.map.put(key, newEntry);
               }

               resultValue = resultEntry.value;
            }

            if (resultEntry != newEntry && this.destroyer != null) {
               this.destroyer.accept(key, value);
            }

            return resultValue;
         }
      }
   }

   public void put(K key, V value) {
      if (this.future.isCancelled()) {
         throw new IllegalStateException("Cache has been shutdown!");
      } else {
         long timestamp = System.nanoTime();
         CacheEntry<K, V> oldEntry;
         synchronized(this.map) {
            CacheEntry<K, V> entry = this.pool.isEmpty() ? new CacheEntry() : (CacheEntry)this.pool.removeLast();
            entry.key = key;
            entry.value = value;
            entry.timestamp = timestamp;
            oldEntry = (CacheEntry)this.map.putAndMoveToFirst(key, entry);
            if (oldEntry != null) {
               entry.key = oldEntry.key;
            }
         }

         if (oldEntry != null && this.destroyer != null) {
            this.destroyer.accept(key, oldEntry.value);
         }

      }
   }

   @Nullable
   public V getWithReusedKey(K reusedKey, @Nonnull Function<K, K> keyPool) {
      if (this.future.isCancelled()) {
         throw new IllegalStateException("Cache has been shutdown!");
      } else {
         long timestamp = System.nanoTime();
         synchronized(this.map) {
            CacheEntry<K, V> entry = (CacheEntry)this.map.getAndMoveToFirst(reusedKey);
            if (entry != null) {
               entry.timestamp = timestamp;
               return entry.value;
            }
         }

         if (this.func == null) {
            return null;
         } else {
            K newKey = (K)keyPool.apply(reusedKey);
            V value = (V)this.func.apply(newKey);
            timestamp = System.nanoTime();
            CacheEntry<K, V> newEntry;
            CacheEntry<K, V> resultEntry;
            V resultValue;
            synchronized(this.map) {
               newEntry = this.pool.isEmpty() ? new CacheEntry() : (CacheEntry)this.pool.removeLast();
               newEntry.key = newKey;
               newEntry.value = value;
               newEntry.timestamp = timestamp;
               resultEntry = (CacheEntry)this.map.getAndMoveToFirst(newKey);
               if (resultEntry != null) {
                  resultEntry.timestamp = timestamp;
               } else {
                  resultEntry = newEntry;
                  this.map.put(newKey, newEntry);
               }

               resultValue = resultEntry.value;
            }

            if (resultEntry != newEntry && this.destroyer != null) {
               this.destroyer.accept(newKey, value);
            }

            return resultValue;
         }
      }
   }

   private static class CacheEntry<K, V> {
      @Nullable
      private V value;
      @Nullable
      private K key;
      private long timestamp;
   }
}
