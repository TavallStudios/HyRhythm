package com.hypixel.hytale.server.worldgen.util.cache;

import com.hypixel.hytale.server.core.HytaleServer;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TimeoutCache<K, V> implements Cache<K, V> {
   private final Map<K, CacheEntry<V>> map = new ConcurrentHashMap();
   private final long timeout;
   @Nonnull
   private final Function<K, V> func;
   @Nullable
   private final BiConsumer<K, V> destroyer;
   @Nonnull
   private final ScheduledFuture<?> future;
   @Nonnull
   private final Cleaner.Cleanable cleanable;

   public TimeoutCache(long expire, @Nonnull TimeUnit unit, @Nonnull Function<K, V> func, @Nullable BiConsumer<K, V> destroyer) {
      this.timeout = unit.toNanos(expire);
      this.func = func;
      this.destroyer = destroyer;
      this.future = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new CleanupRunnable(new WeakReference(this)), expire, expire, unit);
      this.cleanable = CleanupFutureAction.CLEANER.register(this, new CleanupFutureAction(this.future));
   }

   public void cleanup() {
      long expire = System.nanoTime() - this.timeout;

      for(Map.Entry<K, CacheEntry<V>> entry : this.map.entrySet()) {
         CacheEntry<V> cacheEntry = (CacheEntry)entry.getValue();
         if (cacheEntry.timestamp < expire) {
            K key = (K)entry.getKey();
            if (this.map.remove(key, entry.getValue()) && this.destroyer != null) {
               this.destroyer.accept(key, cacheEntry.value);
            }
         }
      }

   }

   public void shutdown() {
      this.cleanable.clean();
      Iterator<Map.Entry<K, CacheEntry<V>>> iterator = this.map.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<K, CacheEntry<V>> entry = (Map.Entry)iterator.next();
         K key = (K)entry.getKey();
         CacheEntry<V> cacheEntry = (CacheEntry)entry.getValue();
         if (this.map.remove(key, cacheEntry)) {
            iterator.remove();
            if (this.destroyer != null) {
               this.destroyer.accept(key, cacheEntry.value);
            }
         }
      }

   }

   public V get(K key) {
      if (this.future.isCancelled()) {
         throw new IllegalStateException("Cache has been shutdown!");
      } else {
         CacheEntry<V> cacheEntry = (CacheEntry)this.map.compute(key, (k, v) -> {
            if (v != null) {
               v.timestamp = System.nanoTime();
               return v;
            } else {
               return new CacheEntry(this.func.apply(k));
            }
         });
         return cacheEntry.value;
      }
   }

   private static class CacheEntry<V> {
      private final V value;
      private long timestamp;

      public CacheEntry(V value) {
         this.value = value;
         this.timestamp = System.nanoTime();
      }
   }
}
