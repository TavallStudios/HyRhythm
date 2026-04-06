package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nonnull;

public interface IChunkStorageProvider<Data> {
   @Nonnull
   BuilderCodecMapCodec<IChunkStorageProvider<?>> CODEC = new BuilderCodecMapCodec<IChunkStorageProvider<?>>("Type", true);

   Data initialize(@Nonnull Store<ChunkStore> var1) throws IOException;

   default <OtherData> Data migrateFrom(@Nonnull Store<ChunkStore> store, IChunkStorageProvider<OtherData> other) throws IOException {
      OtherData oldData = other.initialize(store);
      Data newData = (Data)this.initialize(store);

      try {
         IChunkLoader oldLoader = other.getLoader(oldData, store);

         try {
            IChunkSaver newSaver = this.getSaver(newData, store);

            try {
               World world = ((ChunkStore)store.getExternalData()).getWorld();
               HytaleLogger logger = world.getLogger();
               LongSet chunks = oldLoader.getIndexes();
               LongIterator iterator = chunks.iterator();
               ((HytaleLogger.Api)logger.atInfo()).log("Migrating %d chunks", chunks.size());
               HytaleServer.get().reportSingleplayerStatus(String.format("Migrating chunks for %s", world.getName()), 0.0);
               int count = 0;
               ArrayList<CompletableFuture<Void>> inFlight = new ArrayList();

               while(iterator.hasNext()) {
                  long chunk = iterator.nextLong();
                  int chunkX = ChunkUtil.xOfChunkIndex(chunk);
                  int chunkZ = ChunkUtil.zOfChunkIndex(chunk);
                  inFlight.add(oldLoader.loadHolder(chunkX, chunkZ).thenCompose((v) -> newSaver.saveHolder(chunkX, chunkZ, v)).exceptionally((t) -> {
                     ((HytaleLogger.Api)((HytaleLogger.Api)logger.atSevere()).withCause(t)).log("Failed to load chunk at %d, %d, skipping", chunkX, chunkZ);
                     return null;
                  }));
                  ++count;
                  if (count % 100 == 0) {
                     ((HytaleLogger.Api)logger.atInfo()).log("Migrated %d/%d chunks", count, chunks.size());
                     double progress = MathUtil.round((double)count / (double)chunks.size(), 2) * 100.0;
                     HytaleServer.get().reportSingleplayerStatus(String.format("Migrating chunks for %s", world.getName()), progress);
                  }

                  inFlight.removeIf(CompletableFuture::isDone);
                  if (inFlight.size() >= ForkJoinPool.getCommonPoolParallelism()) {
                     CompletableFuture.anyOf((CompletableFuture[])inFlight.toArray((x$0) -> new CompletableFuture[x$0])).join();
                     inFlight.removeIf(CompletableFuture::isDone);
                  }
               }

               CompletableFuture.allOf((CompletableFuture[])inFlight.toArray((x$0) -> new CompletableFuture[x$0])).join();
               inFlight.clear();
               ((HytaleLogger.Api)logger.atInfo()).log("Finished migrating %d chunks", chunks.size());
            } catch (Throwable var27) {
               if (newSaver != null) {
                  try {
                     newSaver.close();
                  } catch (Throwable var26) {
                     var27.addSuppressed(var26);
                  }
               }

               throw var27;
            }

            if (newSaver != null) {
               newSaver.close();
            }
         } catch (Throwable var28) {
            if (oldLoader != null) {
               try {
                  oldLoader.close();
               } catch (Throwable var25) {
                  var28.addSuppressed(var25);
               }
            }

            throw var28;
         }

         if (oldLoader != null) {
            oldLoader.close();
         }
      } finally {
         other.close(oldData, store);
      }

      return newData;
   }

   void close(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   @Nonnull
   IChunkLoader getLoader(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   @Nonnull
   IChunkSaver getSaver(@Nonnull Data var1, @Nonnull Store<ChunkStore> var2) throws IOException;

   default boolean isSame(IChunkStorageProvider<?> other) {
      return other.getClass().equals(this.getClass());
   }
}
