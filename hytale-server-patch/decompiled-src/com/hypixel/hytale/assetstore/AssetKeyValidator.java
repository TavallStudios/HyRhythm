package com.hypixel.hytale.assetstore;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class AssetKeyValidator<K> implements Validator<K> {
   private final Supplier<AssetStore<K, ?, ?>> store;

   public AssetKeyValidator(Supplier<AssetStore<K, ?, ?>> store) {
      this.store = store;
   }

   public AssetStore<K, ?, ?> getStore() {
      return (AssetStore)this.store.get();
   }

   public void accept(K k, @Nonnull ValidationResults results) {
      ((AssetStore)this.store.get()).validate(k, results, results.getExtraInfo());
   }

   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      target.setHytaleAssetRef(((AssetStore)this.store.get()).getAssetClass().getSimpleName());
   }
}
