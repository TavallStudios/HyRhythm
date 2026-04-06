package com.hypixel.hytale.common.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultMap<K, V> implements Map<K, V> {
   private final Map<K, V> delegate;
   private final boolean allowReplacing;
   private final boolean replaceNullWithDefault;
   private V defaultValue;

   public DefaultMap(V defaultValue) {
      this(defaultValue, new HashMap());
   }

   public DefaultMap(V defaultValue, Map<K, V> delegate) {
      this(defaultValue, delegate, true);
   }

   public DefaultMap(V defaultValue, Map<K, V> delegate, boolean allowReplacing) {
      this(defaultValue, delegate, allowReplacing, true);
   }

   public DefaultMap(V defaultValue, Map<K, V> delegate, boolean allowReplacing, boolean replaceNullWithDefault) {
      this.defaultValue = defaultValue;
      this.delegate = delegate;
      this.allowReplacing = allowReplacing;
      this.replaceNullWithDefault = replaceNullWithDefault;
   }

   public V getDefaultValue() {
      return this.defaultValue;
   }

   public void setDefaultValue(V defaultValue) {
      this.defaultValue = defaultValue;
   }

   public Map<K, V> getDelegate() {
      return this.delegate;
   }

   public int size() {
      return this.delegate.size();
   }

   public boolean isEmpty() {
      return this.delegate.isEmpty();
   }

   public boolean containsKey(Object key) {
      return this.delegate.containsKey(key);
   }

   public boolean containsValue(Object value) {
      return this.delegate.containsValue(value);
   }

   public V get(@Nullable Object key) {
      if (this.replaceNullWithDefault && key == null) {
         return this.defaultValue;
      } else {
         V value = (V)this.delegate.get(key);
         return (V)(value != null ? value : this.defaultValue);
      }
   }

   public V put(K key, V value) {
      if (this.allowReplacing) {
         return (V)this.delegate.put(key, value);
      } else {
         V oldValue = (V)this.delegate.putIfAbsent(key, value);
         if (oldValue == null) {
            return null;
         } else {
            throw new IllegalArgumentException("Attachment (" + String.valueOf(key) + ") is already registered!");
         }
      }
   }

   public V remove(Object key) {
      return (V)this.delegate.remove(key);
   }

   public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
      this.delegate.putAll(m);
   }

   public void clear() {
      this.delegate.clear();
   }

   @Nonnull
   public Set<K> keySet() {
      return this.delegate.keySet();
   }

   @Nonnull
   public Collection<V> values() {
      return this.delegate.values();
   }

   @Nonnull
   public Set<Map.Entry<K, V>> entrySet() {
      return this.delegate.entrySet();
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DefaultMap<?, ?> that = (DefaultMap)o;
         if (this.allowReplacing != that.allowReplacing) {
            return false;
         } else if (this.replaceNullWithDefault != that.replaceNullWithDefault) {
            return false;
         } else {
            if (this.delegate != null) {
               if (!this.delegate.equals(that.delegate)) {
                  return false;
               }
            } else if (that.delegate != null) {
               return false;
            }

            return this.defaultValue != null ? this.defaultValue.equals(that.defaultValue) : that.defaultValue == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.delegate != null ? this.delegate.hashCode() : 0;
      result = 31 * result + (this.allowReplacing ? 1 : 0);
      result = 31 * result + (this.replaceNullWithDefault ? 1 : 0);
      result = 31 * result + (this.defaultValue != null ? this.defaultValue.hashCode() : 0);
      return result;
   }

   public V getOrDefault(Object key, V defaultValue) {
      return (V)this.delegate.getOrDefault(key, defaultValue);
   }

   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.delegate.forEach(action);
   }

   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      this.delegate.replaceAll(function);
   }

   public V putIfAbsent(K key, V value) {
      return (V)this.delegate.putIfAbsent(key, value);
   }

   public boolean remove(Object key, Object value) {
      return this.delegate.remove(key, value);
   }

   public boolean replace(K key, V oldValue, V newValue) {
      return this.delegate.replace(key, oldValue, newValue);
   }

   public V replace(K key, V value) {
      return (V)this.delegate.replace(key, value);
   }

   public V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
      return (V)this.delegate.computeIfAbsent(key, mappingFunction);
   }

   @Nullable
   public V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return (V)this.delegate.computeIfPresent(key, remappingFunction);
   }

   public V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return (V)this.delegate.compute(key, remappingFunction);
   }

   public V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      return (V)this.delegate.merge(key, value, remappingFunction);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.defaultValue);
      return "DefaultMap{defaultValue=" + var10000 + ", delegate=" + String.valueOf(this.delegate) + "}";
   }
}
