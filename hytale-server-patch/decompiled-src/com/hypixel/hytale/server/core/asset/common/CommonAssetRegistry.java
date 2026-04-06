package com.hypixel.hytale.server.core.asset.common;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.util.PatternUtil;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CommonAssetRegistry {
   private static final Map<String, List<PackAsset>> assetByNameMap = new ConcurrentHashMap();
   private static final Map<String, List<PackAsset>> assetByHashMap = new ConcurrentHashMap();
   private static final AtomicInteger duplicateAssetCount = new AtomicInteger();
   private static final Collection<List<PackAsset>> unmodifiableAssetByNameMapValues;

   public static int getDuplicateAssetCount() {
      return duplicateAssetCount.get();
   }

   @Nonnull
   public static Map<String, List<PackAsset>> getDuplicatedAssets() {
      Map<String, List<PackAsset>> duplicates = new Object2ObjectOpenHashMap();

      for(Map.Entry<String, List<PackAsset>> entry : assetByHashMap.entrySet()) {
         if (((List)entry.getValue()).size() > 1) {
            duplicates.put((String)entry.getKey(), new ObjectArrayList((Collection)entry.getValue()));
         }
      }

      return duplicates;
   }

   @Nonnull
   public static Collection<List<PackAsset>> getAllAssets() {
      return unmodifiableAssetByNameMapValues;
   }

   public static void clearAllAssets() {
      assetByNameMap.clear();
      assetByHashMap.clear();
   }

   @Nonnull
   public static AddCommonAssetResult addCommonAsset(String pack, @Nonnull CommonAsset asset) {
      AddCommonAssetResult result = new AddCommonAssetResult();
      result.newPackAsset = new PackAsset(pack, asset);
      List<PackAsset> list = (List)assetByNameMap.computeIfAbsent(asset.getName(), (v) -> new CopyOnWriteArrayList());
      boolean added = false;
      boolean addHash = true;

      for(int i = 0; i < list.size(); ++i) {
         PackAsset e = (PackAsset)list.get(i);
         if (e.pack().equals(pack)) {
            result.previousNameAsset = e;
            if (i == list.size() - 1) {
               ((List)assetByHashMap.get(e.asset.getHash())).remove(e);
               assetByHashMap.compute(e.asset.getHash(), (k, v) -> v != null && !v.isEmpty() ? v : null);
            } else {
               addHash = false;
            }

            list.set(i, result.newPackAsset);
            added = true;
            break;
         }
      }

      if (!added) {
         if (!list.isEmpty()) {
            PackAsset e = (PackAsset)list.getLast();
            ((List)assetByHashMap.get(e.asset.getHash())).remove(e);
            assetByHashMap.compute(e.asset.getHash(), (k, v) -> v != null && !v.isEmpty() ? v : null);
            result.previousNameAsset = e;
         }

         list.add(result.newPackAsset);
      }

      if (addHash) {
         List<PackAsset> commonAssets = (List)assetByHashMap.computeIfAbsent(asset.getHash(), (k) -> new CopyOnWriteArrayList());
         if (!commonAssets.isEmpty()) {
            result.previousHashAssets = (PackAsset[])commonAssets.toArray((x$0) -> new PackAsset[x$0]);
         }

         commonAssets.add(result.newPackAsset);
      }

      if (result.previousHashAssets != null || result.previousNameAsset != null) {
         result.duplicateAssetId = duplicateAssetCount.getAndIncrement();
      }

      result.activeAsset = (PackAsset)list.getLast();
      return result;
   }

   @Nullable
   public static BooleanObjectPair<PackAsset> removeCommonAssetByName(String pack, String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<PackAsset> oldAssets = (List)assetByNameMap.get(name);
      if (oldAssets == null) {
         return null;
      } else {
         PackAsset previousCurrent = (PackAsset)oldAssets.getLast();
         oldAssets.removeIf((v) -> v.pack().equals(pack));
         assetByNameMap.compute(name, (k, v) -> v != null && !v.isEmpty() ? v : null);
         if (oldAssets.isEmpty()) {
            removeCommonAssetByHash0(previousCurrent);
            return BooleanObjectPair.of(false, previousCurrent);
         } else {
            PackAsset newCurrent = (PackAsset)oldAssets.getLast();
            if (newCurrent.equals(previousCurrent)) {
               return null;
            } else {
               removeCommonAssetByHash0(previousCurrent);
               ((List)assetByHashMap.computeIfAbsent(newCurrent.asset.getHash(), (v) -> new CopyOnWriteArrayList())).add(newCurrent);
               return BooleanObjectPair.of(true, newCurrent);
            }
         }
      }
   }

   @Nonnull
   public static List<CommonAsset> getCommonAssetsStartingWith(String pack, String name) {
      List<CommonAsset> oldAssets = new ObjectArrayList();

      for(List<PackAsset> assets : assetByNameMap.values()) {
         for(PackAsset asset : assets) {
            if (asset.asset().getName().startsWith(name) && asset.pack().equals(pack)) {
               oldAssets.add(asset.asset());
            }
         }
      }

      return oldAssets;
   }

   public static boolean hasCommonAsset(String name) {
      return assetByNameMap.containsKey(name);
   }

   public static boolean hasCommonAsset(AssetPack pack, String name) {
      List<PackAsset> packAssets = (List)assetByNameMap.get(name);
      if (packAssets != null) {
         for(PackAsset packAsset : packAssets) {
            if (packAsset.pack.equals(pack.getName())) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public static CommonAsset getByName(String name) {
      name = PatternUtil.replaceBackslashWithForwardSlash(name);
      List<PackAsset> asset = (List)assetByNameMap.get(name);
      return asset == null ? null : ((PackAsset)asset.getLast()).asset();
   }

   @Nullable
   public static CommonAsset getByHash(@Nonnull String hash) {
      List<PackAsset> assets = (List)assetByHashMap.get(hash.toLowerCase());
      return assets != null && !assets.isEmpty() ? ((PackAsset)assets.getFirst()).asset() : null;
   }

   private static void removeCommonAssetByHash0(@Nonnull PackAsset oldAsset) {
      List<PackAsset> commonAssets = (List)assetByHashMap.get(oldAsset.asset().getHash());
      if (commonAssets != null && commonAssets.remove(oldAsset) && commonAssets.isEmpty()) {
         assetByHashMap.compute(oldAsset.asset().getHash(), (key, assets) -> assets != null && !assets.isEmpty() ? assets : null);
      }

   }

   static {
      unmodifiableAssetByNameMapValues = Collections.unmodifiableCollection(assetByNameMap.values());
   }

   public static class AddCommonAssetResult {
      private PackAsset newPackAsset;
      private PackAsset previousNameAsset;
      private PackAsset activeAsset;
      private PackAsset[] previousHashAssets;
      private int duplicateAssetId;

      public PackAsset getNewPackAsset() {
         return this.newPackAsset;
      }

      public PackAsset getPreviousNameAsset() {
         return this.previousNameAsset;
      }

      public PackAsset getActiveAsset() {
         return this.activeAsset;
      }

      public PackAsset[] getPreviousHashAssets() {
         return this.previousHashAssets;
      }

      public int getDuplicateAssetId() {
         return this.duplicateAssetId;
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.previousNameAsset);
         return "AddCommonAssetResult{previousNameAsset=" + var10000 + ", previousHashAssets=" + Arrays.toString(this.previousHashAssets) + ", duplicateAssetId=" + this.duplicateAssetId + "}";
      }
   }

   public static record PackAsset(String pack, CommonAsset asset) {
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            PackAsset packAsset = (PackAsset)o;
            return !this.pack.equals(packAsset.pack) ? false : this.asset.equals(packAsset.asset);
         } else {
            return false;
         }
      }

      @Nonnull
      public String toString() {
         String var10000 = this.pack;
         return "PackAsset{pack='" + var10000 + "', asset=" + String.valueOf(this.asset) + "}";
      }
   }
}
