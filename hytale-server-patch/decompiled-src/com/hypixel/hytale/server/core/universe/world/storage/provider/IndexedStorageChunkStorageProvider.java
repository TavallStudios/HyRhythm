package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkSaver;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableSupplier;
import com.hypixel.hytale.storage.IndexedStorageFile;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class IndexedStorageChunkStorageProvider implements IChunkStorageProvider<IndexedStorageCache> {
   public static final String ID = "IndexedStorage";
   @Nonnull
   public static final BuilderCodec<IndexedStorageChunkStorageProvider> CODEC;
   private boolean flushOnWrite = false;

   public IndexedStorageCache initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      World world = ((ChunkStore)store.getExternalData()).getWorld();
      IndexedStorageCache cache = new IndexedStorageCache();
      cache.path = world.getSavePath().resolve("chunks");
      return cache;
   }

   public void close(@NonNullDecl IndexedStorageCache cache, @NonNullDecl Store<ChunkStore> store) throws IOException {
      cache.close();
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull IndexedStorageCache cache, @Nonnull Store<ChunkStore> store) {
      return new IndexedStorageChunkLoader(store, cache, this.flushOnWrite);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull IndexedStorageCache cache, @Nonnull Store<ChunkStore> store) {
      return new IndexedStorageChunkSaver(store, cache, this.flushOnWrite);
   }

   @Nonnull
   public String toString() {
      return "IndexedStorageChunkStorageProvider{}";
   }

   @Nonnull
   private static String toFileName(int regionX, int regionZ) {
      return regionX + "." + regionZ + ".region.bin";
   }

   private static long fromFileName(@Nonnull String fileName) {
      String[] split = fileName.split("\\.");
      if (split.length != 4) {
         throw new IllegalArgumentException("Unexpected file name format!");
      } else if (!"region".equals(split[2])) {
         throw new IllegalArgumentException("Unexpected file name format!");
      } else if (!"bin".equals(split[3])) {
         throw new IllegalArgumentException("Unexpected file extension!");
      } else {
         int regionX = Integer.parseInt(split[0]);
         int regionZ = Integer.parseInt(split[1]);
         return ChunkUtil.indexChunk(regionX, regionZ);
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(IndexedStorageChunkStorageProvider.class, IndexedStorageChunkStorageProvider::new).documentation("Uses the indexed storage file format to store chunks.")).appendInherited(new KeyedCodec("FlushOnWrite", Codec.BOOLEAN), (o, i) -> o.flushOnWrite = i, (o) -> o.flushOnWrite, (o, p) -> o.flushOnWrite = p.flushOnWrite).documentation("Controls whether the indexed storage flushes during writes.\nRecommended to be enabled to prevent corruption of chunks during unclean shutdowns.").add()).build();
   }

   public static class IndexedStorageChunkLoader extends BufferChunkLoader implements MetricProvider {
      @Nonnull
      private final IndexedStorageCache cache;
      private final boolean flushOnWrite;

      public IndexedStorageChunkLoader(@Nonnull Store<ChunkStore> store, @Nonnull IndexedStorageCache cache, boolean flushOnWrite) {
         super(store);
         this.cache = cache;
         this.flushOnWrite = flushOnWrite;
      }

      public void close() throws IOException {
      }

      @Nonnull
      public CompletableFuture<ByteBuffer> loadBuffer(int x, int z) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier((ThrowableSupplier)(() -> {
            IndexedStorageFile chunks = this.cache.getOrTryOpen(regionX, regionZ, this.flushOnWrite);
            return chunks == null ? null : chunks.readBlob(index);
         })));
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         return this.cache.getIndexes();
      }

      @Nullable
      public MetricResults toMetricResults() {
         return ((ChunkStore)this.getStore().getExternalData()).getSaver() instanceof IndexedStorageChunkSaver ? null : this.cache.toMetricResults();
      }
   }

   public static class IndexedStorageChunkSaver extends BufferChunkSaver implements MetricProvider {
      @Nonnull
      private final IndexedStorageCache cache;
      private final boolean flushOnWrite;

      protected IndexedStorageChunkSaver(@Nonnull Store<ChunkStore> store, @Nonnull IndexedStorageCache cache, boolean flushOnWrite) {
         super(store);
         this.cache = cache;
         this.flushOnWrite = flushOnWrite;
      }

      public void close() throws IOException {
      }

      @Nonnull
      public CompletableFuture<Void> saveBuffer(int x, int z, @Nonnull ByteBuffer buffer) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
            IndexedStorageFile chunks = this.cache.getOrCreate(regionX, regionZ, this.flushOnWrite);
            chunks.writeBlob(index, buffer);
         }));
      }

      @Nonnull
      public CompletableFuture<Void> removeBuffer(int x, int z) {
         int regionX = x >> 5;
         int regionZ = z >> 5;
         int localX = x & 31;
         int localZ = z & 31;
         int index = ChunkUtil.indexColumn(localX, localZ);
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
            IndexedStorageFile chunks = this.cache.getOrTryOpen(regionX, regionZ, this.flushOnWrite);
            if (chunks != null) {
               chunks.removeBlob(index);
            }

         }));
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         return this.cache.getIndexes();
      }

      public void flush() throws IOException {
         this.cache.flush();
      }

      public MetricResults toMetricResults() {
         return this.cache.toMetricResults();
      }
   }

   public static class IndexedStorageCache implements Closeable, MetricProvider, Resource<ChunkStore> {
      @Nonnull
      public static final MetricsRegistry<IndexedStorageCache> METRICS_REGISTRY;
      private final Long2ObjectConcurrentHashMap<IndexedStorageFile> cache;
      private Path path;

      public IndexedStorageCache() {
         this.cache = new Long2ObjectConcurrentHashMap<IndexedStorageFile>(true, ChunkUtil.NOT_FOUND);
      }

      @Nonnull
      public Long2ObjectConcurrentHashMap<IndexedStorageFile> getCache() {
         return this.cache;
      }

      public void close() throws IOException {
         IOException exception = null;
         Iterator<IndexedStorageFile> iterator = this.cache.values().iterator();

         while(iterator.hasNext()) {
            try {
               ((IndexedStorageFile)iterator.next()).close();
               iterator.remove();
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

      @Nullable
      public IndexedStorageFile getOrTryOpen(int regionX, int regionZ, boolean flushOnWrite) {
         return this.cache.computeIfAbsent(ChunkUtil.indexChunk(regionX, regionZ), (k) -> {
            Path regionFile = this.path.resolve(IndexedStorageChunkStorageProvider.toFileName(regionX, regionZ));
            if (!Files.exists(regionFile, new LinkOption[0])) {
               return null;
            } else {
               try {
                  IndexedStorageFile open = IndexedStorageFile.open(regionFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
                  open.setFlushOnWrite(flushOnWrite);
                  return open;
               } catch (FileNotFoundException var8) {
                  return null;
               } catch (IOException e) {
                  throw SneakyThrow.sneakyThrow(e);
               }
            }
         });
      }

      @Nonnull
      public IndexedStorageFile getOrCreate(int regionX, int regionZ, boolean flushOnWrite) {
         return this.cache.computeIfAbsent(ChunkUtil.indexChunk(regionX, regionZ), (k) -> {
            try {
               if (!Files.exists(this.path, new LinkOption[0])) {
                  try {
                     Files.createDirectory(this.path);
                  } catch (FileAlreadyExistsException var8) {
                  }
               }

               Path regionFile = this.path.resolve(IndexedStorageChunkStorageProvider.toFileName(regionX, regionZ));
               IndexedStorageFile open = IndexedStorageFile.open(regionFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
               open.setFlushOnWrite(flushOnWrite);
               return open;
            } catch (IOException e) {
               throw SneakyThrow.sneakyThrow(e);
            }
         });
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         if (!Files.exists(this.path, new LinkOption[0])) {
            return LongSets.EMPTY_SET;
         } else {
            LongOpenHashSet chunkIndexes = new LongOpenHashSet();
            Stream<Path> stream = Files.list(this.path);

            try {
               stream.forEach((path) -> {
                  if (!Files.isDirectory(path, new LinkOption[0])) {
                     long regionIndex;
                     try {
                        regionIndex = IndexedStorageChunkStorageProvider.fromFileName(path.getFileName().toString());
                     } catch (IllegalArgumentException var15) {
                        return;
                     }

                     int regionX = ChunkUtil.xOfChunkIndex(regionIndex);
                     int regionZ = ChunkUtil.zOfChunkIndex(regionIndex);
                     IndexedStorageFile regionFile = this.getOrTryOpen(regionX, regionZ, true);
                     if (regionFile != null) {
                        IntList blobIndexes = regionFile.keys();
                        IntListIterator iterator = blobIndexes.iterator();

                        while(iterator.hasNext()) {
                           int blobIndex = iterator.nextInt();
                           int localX = ChunkUtil.xFromColumn(blobIndex);
                           int localZ = ChunkUtil.zFromColumn(blobIndex);
                           int chunkX = regionX << 5 | localX;
                           int chunkZ = regionZ << 5 | localZ;
                           chunkIndexes.add(ChunkUtil.indexChunk(chunkX, chunkZ));
                        }

                     }
                  }
               });
            } catch (Throwable var6) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stream != null) {
               stream.close();
            }

            return chunkIndexes;
         }
      }

      public void flush() throws IOException {
         IOException exception = null;

         for(IndexedStorageFile indexedStorageFile : this.cache.values()) {
            try {
               indexedStorageFile.force(false);
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
      public MetricResults toMetricResults() {
         return METRICS_REGISTRY.toMetricResults(this);
      }

      @Nonnull
      public Resource<ChunkStore> clone() {
         return new IndexedStorageCache();
      }

      static {
         METRICS_REGISTRY = (new MetricsRegistry()).register("Files", (cache) -> (CacheEntryMetricData[])cache.cache.long2ObjectEntrySet().stream().map(CacheEntryMetricData::new).toArray((x$0) -> new CacheEntryMetricData[x$0]), new ArrayCodec(IndexedStorageChunkStorageProvider.IndexedStorageCache.CacheEntryMetricData.CODEC, (x$0) -> new CacheEntryMetricData[x$0]));
      }

      private static class CacheEntryMetricData {
         @Nonnull
         private static final Codec<CacheEntryMetricData> CODEC;
         private long key;
         private IndexedStorageFile value;

         public CacheEntryMetricData() {
         }

         public CacheEntryMetricData(@Nonnull Long2ObjectMap.Entry<IndexedStorageFile> entry) {
            this.key = entry.getLongKey();
            this.value = (IndexedStorageFile)entry.getValue();
         }

         static {
            CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(CacheEntryMetricData.class, CacheEntryMetricData::new).append(new KeyedCodec("Key", Codec.LONG), (entry, o) -> entry.key = o, (entry) -> entry.key).add()).append(new KeyedCodec("File", IndexedStorageFile.METRICS_REGISTRY), (entry, o) -> entry.value = o, (entry) -> entry.value).add()).build();
         }
      }
   }
}
