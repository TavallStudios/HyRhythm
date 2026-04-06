package com.hypixel.hytale.assetstore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DecodedAsset<K, T extends JsonAsset<K>> implements AssetHolder<K> {
   private final K key;
   private final T asset;

   public DecodedAsset(K key, T asset) {
      this.key = key;
      this.asset = asset;
   }

   public K getKey() {
      return this.key;
   }

   public T getAsset() {
      return this.asset;
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DecodedAsset<?, ?> that = (DecodedAsset)o;
         if (this.key != null) {
            if (!this.key.equals(that.key)) {
               return false;
            }
         } else if (that.key != null) {
            return false;
         }

         return this.asset != null ? this.asset.equals(that.asset) : that.asset == null;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.key != null ? this.key.hashCode() : 0;
      result = 31 * result + (this.asset != null ? this.asset.hashCode() : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.key);
      return "DecodedAsset{key=" + var10000 + ", asset=" + String.valueOf(this.asset) + "}";
   }
}
