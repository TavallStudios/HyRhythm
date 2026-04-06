package com.hypixel.hytale.codec.lookup;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.EmptyExtraInfo;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MapKeyMapCodec<V> extends AMapProvidedMapCodec<Class<? extends V>, V, Codec<V>, TypeMap<V>> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Set<Reference<TypeMap<?>>> ACTIVE_MAPS = ConcurrentHashMap.newKeySet();
   private static final ReferenceQueue<TypeMap<?>> MAP_REFERENCE_QUEUE = new ReferenceQueue();
   private static final StampedLock DATA_LOCK = new StampedLock();
   protected final Map<String, Class<? extends V>> idToClass;
   protected final Map<Class<? extends V>, String> classToId;

   public MapKeyMapCodec() {
      this(true);
   }

   public MapKeyMapCodec(boolean unmodifiable) {
      super(new ConcurrentHashMap(), Function.identity(), unmodifiable);
      this.idToClass = new ConcurrentHashMap();
      this.classToId = new ConcurrentHashMap();
   }

   public <T extends V> void register(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
      long lock = DATA_LOCK.writeLock();

      try {
         if (this.codecProvider.put(tClass, codec) != null) {
            throw new IllegalArgumentException("Id already registered");
         }

         if (this.idToClass.put(id, tClass) != null) {
            throw new IllegalArgumentException("Id already registered");
         }

         if (this.classToId.put(tClass, id) != null) {
            throw new IllegalArgumentException("Class already registered");
         }

         for(Reference<TypeMap<?>> mapRef : ACTIVE_MAPS) {
            TypeMap<?> map = (TypeMap)mapRef.get();
            if (map != null && map.codec == this) {
               map.tryUpgrade(tClass, id, codec);
            }
         }
      } finally {
         DATA_LOCK.unlockWrite(lock);
      }

   }

   public <T extends V> void unregister(@Nonnull Class<T> tClass) {
      long lock = DATA_LOCK.writeLock();

      try {
         Codec<V> codec = (Codec)this.codecProvider.get(tClass);
         if (codec == null) {
            throw new IllegalStateException(String.valueOf(tClass) + " not registered");
         }

         String id = (String)this.classToId.get(tClass);

         for(Reference<TypeMap<?>> mapRef : ACTIVE_MAPS) {
            TypeMap<?> map = (TypeMap)mapRef.get();
            if (map != null && map.codec == this) {
               map.tryDowngrade(tClass, id, codec);
            }
         }

         this.codecProvider.remove(tClass);
         this.classToId.remove(tClass);
         this.idToClass.remove(id);
      } finally {
         DATA_LOCK.unlockWrite(lock);
      }

   }

   /** @deprecated */
   @Nullable
   @Deprecated(
      forRemoval = true
   )
   public V decodeById(@Nonnull String id, BsonValue value, ExtraInfo extraInfo) {
      Codec<V> codec = (Codec)this.codecProvider.get(this.getKeyForId(id));
      return codec.decode(value, extraInfo);
   }

   protected String getIdForKey(Class<? extends V> key) {
      return (String)this.classToId.get(key);
   }

   @Nonnull
   public TypeMap<V> createMap() {
      return new TypeMap<V>(this);
   }

   public void handleUnknown(@Nonnull TypeMap<V> map, @Nonnull String key, BsonValue value, @Nonnull ExtraInfo extraInfo) {
      extraInfo.addUnknownKey(key);
      map.unknownValues.put(key, value);
   }

   public void handleUnknown(@Nonnull TypeMap<V> map, @Nonnull String key, @Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
      extraInfo.addUnknownKey(key);
      map.unknownValues.put(key, RawJsonReader.readBsonValue(reader));
   }

   protected void encodeExtra(@Nonnull BsonDocument document, @Nonnull TypeMap<V> map, ExtraInfo extraInfo) {
      document.putAll(map.unknownValues);
   }

   public Class<? extends V> getKeyForId(String id) {
      return (Class)this.idToClass.get(id);
   }

   @Nonnull
   protected TypeMap<V> emptyMap() {
      return MapKeyMapCodec.TypeMap.EMPTY;
   }

   @Nonnull
   protected TypeMap<V> unmodifiableMap(@Nonnull TypeMap<V> m) {
      return new TypeMap<V>(this, Collections.unmodifiableMap(m.map), m.map, m.unknownValues);
   }

   static {
      Thread thread = new Thread(() -> {
         while(!Thread.interrupted()) {
            try {
               ACTIVE_MAPS.remove(MAP_REFERENCE_QUEUE.remove());
            } catch (InterruptedException var1) {
               Thread.currentThread().interrupt();
               return;
            }
         }

      }, "MapKeyMapCodec");
      thread.setDaemon(true);
      thread.start();
   }

   public static class TypeMap<V> implements Map<Class<? extends V>, V> {
      private static final TypeMap EMPTY = new TypeMap((MapKeyMapCodec)null, Collections.emptyMap(), Collections.emptyMap());
      private final MapKeyMapCodec<V> codec;
      @Nonnull
      private final Map<Class<? extends V>, V> map;
      @Nonnull
      private final Map<Class<? extends V>, V> internalMap;
      @Nonnull
      private final Map<String, BsonValue> unknownValues;

      public TypeMap(MapKeyMapCodec<V> codec) {
         this(codec, new Object2ObjectOpenHashMap(), new Object2ObjectOpenHashMap());
      }

      public TypeMap(MapKeyMapCodec<V> codec, @Nonnull Map<Class<? extends V>, V> map, @Nonnull Map<String, BsonValue> unknownValues) {
         this(codec, map, map, unknownValues);
      }

      public TypeMap(MapKeyMapCodec<V> codec, @Nonnull Map<Class<? extends V>, V> map, @Nonnull Map<Class<? extends V>, V> internalMap, @Nonnull Map<String, BsonValue> unknownValues) {
         this.codec = codec;
         this.map = map;
         this.internalMap = internalMap;
         this.unknownValues = unknownValues;
         MapKeyMapCodec.ACTIVE_MAPS.add(new WeakReference(this, MapKeyMapCodec.MAP_REFERENCE_QUEUE));
      }

      public <T extends V> void tryUpgrade(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
         BsonValue unknownValue = (BsonValue)this.unknownValues.remove(id);
         if (unknownValue != null) {
            T value = codec.decode(unknownValue, EmptyExtraInfo.EMPTY);
            this.internalMap.put(tClass, value);
            ((HytaleLogger.Api)MapKeyMapCodec.LOGGER.atInfo()).log("Upgrade " + id + " from unknown value");
         }
      }

      public <T extends V> void tryDowngrade(@Nonnull Class<T> tClass, @Nonnull String id, @Nonnull Codec<T> codec) {
         V value = (V)this.internalMap.remove(tClass);
         if (value != null) {
            BsonValue encoded = codec.encode(value, EmptyExtraInfo.EMPTY);
            this.unknownValues.put(id, encoded);
            ((HytaleLogger.Api)MapKeyMapCodec.LOGGER.atInfo()).log("Downgraded " + id + " to unknown value");
         }
      }

      public int size() {
         return this.map.size();
      }

      public boolean isEmpty() {
         return this.map.isEmpty();
      }

      public boolean containsKey(Object key) {
         return this.map.containsKey(key);
      }

      public boolean containsValue(Object value) {
         return this.map.containsValue(value);
      }

      public V get(Object key) {
         return (V)this.map.get(key);
      }

      @Nullable
      public <T extends V> T get(Class<? extends T> key) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var4;
         try {
            var4 = this.map.get(key);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (T)var4;
      }

      public V put(@Nonnull Class<? extends V> key, V value) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var5;
         try {
            if (!key.isInstance(value)) {
               String var10002 = String.valueOf(value);
               throw new IllegalArgumentException("Passed value '" + var10002 + "' isn't of type: " + String.valueOf(key));
            }

            var5 = this.map.put(key, value);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (V)var5;
      }

      public V remove(Object key) {
         return (V)this.map.remove(key);
      }

      public void putAll(@Nonnull Map<? extends Class<? extends V>, ? extends V> m) {
         for(Map.Entry<? extends Class<? extends V>, ? extends V> e : m.entrySet()) {
            this.put((Class)e.getKey(), e.getValue());
         }

      }

      public void clear() {
         this.map.clear();
      }

      @Nonnull
      public Set<Class<? extends V>> keySet() {
         return this.map.keySet();
      }

      @Nonnull
      public Collection<V> values() {
         return this.map.values();
      }

      @Nonnull
      public Set<Map.Entry<Class<? extends V>, V>> entrySet() {
         return this.map.entrySet();
      }

      public <T extends V> T computeIfAbsent(Class<? extends T> key, @Nonnull Function<? super Class<? extends V>, T> mappingFunction) {
         long lock = MapKeyMapCodec.DATA_LOCK.readLock();

         Object var5;
         try {
            var5 = this.map.computeIfAbsent(key, mappingFunction);
         } finally {
            MapKeyMapCodec.DATA_LOCK.unlockRead(lock);
         }

         return (T)var5;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof Map) ? false : this.entrySet().equals(((Map)o).entrySet());
         }
      }

      public int hashCode() {
         return this.map.hashCode();
      }

      @Nonnull
      public String toString() {
         return "TypeMap{map=" + String.valueOf(this.map) + "}";
      }

      public static <V> TypeMap<V> empty() {
         return EMPTY;
      }
   }
}
