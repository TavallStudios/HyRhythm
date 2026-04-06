package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DefaultChunkStorageProvider implements IChunkStorageProvider<Object> {
   public static final int VERSION = 0;
   public static final String ID = "Hytale";
   @Nonnull
   private static final IChunkStorageProvider<?> DEFAULT_INDEXED = new IndexedStorageChunkStorageProvider();
   @Nonnull
   public static final BuilderCodec<DefaultChunkStorageProvider> CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DefaultChunkStorageProvider.class, DefaultChunkStorageProvider::new).versioned()).codecVersion(0)).documentation("Selects the default recommended storage as decided by the server.")).build();
   private IChunkStorageProvider<?> provider;

   public DefaultChunkStorageProvider() {
      this.provider = DEFAULT_INDEXED;
   }

   public Object initialize(@Nonnull Store<ChunkStore> store) throws IOException {
      return this.provider.initialize(store);
   }

   public void close(@Nonnull Object o, @NonNullDecl Store<ChunkStore> store) throws IOException {
      this.provider.close(o, store);
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull Object o, @Nonnull Store<ChunkStore> store) throws IOException {
      return this.provider.getLoader(o, store);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull Object o, @Nonnull Store<ChunkStore> store) throws IOException {
      return this.provider.getSaver(o, store);
   }

   public boolean isSame(IChunkStorageProvider<?> other) {
      return other.getClass().equals(this.getClass()) || this.provider.isSame(other);
   }

   @Nonnull
   public String toString() {
      return "DefaultChunkStorageProvider{DEFAULT=" + String.valueOf(this.provider) + "}";
   }
}
