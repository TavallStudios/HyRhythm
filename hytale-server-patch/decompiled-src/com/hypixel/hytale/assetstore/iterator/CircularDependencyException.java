package com.hypixel.hytale.assetstore.iterator;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.JsonAsset;
import java.util.Collection;
import javax.annotation.Nonnull;

public class CircularDependencyException extends RuntimeException {
   public CircularDependencyException(@Nonnull Collection<AssetStore<?, ?, ?>> values, @Nonnull AssetStoreIterator iterator) {
      super(makeMessage(values, iterator));
   }

   @Nonnull
   protected static String makeMessage(@Nonnull Collection<AssetStore<?, ?, ?>> values, @Nonnull AssetStoreIterator iterator) {
      String var10002 = String.valueOf(values);
      StringBuilder sb = new StringBuilder("Failed to process any stores there must be a circular dependency! " + var10002 + ", " + iterator.size() + "\nWaiting for Asset Stores:\n");

      for(AssetStore<?, ?, ?> store : values) {
         if (iterator.isWaitingForDependencies(store)) {
            sb.append(store.getAssetClass()).append("\n");

            for(Class<? extends JsonAsset<?>> aClass : store.getLoadsAfter()) {
               AssetStore otherStore = AssetRegistry.getAssetStore(aClass);
               if (otherStore == null) {
                  throw new IllegalArgumentException("Unable to find asset store: " + String.valueOf(aClass));
               }

               if (iterator.isWaitingForDependencies(otherStore)) {
                  sb.append("\t- ").append(otherStore.getAssetClass()).append("\n");
               }
            }
         }
      }

      return sb.toString();
   }
}
