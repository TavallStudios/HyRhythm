package com.hypixel.hytale.server.core.universe.world.storage.provider;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.BufferChunkSaver;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkLoader;
import com.hypixel.hytale.server.core.universe.world.storage.IChunkSaver;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableSupplier;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompactionPriority;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.DBOptions;
import org.rocksdb.FlushOptions;
import org.rocksdb.IndexType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksDbChunkStorageProvider implements IChunkStorageProvider<RocksDbResource> {
   public static final String ID = "RocksDb";
   public static final BuilderCodec<RocksDbChunkStorageProvider> CODEC = BuilderCodec.builder(RocksDbChunkStorageProvider.class, RocksDbChunkStorageProvider::new).build();

   public RocksDbResource initialize(@NonNullDecl Store<ChunkStore> store) throws IOException {
      try {
         Options options = (new Options()).setCreateIfMissing(true).setCreateMissingColumnFamilies(true).setIncreaseParallelism(ForkJoinPool.getCommonPoolParallelism());

         RocksDbResource var9;
         try {
            BloomFilter bloomFilter = new BloomFilter(9.9);

            try {
               ColumnFamilyOptions chunkColumnOptions = (new ColumnFamilyOptions()).setCompressionType(CompressionType.LZ4_COMPRESSION).setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION).setTableFormatConfig((new BlockBasedTableConfig()).setIndexType(IndexType.kHashSearch).setFilterPolicy(bloomFilter).setOptimizeFiltersForMemory(true)).setCompactionStyle(CompactionStyle.LEVEL).optimizeLevelStyleCompaction(134217728L).setLevelCompactionDynamicLevelBytes(true).setCompactionPriority(CompactionPriority.MinOverlappingRatio).useFixedLengthPrefixExtractor(8).setEnableBlobFiles(true).setEnableBlobGarbageCollection(true).setBlobCompressionType(CompressionType.ZSTD_COMPRESSION);

               try {
                  DBOptions dbOptions = new DBOptions(options);

                  try {
                     RocksDbResource resource = new RocksDbResource();
                     List<ColumnFamilyDescriptor> columns = List.of(new ColumnFamilyDescriptor("default".getBytes(StandardCharsets.UTF_8)), new ColumnFamilyDescriptor("chunks".getBytes(StandardCharsets.UTF_8), chunkColumnOptions));
                     ArrayList<ColumnFamilyHandle> handles = new ArrayList();
                     resource.db = RocksDB.open(dbOptions, String.valueOf(((ChunkStore)store.getExternalData()).getWorld().getSavePath().resolve("db")), columns, handles);
                     resource.chunkColumn = (ColumnFamilyHandle)handles.get(1);
                     ((ColumnFamilyHandle)handles.get(0)).close();
                     var9 = resource;
                  } catch (Throwable var14) {
                     try {
                        dbOptions.close();
                     } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                     }

                     throw var14;
                  }

                  dbOptions.close();
               } catch (Throwable var15) {
                  if (chunkColumnOptions != null) {
                     try {
                        chunkColumnOptions.close();
                     } catch (Throwable var12) {
                        var15.addSuppressed(var12);
                     }
                  }

                  throw var15;
               }

               if (chunkColumnOptions != null) {
                  chunkColumnOptions.close();
               }
            } catch (Throwable var16) {
               try {
                  bloomFilter.close();
               } catch (Throwable var11) {
                  var16.addSuppressed(var11);
               }

               throw var16;
            }

            bloomFilter.close();
         } catch (Throwable var17) {
            if (options != null) {
               try {
                  options.close();
               } catch (Throwable var10) {
                  var17.addSuppressed(var10);
               }
            }

            throw var17;
         }

         if (options != null) {
            options.close();
         }

         return var9;
      } catch (RocksDBException e) {
         throw SneakyThrow.sneakyThrow(e);
      }
   }

   public void close(@NonNullDecl RocksDbResource resource, @NonNullDecl Store<ChunkStore> store) throws IOException {
      try {
         resource.db.syncWal();
      } catch (RocksDBException e) {
         throw SneakyThrow.sneakyThrow(e);
      }

      resource.chunkColumn.close();
      resource.db.close();
      resource.db = null;
   }

   @Nonnull
   public IChunkLoader getLoader(@Nonnull RocksDbResource resource, @Nonnull Store<ChunkStore> store) throws IOException {
      return new Loader(store, resource);
   }

   @Nonnull
   public IChunkSaver getSaver(@Nonnull RocksDbResource resource, @Nonnull Store<ChunkStore> store) throws IOException {
      return new Saver(store, resource);
   }

   private static byte[] toKey(int x, int z) {
      byte[] key = new byte[8];
      key[0] = (byte)(x >>> 24);
      key[1] = (byte)(x >>> 16);
      key[2] = (byte)(x >>> 8);
      key[3] = (byte)x;
      key[4] = (byte)(z >>> 24);
      key[5] = (byte)(z >>> 16);
      key[6] = (byte)(z >>> 8);
      key[7] = (byte)z;
      return key;
   }

   private static int keyToX(byte[] key) {
      return (key[0] & 255) << 24 | (key[1] & 255) << 16 | (key[2] & 255) << 8 | key[3] & 255;
   }

   private static int keyToZ(byte[] key) {
      return (key[4] & 255) << 24 | (key[5] & 255) << 16 | (key[6] & 255) << 8 | key[7] & 255;
   }

   public static class Loader extends BufferChunkLoader implements IChunkLoader {
      private final RocksDbResource db;

      public Loader(Store<ChunkStore> store, RocksDbResource db) {
         super(store);
         this.db = db;
      }

      public CompletableFuture<ByteBuffer> loadBuffer(int x, int z) {
         return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier((ThrowableSupplier)(() -> {
            byte[] key = RocksDbChunkStorageProvider.toKey(x, z);
            byte[] data = this.db.db.get(this.db.chunkColumn, key);
            return data == null ? null : ByteBuffer.wrap(data);
         })));
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         LongOpenHashSet set = new LongOpenHashSet();
         RocksIterator iter = this.db.db.newIterator(this.db.chunkColumn);

         try {
            iter.seekToFirst();

            while(iter.isValid()) {
               byte[] key = iter.key();
               set.add(ChunkUtil.indexChunk(RocksDbChunkStorageProvider.keyToX(key), RocksDbChunkStorageProvider.keyToZ(key)));
               iter.next();
            }
         } catch (Throwable var6) {
            if (iter != null) {
               try {
                  iter.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (iter != null) {
            iter.close();
         }

         return set;
      }

      public void close() throws IOException {
      }
   }

   public static class Saver extends BufferChunkSaver implements IChunkSaver {
      private final RocksDbResource db;

      public Saver(Store<ChunkStore> store, RocksDbResource db) {
         super(store);
         this.db = db;
      }

      @Nonnull
      public CompletableFuture<Void> saveBuffer(int x, int z, @Nonnull ByteBuffer buffer) {
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
            if (buffer.hasArray()) {
               this.db.db.put(this.db.chunkColumn, RocksDbChunkStorageProvider.toKey(x, z), buffer.array());
            } else {
               byte[] buf = new byte[buffer.remaining()];
               buffer.get(buf);
               this.db.db.put(this.db.chunkColumn, RocksDbChunkStorageProvider.toKey(x, z), buf);
            }

         }));
      }

      @Nonnull
      public CompletableFuture<Void> removeBuffer(int x, int z) {
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> this.db.db.delete(this.db.chunkColumn, RocksDbChunkStorageProvider.toKey(x, z))));
      }

      @Nonnull
      public LongSet getIndexes() throws IOException {
         LongOpenHashSet set = new LongOpenHashSet();
         RocksIterator iter = this.db.db.newIterator(this.db.chunkColumn);

         try {
            iter.seekToFirst();

            while(iter.isValid()) {
               byte[] key = iter.key();
               set.add(ChunkUtil.indexChunk(RocksDbChunkStorageProvider.keyToX(key), RocksDbChunkStorageProvider.keyToZ(key)));
               iter.next();
            }
         } catch (Throwable var6) {
            if (iter != null) {
               try {
                  iter.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (iter != null) {
            iter.close();
         }

         return set;
      }

      public void flush() throws IOException {
         try {
            FlushOptions opts = (new FlushOptions()).setWaitForFlush(true);

            try {
               this.db.db.flush(opts);
            } catch (Throwable var5) {
               if (opts != null) {
                  try {
                     opts.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }
               }

               throw var5;
            }

            if (opts != null) {
               opts.close();
            }

         } catch (RocksDBException e) {
            throw SneakyThrow.sneakyThrow(e);
         }
      }

      public void close() throws IOException {
      }
   }

   public static class RocksDbResource {
      public RocksDB db;
      public ColumnFamilyHandle chunkColumn;
   }
}
