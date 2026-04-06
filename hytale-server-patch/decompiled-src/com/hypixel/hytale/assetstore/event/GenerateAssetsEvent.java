package com.hypixel.hytale.assetstore.event;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.event.IProcessedEvent;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class GenerateAssetsEvent<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> extends AssetsEvent<K, T> implements IProcessedEvent {
   private final Class<T> tClass;
   private final M assetMap;
   @Nonnull
   private final Map<K, T> loadedAssets;
   private final Map<K, Set<K>> assetChildren;
   @Nonnull
   private final Map<K, T> unmodifiableLoadedAssets;
   private final Map<K, T> addedAssets = new ConcurrentHashMap();
   private final Map<K, Set<K>> addedAssetChildren = new ConcurrentHashMap();
   private final Map<Class<? extends JsonAssetWithMap<?, ?>>, Map<?, Set<K>>> addedChildAssetsMap = new ConcurrentHashMap();
   private long before;

   public GenerateAssetsEvent(Class<T> tClass, M assetMap, @Nonnull Map<K, T> loadedAssets, Map<K, Set<K>> assetChildren) {
      this.tClass = tClass;
      this.assetMap = assetMap;
      this.loadedAssets = loadedAssets;
      this.assetChildren = assetChildren;
      this.unmodifiableLoadedAssets = Collections.unmodifiableMap(loadedAssets);
      this.before = System.nanoTime();
   }

   public Class<T> getAssetClass() {
      return this.tClass;
   }

   @Nonnull
   public Map<K, T> getLoadedAssets() {
      return this.unmodifiableLoadedAssets;
   }

   public M getAssetMap() {
      return this.assetMap;
   }

   public void addChildAsset(K childKey, T asset, @Nonnull K parent) {
      if (!this.loadedAssets.containsKey(parent) && this.assetMap.getAsset(parent) == null) {
         throw new IllegalArgumentException("Parent '" + String.valueOf(parent) + "' doesn't exist!");
      } else if (parent.equals(childKey)) {
         throw new IllegalArgumentException("Unable to to add asset '" + String.valueOf(parent) + "' because it is its own parent!");
      } else {
         AssetStore<K, T, M> assetStore = AssetRegistry.<K, T, M>getAssetStore(this.tClass);
         AssetExtraInfo<K> extraInfo = new AssetExtraInfo<K>(assetStore.getCodec().getData(asset));
         assetStore.getCodec().validate(asset, extraInfo);
         extraInfo.getValidationResults().logOrThrowValidatorExceptions(assetStore.getLogger());
         this.addedAssets.put(childKey, asset);
         ((Set)this.addedAssetChildren.computeIfAbsent(parent, (k) -> new HashSet())).add(childKey);
      }
   }

   @SafeVarargs
   public final void addChildAsset(K childKey, T asset, @Nonnull K... parents) {
      for(int i = 0; i < parents.length; ++i) {
         K parent = (K)parents[i];
         if (!this.loadedAssets.containsKey(parent) && this.assetMap.getAsset(parent) == null) {
            throw new IllegalArgumentException("Parent at " + i + " '" + String.valueOf(parent) + "' doesn't exist!");
         }

         if (parent.equals(childKey)) {
            throw new IllegalArgumentException("Unable to to add asset '" + String.valueOf(parent) + "' because it is its own parent!");
         }
      }

      AssetStore<K, T, M> assetStore = AssetRegistry.<K, T, M>getAssetStore(this.tClass);
      AssetExtraInfo<K> extraInfo = new AssetExtraInfo<K>(assetStore.getCodec().getData(asset));
      assetStore.getCodec().validate(asset, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(assetStore.getLogger());
      this.addedAssets.put(childKey, asset);

      for(K parent : parents) {
         ((Set)this.addedAssetChildren.computeIfAbsent(parent, (k) -> new HashSet())).add(childKey);
      }

   }

   public <P extends JsonAssetWithMap<PK, ?>, PK> void addChildAssetWithReference(K childKey, T asset, Class<P> parentAssetClass, @Nonnull PK parentKey) {
      if (AssetRegistry.getAssetStore(parentAssetClass).getAssetMap().getAsset(parentKey) == null) {
         String var10002 = String.valueOf(parentKey);
         throw new IllegalArgumentException("Parent '" + var10002 + "' from " + String.valueOf(parentAssetClass) + " doesn't exist!");
      } else if (parentKey.equals(childKey)) {
         throw new IllegalArgumentException("Unable to to add asset '" + String.valueOf(parentKey) + "' because it is its own parent!");
      } else {
         AssetStore<K, T, M> assetStore = AssetRegistry.<K, T, M>getAssetStore(this.tClass);
         AssetExtraInfo<K> extraInfo = new AssetExtraInfo<K>(assetStore.getCodec().getData(asset));
         assetStore.getCodec().validate(asset, extraInfo);
         extraInfo.getValidationResults().logOrThrowValidatorExceptions(assetStore.getLogger());
         this.addedAssets.put(childKey, asset);
         ((Set)((Map)this.addedChildAssetsMap.computeIfAbsent(parentAssetClass, (k) -> new ConcurrentHashMap())).computeIfAbsent(parentKey, (k) -> new HashSet())).add(childKey);
      }
   }

   public void addChildAssetWithReferences(K childKey, T asset, @Nonnull ParentReference<?, ?>... parents) {
      for(int i = 0; i < parents.length; ++i) {
         ParentReference<?, ?> parent = parents[i];
         if (AssetRegistry.getAssetStore(parent.getParentAssetClass()).getAssetMap().getAsset(parent.getParentKey()) == null) {
            throw new IllegalArgumentException("Parent at " + i + " '" + String.valueOf(parent) + "' doesn't exist!");
         }

         if (parent.parentKey.equals(childKey)) {
            throw new IllegalArgumentException("Unable to to add asset '" + String.valueOf(parent.parentKey) + "' because it is its own parent!");
         }
      }

      AssetStore<K, T, M> assetStore = AssetRegistry.<K, T, M>getAssetStore(this.tClass);
      AssetExtraInfo<K> extraInfo = new AssetExtraInfo<K>(assetStore.getCodec().getData(asset));
      assetStore.getCodec().validate(asset, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(assetStore.getLogger());
      this.addedAssets.put(childKey, asset);

      for(ParentReference<?, ?> parent : parents) {
         ((Set)((Map)this.addedChildAssetsMap.computeIfAbsent(parent.parentAssetClass, (k) -> new ConcurrentHashMap())).computeIfAbsent(parent.parentKey, (k) -> new HashSet())).add(childKey);
      }

   }

   public void processEvent(@Nonnull String hookName) {
      HytaleLogger.getLogger().at(Level.INFO).log("Generated %d of %s from %s in %s", this.addedAssets.size(), this.tClass.getSimpleName(), hookName, FormatUtil.nanosToString(System.nanoTime() - this.before));
      this.loadedAssets.putAll(this.addedAssets);
      this.addedAssets.clear();

      for(Map.Entry<K, Set<K>> entry : this.addedAssetChildren.entrySet()) {
         K parent = (K)entry.getKey();
         ((Set)this.assetChildren.computeIfAbsent(parent, (kx) -> ConcurrentHashMap.newKeySet())).addAll((Collection)entry.getValue());
      }

      this.addedAssetChildren.clear();

      for(Map.Entry<Class<? extends JsonAssetWithMap<?, ?>>, Map<?, Set<K>>> entry : this.addedChildAssetsMap.entrySet()) {
         Class k = (Class)entry.getKey();
         AssetStore assetStore = AssetRegistry.getAssetStore(k);

         for(Map.Entry<?, Set<K>> childEntry : ((Map)entry.getValue()).entrySet()) {
            assetStore.addChildAssetReferences(childEntry.getKey(), this.tClass, (Set)childEntry.getValue());
         }
      }

      this.addedChildAssetsMap.clear();
      this.before = System.nanoTime();
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.tClass);
      return "GenerateAssetsEvent{tClass=" + var10000 + ", loadedAssets.size()=" + this.loadedAssets.size() + ", " + super.toString() + "}";
   }

   public static class ParentReference<P extends JsonAssetWithMap<PK, ?>, PK> {
      private final Class<P> parentAssetClass;
      private final PK parentKey;

      public ParentReference(Class<P> parentAssetClass, PK parentKey) {
         this.parentAssetClass = parentAssetClass;
         this.parentKey = parentKey;
      }

      public Class<P> getParentAssetClass() {
         return this.parentAssetClass;
      }

      public PK getParentKey() {
         return this.parentKey;
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.parentAssetClass);
         return "ParentReference{parentAssetClass=" + var10000 + ", parentKey=" + String.valueOf(this.parentKey) + "}";
      }
   }
}
