package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class DiskDataStoreProvider implements DataStoreProvider {
   public static final String ID = "Disk";
   public static final BuilderCodec<DiskDataStoreProvider> CODEC;
   private String path;

   public DiskDataStoreProvider(String path) {
      this.path = path;
   }

   protected DiskDataStoreProvider() {
   }

   @Nonnull
   public <T> DataStore<T> create(BuilderCodec<T> builderCodec) {
      return new DiskDataStore<T>(this.path, builderCodec);
   }

   @Nonnull
   public String toString() {
      return "DiskDataStoreProvider{path='" + this.path + "'}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(DiskDataStoreProvider.class, DiskDataStoreProvider::new).append(new KeyedCodec("Path", Codec.STRING), (diskDataStoreProvider, s) -> diskDataStoreProvider.path = s, (diskDataStoreProvider) -> diskDataStoreProvider.path).addValidator(Validators.nonNull()).add()).build();
   }
}
