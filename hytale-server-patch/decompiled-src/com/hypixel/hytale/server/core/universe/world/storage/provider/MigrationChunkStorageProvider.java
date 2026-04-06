package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MigrationChunkStorageProvider implements IChunkStorageProvider<MigrationData> {
   public static final String ID = "Migration";
   @Nonnull
   public static final BuilderCodec<MigrationChunkStorageProvider> CODEC;
   private IChunkStorageProvider<?>[] from;
   private IChunkStorageProvider<?> to;

   public MigrationChunkStorageProvider() {
   }

   public MigrationChunkStorageProvider(@Nonnull IChunkStorageProvider[] from, @Nonnull IChunkStorageProvider to) {
      this.from = from;
      this.to = to;
   }

   public MigrationData initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      MigrationData data = new MigrationData();
      data.loaderData = new Object[this.from.length];

      for(int i = 0; i < this.from.length; ++i) {
         data.loaderData[i] = this.from[i].initialize(store);
      }

      data.saverData = this.to.initialize(store);
      return data;
   }

   public void close(@NonNullDecl MigrationData migrationData, @NonNullDecl Store<ChunkStore> store) throws IOException {
      for(int i = 0; i < this.from.length; ++i) {
         this.from[i].close(migrationData.loaderData[i], store);
      }

      this.to.close(migrationData.saverData, store);
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull MigrationData migrationData, @Nonnull Store<ChunkStore> store) throws IOException {
      IChunkLoader[] loaders = new IChunkLoader[this.from.length];

      for(int i = 0; i < this.from.length; ++i) {
         loaders[i] = this.from[i].getLoader(migrationData.loaderData[i], store);
      }

      return new MigrationChunkLoader(loaders);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull MigrationData migrationData, @Nonnull Store<ChunkStore> store) throws IOException {
      return this.to.getSaver(migrationData.saverData, store);
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.from);
      return "MigrationChunkStorageProvider{from=" + var10000 + ", to=" + String.valueOf(this.to) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(MigrationChunkStorageProvider.class, MigrationChunkStorageProvider::new).documentation("A provider that combines multiple storage providers in a chain to assist with migrating worlds between storage formats.\n\nCan also be used to set storage to load chunks but block saving them if combined with the **Empty** storage provider")).append(new KeyedCodec("Loaders", new ArrayCodec(IChunkStorageProvider.CODEC, (x$0) -> new IChunkStorageProvider[x$0])), (migration, o) -> migration.from = o, (migration) -> migration.from).documentation("A list of storage providers to use as chunk loaders.\n\nEach loader will be tried in order to load a chunk, returning the chunk if found otherwise trying the next loaded until found or none are left.").add()).append(new KeyedCodec("Saver", IChunkStorageProvider.CODEC), (migration, o) -> migration.to = o, (migration) -> migration.to).documentation("The storage provider to use to save chunks.").add()).build();
   }

   public static class MigrationData {
      private Object[] loaderData;
      private Object saverData;
   }

   public static class MigrationChunkLoader implements IChunkLoader {
      @Nonnull
      private final IChunkLoader[] loaders;

      public MigrationChunkLoader(@Nonnull IChunkLoader... loaders) {
         this.loaders = loaders;
      }

      public void close() throws IOException {
         IOException exception = null;

         for(IChunkLoader loader : this.loaders) {
            try {
               loader.close();
            } catch (Exception e) {
               if (exception == null) {
                  exception = new IOException("Failed to close one or more loaders!");
               }

               exception.addSuppressed(e);
            }
         }

         if (exception != null) {
            throw exception;
         }
      }

      @Nonnull
      public CompletableFuture<Holder<ChunkStore>> loadHolder(int x, int z) {
         CompletableFuture<Holder<ChunkStore>> future = this.loaders[0].loadHolder(x, z);

         for(int i = 1; i < this.loaders.length; ++i) {
            IChunkLoader loader = this.loaders[i];
            future = future.handle((worldChunk, throwable) -> {
               if (throwable != null) {
                  return loader.loadHolder(x, z).exceptionally((throwable1) -> {
                     throwable1.addSuppressed(throwable);
                     throw SneakyThrow.sneakyThrow(throwable1);
                  });
               } else {
                  return worldChunk == null ? loader.loadHolder(x, z) : future;
               }
            }).thenCompose(Function.identity());
         }

         return future;
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         LongOpenHashSet indexes = new LongOpenHashSet();

         for(IChunkLoader loader : this.loaders) {
            indexes.addAll(loader.getIndexes());
         }

         return indexes;
      }
   }
}
