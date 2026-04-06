package com.hypixel.hytale.server.core.universe.world.storage;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.store.CodecKey;
import com.hypixel.hytale.codec.store.CodecStore;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.IResourceStorage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.data.EntityDataSystem;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkUnloadingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.LongPredicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkStore implements WorldProvider {
   @Nonnull
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final MetricsRegistry<ChunkStore> METRICS_REGISTRY;
   public static final long MAX_FAILURE_BACKOFF_NANOS;
   public static final long FAILURE_BACKOFF_NANOS;
   public static final ComponentRegistry<ChunkStore> REGISTRY;
   public static final CodecKey<Holder<ChunkStore>> HOLDER_CODEC_KEY;
   @Nonnull
   public static final SystemType<ChunkStore, LoadPacketDataQuerySystem> LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE;
   @Nonnull
   public static final SystemType<ChunkStore, LoadFuturePacketDataQuerySystem> LOAD_FUTURE_PACKETS_DATA_QUERY_SYSTEM_TYPE;
   @Nonnull
   public static final SystemType<ChunkStore, UnloadPacketDataQuerySystem> UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE;
   @Nonnull
   public static final ResourceType<ChunkStore, ChunkUnloadingSystem.Data> UNLOAD_RESOURCE;
   @Nonnull
   public static final ResourceType<ChunkStore, ChunkSavingSystems.Data> SAVE_RESOURCE;
   public static final SystemGroup<ChunkStore> INIT_GROUP;
   @Nonnull
   private final World world;
   @Nonnull
   private final Long2ObjectConcurrentHashMap<ChunkLoadState> chunks;
   private Store<ChunkStore> store;
   private Object storageData;
   @Nullable
   private IChunkLoader loader;
   @Nullable
   private IChunkSaver saver;
   @Nullable
   private IWorldGen generator;
   @Nonnull
   private CompletableFuture<Void> generatorLoaded;
   private final StampedLock generatorLock;
   private final AtomicInteger totalGeneratedChunksCount;
   private final AtomicInteger totalLoadedChunksCount;

   public ChunkStore(@Nonnull World world) {
      this.chunks = new Long2ObjectConcurrentHashMap<ChunkLoadState>(true, ChunkUtil.NOT_FOUND);
      this.generatorLoaded = new CompletableFuture();
      this.generatorLock = new StampedLock();
      this.totalGeneratedChunksCount = new AtomicInteger();
      this.totalLoadedChunksCount = new AtomicInteger();
      this.world = world;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   @Nonnull
   public Store<ChunkStore> getStore() {
      return this.store;
   }

   public Object getStorageData() {
      return this.storageData;
   }

   @Nullable
   public IChunkLoader getLoader() {
      return this.loader;
   }

   @Nullable
   public IChunkSaver getSaver() {
      return this.saver;
   }

   @Nullable
   public IWorldGen getGenerator() {
      long readStamp = this.generatorLock.readLock();

      IWorldGen var3;
      try {
         var3 = this.generator;
      } finally {
         this.generatorLock.unlockRead(readStamp);
      }

      return var3;
   }

   public void shutdownGenerator() {
      this.setGenerator((IWorldGen)null);
   }

   public void setGenerator(@Nullable IWorldGen generator) {
      long writeStamp = this.generatorLock.writeLock();

      try {
         if (this.generator != null) {
            this.generator.shutdown();
         }

         this.totalGeneratedChunksCount.set(0);
         this.generator = generator;
         if (generator != null) {
            this.generatorLoaded.complete((Object)null);
            this.generatorLoaded = new CompletableFuture();
         }
      } finally {
         this.generatorLock.unlockWrite(writeStamp);
      }

   }

   @Nonnull
   public LongSet getChunkIndexes() {
      return LongSets.unmodifiable(this.chunks.keySet());
   }

   public int getLoadedChunksCount() {
      return this.chunks.size();
   }

   public int getTotalGeneratedChunksCount() {
      return this.totalGeneratedChunksCount.get();
   }

   public int getTotalLoadedChunksCount() {
      return this.totalLoadedChunksCount.get();
   }

   public void start(@Nonnull IResourceStorage resourceStorage) {
      this.store = REGISTRY.addStore(this, resourceStorage, (store) -> this.store = store);
   }

   public void waitForLoadingChunks() {
      long start = System.nanoTime();

      boolean hasLoadingChunks;
      do {
         this.world.consumeTaskQueue();
         Thread.yield();
         hasLoadingChunks = false;
         ObjectIterator var4 = this.chunks.long2ObjectEntrySet().iterator();

         while(var4.hasNext()) {
            Long2ObjectMap.Entry<ChunkLoadState> entry = (Long2ObjectMap.Entry)var4.next();
            ChunkLoadState chunkState = (ChunkLoadState)entry.getValue();
            long stamp = chunkState.lock.readLock();

            try {
               CompletableFuture<Ref<ChunkStore>> future = chunkState.future;
               if (future != null && !future.isDone()) {
                  hasLoadingChunks = true;
                  break;
               }
            } finally {
               chunkState.lock.unlockRead(stamp);
            }
         }
      } while(hasLoadingChunks && System.nanoTime() - start <= 5000000000L);

      this.world.consumeTaskQueue();
   }

   public void shutdown() {
      this.store.shutdown();
      this.chunks.clear();
   }

   @Nonnull
   private Ref<ChunkStore> add(@Nonnull Holder<ChunkStore> holder) {
      this.world.debugAssertInTickingThread();
      WorldChunk worldChunkComponent = (WorldChunk)holder.getComponent(WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      ChunkLoadState chunkState = this.chunks.get(worldChunkComponent.getIndex());
      if (chunkState == null) {
         throw new IllegalStateException("Expected the ChunkLoadState to exist!");
      } else {
         Ref<ChunkStore> oldReference = null;
         long stamp = chunkState.lock.writeLock();

         try {
            if (chunkState.future == null) {
               throw new IllegalStateException("Expected the ChunkLoadState to have a future!");
            }

            if (chunkState.reference != null) {
               oldReference = chunkState.reference;
               chunkState.reference = null;
            }
         } finally {
            chunkState.lock.unlockWrite(stamp);
         }

         if (oldReference != null) {
            WorldChunk oldWorldChunkComponent = (WorldChunk)this.store.getComponent(oldReference, WorldChunk.getComponentType());

            assert oldWorldChunkComponent != null;

            oldWorldChunkComponent.setFlag(ChunkFlag.TICKING, false);
            this.store.removeEntity(oldReference, RemoveReason.REMOVE);
            this.world.getNotificationHandler().updateChunk(worldChunkComponent.getIndex());
         }

         oldReference = this.store.addEntity(holder, AddReason.SPAWN);
         if (oldReference == null) {
            throw new UnsupportedOperationException("Unable to add the chunk to the world!");
         } else {
            worldChunkComponent.setReference(oldReference);
            stamp = chunkState.lock.writeLock();

            Ref var17;
            try {
               chunkState.reference = oldReference;
               chunkState.flags = 0;
               chunkState.future = null;
               chunkState.throwable = null;
               chunkState.failedWhen = 0L;
               chunkState.failedCounter = 0;
               var17 = oldReference;
            } finally {
               chunkState.lock.unlockWrite(stamp);
            }

            return var17;
         }
      }
   }

   public void remove(@Nonnull Ref<ChunkStore> reference, @Nonnull RemoveReason reason) {
      this.world.debugAssertInTickingThread();
      WorldChunk worldChunkComponent = (WorldChunk)this.store.getComponent(reference, WorldChunk.getComponentType());

      assert worldChunkComponent != null;

      long index = worldChunkComponent.getIndex();
      ChunkLoadState chunkState = this.chunks.get(index);
      long stamp = chunkState.lock.readLock();

      try {
         worldChunkComponent.setFlag(ChunkFlag.TICKING, false);
         this.store.removeEntity(reference, reason);
         if (chunkState.future != null) {
            chunkState.reference = null;
         } else {
            this.chunks.remove(index, chunkState);
         }
      } finally {
         chunkState.lock.unlockRead(stamp);
      }

   }

   @Nullable
   public Ref<ChunkStore> getChunkReference(long index) {
      ChunkLoadState chunkState = this.chunks.get(index);
      if (chunkState == null) {
         return null;
      } else {
         long stamp = chunkState.lock.tryOptimisticRead();
         Ref<ChunkStore> reference = chunkState.reference;
         if (chunkState.lock.validate(stamp)) {
            return reference;
         } else {
            stamp = chunkState.lock.readLock();

            Ref var7;
            try {
               var7 = chunkState.reference;
            } finally {
               chunkState.lock.unlockRead(stamp);
            }

            return var7;
         }
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunkSectionReference(int x, int y, int z) {
      Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
      if (ref == null) {
         return null;
      } else {
         ChunkColumn chunkColumnComponent = (ChunkColumn)this.store.getComponent(ref, ChunkColumn.getComponentType());
         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
      }
   }

   @Nullable
   public Ref<ChunkStore> getChunkSectionReference(@Nonnull ComponentAccessor<ChunkStore> commandBuffer, int x, int y, int z) {
      Ref<ChunkStore> ref = this.getChunkReference(ChunkUtil.indexChunk(x, z));
      if (ref == null) {
         return null;
      } else {
         ChunkColumn chunkColumnComponent = (ChunkColumn)commandBuffer.getComponent(ref, ChunkColumn.getComponentType());
         return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
      }
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkSectionReferenceAsync(int x, int y, int z) {
      return y >= 0 && y < 10 ? this.getChunkReferenceAsync(ChunkUtil.indexChunk(x, z)).thenApplyAsync((ref) -> {
         if (ref != null && ref.isValid()) {
            Store<ChunkStore> store = ref.getStore();
            ChunkColumn chunkColumnComponent = (ChunkColumn)store.getComponent(ref, ChunkColumn.getComponentType());
            return chunkColumnComponent == null ? null : chunkColumnComponent.getSection(y);
         } else {
            return null;
         }
      }, ((ChunkStore)this.store.getExternalData()).getWorld()) : CompletableFuture.failedFuture(new IndexOutOfBoundsException("Invalid y: " + y));
   }

   @Nullable
   public <T extends Component<ChunkStore>> T getChunkComponent(long index, @Nonnull ComponentType<ChunkStore, T> componentType) {
      Ref<ChunkStore> reference = this.getChunkReference(index);
      return (T)(reference != null && reference.isValid() ? this.store.getComponent(reference, componentType) : null);
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkReferenceAsync(long index) {
      return this.getChunkReferenceAsync(index, 0);
   }

   @Nonnull
   public CompletableFuture<Ref<ChunkStore>> getChunkReferenceAsync(long index, int flags) {
      if (this.store.isShutdown()) {
         return CompletableFuture.completedFuture((Object)null);
      } else {
         ChunkLoadState chunkState;
         if ((flags & 3) == 3) {
            chunkState = this.chunks.get(index);
            if (chunkState == null) {
               return CompletableFuture.completedFuture((Object)null);
            }

            long stamp = chunkState.lock.readLock();

            try {
               if ((flags & 4) == 0 || (chunkState.flags & 4) != 0) {
                  if (chunkState.reference != null) {
                     CompletableFuture var30 = CompletableFuture.completedFuture(chunkState.reference);
                     return var30;
                  }

                  if (chunkState.future == null) {
                     CompletableFuture var29 = CompletableFuture.completedFuture((Object)null);
                     return var29;
                  }

                  CompletableFuture var7 = chunkState.future;
                  return var7;
               }
            } finally {
               chunkState.lock.unlockRead(stamp);
            }
         } else {
            chunkState = this.chunks.computeIfAbsent(index, (l) -> new ChunkLoadState());
         }

         long stamp = chunkState.lock.writeLock();
         if (chunkState.future == null && chunkState.reference != null && (flags & 8) == 0) {
            Ref<ChunkStore> reference = chunkState.reference;
            if ((flags & 4) == 0) {
               chunkState.lock.unlockWrite(stamp);
               return CompletableFuture.completedFuture(reference);
            } else if (this.world.isInThread() && (flags & -2147483648) == 0) {
               chunkState.lock.unlockWrite(stamp);
               WorldChunk worldChunkComponent = (WorldChunk)this.store.getComponent(reference, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
               return CompletableFuture.completedFuture(reference);
            } else {
               chunkState.lock.unlockWrite(stamp);
               return CompletableFuture.supplyAsync(() -> {
                  WorldChunk worldChunkComponent = (WorldChunk)this.store.getComponent(reference, WorldChunk.getComponentType());

                  assert worldChunkComponent != null;

                  worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
                  return reference;
               }, this.world);
            }
         } else {
            try {
               if (chunkState.throwable != null) {
                  long nanosSince = System.nanoTime() - chunkState.failedWhen;
                  int count = chunkState.failedCounter;
                  if (nanosSince < Math.min(MAX_FAILURE_BACKOFF_NANOS, (long)(count * count) * FAILURE_BACKOFF_NANOS)) {
                     CompletableFuture var38 = CompletableFuture.failedFuture(new RuntimeException("Chunk failure backoff", chunkState.throwable));
                     return var38;
                  }

                  chunkState.throwable = null;
                  chunkState.failedWhen = 0L;
               }

               boolean isNew = chunkState.future == null;
               if (isNew) {
                  chunkState.flags = flags;
               }

               int x = ChunkUtil.xOfChunkIndex(index);
               int z = ChunkUtil.zOfChunkIndex(index);
               if ((isNew || (chunkState.flags & 1) != 0) && (flags & 1) == 0) {
                  if (chunkState.future == null) {
                     chunkState.future = this.loader.loadHolder(x, z).thenApplyAsync((holder) -> {
                        if (holder != null && !this.store.isShutdown()) {
                           this.totalLoadedChunksCount.getAndIncrement();
                           return this.preLoadChunkAsync(index, holder, false);
                        } else {
                           return null;
                        }
                     }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally((throwable) -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to load chunk! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  } else {
                     chunkState.flags &= -2;
                     chunkState.future = chunkState.future.thenCompose((reference) -> reference != null ? CompletableFuture.completedFuture(reference) : this.loader.loadHolder(x, z).thenApplyAsync((holder) -> {
                           if (holder != null && !this.store.isShutdown()) {
                              this.totalLoadedChunksCount.getAndIncrement();
                              return this.preLoadChunkAsync(index, holder, false);
                           } else {
                              return null;
                           }
                        }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally((throwable) -> {
                           ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to load chunk! %s, %s", x, z);
                           chunkState.fail(throwable);
                           throw SneakyThrow.sneakyThrow(throwable);
                        }));
                  }
               }

               if ((isNew || (chunkState.flags & 2) != 0) && (flags & 2) == 0) {
                  int seed = (int)this.world.getWorldConfig().getSeed();
                  if (chunkState.future == null) {
                     long readStamp = this.generatorLock.readLock();

                     CompletableFuture<GeneratedChunk> future;
                     try {
                        if (this.generator == null) {
                           future = this.generatorLoaded.thenCompose((aVoid) -> this.generator.generate(seed, index, x, z, (flags & 16) != 0 ? this::isChunkStillNeeded : null));
                        } else {
                           future = this.generator.generate(seed, index, x, z, (flags & 16) != 0 ? this::isChunkStillNeeded : null);
                        }
                     } finally {
                        this.generatorLock.unlockRead(readStamp);
                     }

                     chunkState.future = future.thenApplyAsync((generatedChunk) -> {
                        if (generatedChunk != null && !this.store.isShutdown()) {
                           this.totalGeneratedChunksCount.getAndIncrement();
                           return this.preLoadChunkAsync(index, generatedChunk.toHolder(this.world), true);
                        } else {
                           return null;
                        }
                     }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally((throwable) -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to generate chunk! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  } else {
                     chunkState.flags &= -3;
                     chunkState.future = chunkState.future.thenCompose((reference) -> {
                        if (reference != null) {
                           return CompletableFuture.completedFuture(reference);
                        } else {
                           long readStamp = this.generatorLock.readLock();

                           CompletableFuture<GeneratedChunk> future;
                           try {
                              if (this.generator == null) {
                                 future = this.generatorLoaded.thenCompose((aVoid) -> this.generator.generate(seed, index, x, z, (LongPredicate)null));
                              } else {
                                 future = this.generator.generate(seed, index, x, z, (LongPredicate)null);
                              }
                           } finally {
                              this.generatorLock.unlockRead(readStamp);
                           }

                           return future.thenApplyAsync((generatedChunk) -> {
                              if (generatedChunk != null && !this.store.isShutdown()) {
                                 this.totalGeneratedChunksCount.getAndIncrement();
                                 return this.preLoadChunkAsync(index, generatedChunk.toHolder(this.world), true);
                              } else {
                                 return null;
                              }
                           }).thenApplyAsync(this::postLoadChunk, this.world).exceptionally((throwable) -> {
                              ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to generate chunk! %s, %s", x, z);
                              chunkState.fail(throwable);
                              throw SneakyThrow.sneakyThrow(throwable);
                           });
                        }
                     });
                  }
               }

               if ((isNew || (chunkState.flags & 4) == 0) && (flags & 4) != 0) {
                  chunkState.flags |= 4;
                  if (chunkState.future != null) {
                     chunkState.future = chunkState.future.thenApplyAsync((reference) -> {
                        if (reference != null) {
                           WorldChunk worldChunkComponent = (WorldChunk)this.store.getComponent(reference, WorldChunk.getComponentType());

                           assert worldChunkComponent != null;

                           worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
                        }

                        return reference;
                     }, this.world).exceptionally((throwable) -> {
                        ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to set chunk ticking! %s, %s", x, z);
                        chunkState.fail(throwable);
                        throw SneakyThrow.sneakyThrow(throwable);
                     });
                  }
               }

               if (chunkState.future == null) {
                  CompletableFuture var37 = CompletableFuture.completedFuture((Object)null);
                  return var37;
               } else {
                  CompletableFuture var36 = chunkState.future;
                  return var36;
               }
            } finally {
               chunkState.lock.unlockWrite(stamp);
            }
         }
      }
   }

   private boolean isChunkStillNeeded(long index) {
      for(PlayerRef playerRef : this.world.getPlayerRefs()) {
         if (playerRef.getChunkTracker().shouldBeVisible(index)) {
            return true;
         }
      }

      return false;
   }

   public boolean isChunkOnBackoff(long index, long maxFailureBackoffNanos) {
      ChunkLoadState chunkState = this.chunks.get(index);
      if (chunkState == null) {
         return false;
      } else {
         long stamp = chunkState.lock.readLock();

         boolean var8;
         try {
            if (chunkState.throwable != null) {
               long nanosSince = System.nanoTime() - chunkState.failedWhen;
               int count = chunkState.failedCounter;
               boolean var11 = nanosSince < Math.min(maxFailureBackoffNanos, (long)(count * count) * FAILURE_BACKOFF_NANOS);
               return var11;
            }

            var8 = false;
         } finally {
            chunkState.lock.unlockRead(stamp);
         }

         return var8;
      }
   }

   @Nonnull
   private Holder<ChunkStore> preLoadChunkAsync(long index, @Nonnull Holder<ChunkStore> holder, boolean newlyGenerated) {
      WorldChunk worldChunkComponent = (WorldChunk)holder.getComponent(WorldChunk.getComponentType());
      if (worldChunkComponent == null) {
         throw new IllegalStateException(String.format("Holder missing WorldChunk component! (%d, %d)", ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index)));
      } else if (worldChunkComponent.getIndex() != index) {
         throw new IllegalStateException(String.format("Incorrect chunk index! Got (%d, %d) expected (%d, %d)", worldChunkComponent.getX(), worldChunkComponent.getZ(), ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index)));
      } else {
         BlockChunk blockChunk = (BlockChunk)holder.getComponent(BlockChunk.getComponentType());
         if (blockChunk == null) {
            throw new IllegalStateException(String.format("Holder missing BlockChunk component! (%d, %d)", ChunkUtil.xOfChunkIndex(index), ChunkUtil.zOfChunkIndex(index)));
         } else {
            blockChunk.loadFromHolder(holder);
            worldChunkComponent.setFlag(ChunkFlag.NEWLY_GENERATED, newlyGenerated);
            worldChunkComponent.setLightingUpdatesEnabled(false);
            if (newlyGenerated && this.world.getWorldConfig().shouldSaveNewChunks()) {
               worldChunkComponent.markNeedsSaving();
            }

            try {
               long start = System.nanoTime();
               IEventDispatcher<ChunkPreLoadProcessEvent, ChunkPreLoadProcessEvent> dispatcher = HytaleServer.get().getEventBus().dispatchFor(ChunkPreLoadProcessEvent.class, this.world.getName());
               if (dispatcher.hasListener()) {
                  ChunkPreLoadProcessEvent event = dispatcher.dispatch(new ChunkPreLoadProcessEvent(holder, worldChunkComponent, newlyGenerated, start));
                  if (!event.didLog()) {
                     long end = System.nanoTime();
                     long diff = end - start;
                     if (diff > (long)this.world.getTickStepNanos()) {
                        LOGGER.at(Level.SEVERE).log("Took too long to pre-load process chunk: %s > TICK_STEP, Has GC Run: %s, %s", FormatUtil.nanosToString(diff), this.world.consumeGCHasRun(), worldChunkComponent);
                     }
                  }
               }
            } finally {
               worldChunkComponent.setLightingUpdatesEnabled(true);
            }

            return holder;
         }
      }
   }

   @Nullable
   private Ref<ChunkStore> postLoadChunk(@Nullable Holder<ChunkStore> holder) {
      this.world.debugAssertInTickingThread();
      if (holder != null && !this.store.isShutdown()) {
         long start = System.nanoTime();
         WorldChunk worldChunkComponent = (WorldChunk)holder.getComponent(WorldChunk.getComponentType());

         assert worldChunkComponent != null;

         worldChunkComponent.setFlag(ChunkFlag.START_INIT, true);
         if (worldChunkComponent.is(ChunkFlag.TICKING)) {
            holder.tryRemoveComponent(REGISTRY.getNonTickingComponentType());
         } else {
            holder.ensureComponent(REGISTRY.getNonTickingComponentType());
         }

         Ref<ChunkStore> reference = this.add(holder);
         worldChunkComponent.initFlags();
         this.world.getChunkLighting().init(worldChunkComponent);
         long end = System.nanoTime();
         long diff = end - start;
         if (diff > (long)this.world.getTickStepNanos()) {
            LOGGER.at(Level.SEVERE).log("Took too long to post-load process chunk: %s > TICK_STEP, Has GC Run: %s, %s", FormatUtil.nanosToString(diff), this.world.consumeGCHasRun(), worldChunkComponent);
         }

         return reference;
      } else {
         return null;
      }
   }

   static {
      METRICS_REGISTRY = (new MetricsRegistry()).register("Store", ChunkStore::getStore, Store.METRICS_REGISTRY).register("ChunkLoader", MetricProvider.maybe(ChunkStore::getLoader)).register("ChunkSaver", MetricProvider.maybe(ChunkStore::getSaver)).register("WorldGen", MetricProvider.maybe(ChunkStore::getGenerator)).register("TotalGeneratedChunkCount", (chunkComponentStore) -> (long)chunkComponentStore.totalGeneratedChunksCount.get(), Codec.LONG).register("TotalLoadedChunkCount", (chunkComponentStore) -> (long)chunkComponentStore.totalLoadedChunksCount.get(), Codec.LONG);
      MAX_FAILURE_BACKOFF_NANOS = TimeUnit.SECONDS.toNanos(10L);
      FAILURE_BACKOFF_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
      REGISTRY = new ComponentRegistry<ChunkStore>();
      HOLDER_CODEC_KEY = new CodecKey<Holder<ChunkStore>>("ChunkHolder");
      CodecStore var10000 = CodecStore.STATIC;
      CodecKey var10001 = HOLDER_CODEC_KEY;
      ComponentRegistry var10002 = REGISTRY;
      Objects.requireNonNull(var10002);
      var10000.putCodecSupplier(var10001, var10002::getEntityCodec);
      LOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(LoadPacketDataQuerySystem.class);
      LOAD_FUTURE_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(LoadFuturePacketDataQuerySystem.class);
      UNLOAD_PACKETS_DATA_QUERY_SYSTEM_TYPE = REGISTRY.registerSystemType(UnloadPacketDataQuerySystem.class);
      UNLOAD_RESOURCE = REGISTRY.registerResource(ChunkUnloadingSystem.Data.class, ChunkUnloadingSystem.Data::new);
      SAVE_RESOURCE = REGISTRY.registerResource(ChunkSavingSystems.Data.class, ChunkSavingSystems.Data::new);
      INIT_GROUP = REGISTRY.registerSystemGroup();
      REGISTRY.registerSystem(new ChunkLoaderSaverSetupSystem());
      REGISTRY.registerSystem(new ChunkUnloadingSystem());
      REGISTRY.registerSystem(new ChunkSavingSystems.WorldRemoved());
      REGISTRY.registerSystem(new ChunkSavingSystems.Ticking());
   }

   private static class ChunkLoadState {
      private final StampedLock lock = new StampedLock();
      private int flags = 0;
      @Nullable
      private CompletableFuture<Ref<ChunkStore>> future;
      @Nullable
      private Ref<ChunkStore> reference;
      @Nullable
      private Throwable throwable;
      private long failedWhen;
      private int failedCounter;

      private void fail(Throwable throwable) {
         long stamp = this.lock.writeLock();

         try {
            this.flags = 0;
            this.future = null;
            this.throwable = throwable;
            this.failedWhen = System.nanoTime();
            ++this.failedCounter;
         } finally {
            this.lock.unlockWrite(stamp);
         }

      }
   }

   public abstract static class LoadPacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, ToClientPacket> {
   }

   public abstract static class LoadFuturePacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, CompletableFuture<ToClientPacket>> {
   }

   public abstract static class UnloadPacketDataQuerySystem extends EntityDataSystem<ChunkStore, PlayerRef, ToClientPacket> {
   }

   private static class ChunkStorage implements Resource<ChunkStore> {
      public static final BuilderCodec<ChunkStorage> CODEC;
      @Nullable
      private IChunkStorageProvider<?> currentProvider;

      public ChunkStorage(@Nullable IChunkStorageProvider<?> currentProvider) {
         this.currentProvider = currentProvider;
      }

      public ChunkStorage() {
      }

      @Nullable
      public Resource<ChunkStore> clone() {
         return new ChunkStorage(this.currentProvider);
      }

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(ChunkStorage.class, ChunkStorage::new).append(new KeyedCodec("CurrentProvider", IChunkStorageProvider.CODEC), (o, i) -> o.currentProvider = i, (o) -> o.currentProvider).add()).build();
      }
   }

   public static class ChunkLoaderSaverSetupSystem extends StoreSystem<ChunkStore> {
      private final ResourceType<ChunkStore, ChunkStorage> chunkStorageResourceType;

      public ChunkLoaderSaverSetupSystem() {
         this.chunkStorageResourceType = this.registerResource(ChunkStorage.class, "ChunkStorage", ChunkStore.ChunkStorage.CODEC);
      }

      @Nullable
      public SystemGroup<ChunkStore> getGroup() {
         return ChunkStore.INIT_GROUP;
      }

      public void onSystemAddedToStore(@Nonnull Store<ChunkStore> store) {
         ChunkStore data = store.getExternalData();
         World world = data.getWorld();
         IChunkStorageProvider<?> chunkStorageProvider = world.getWorldConfig().getChunkStorageProvider();
         ChunkStorage chunkStorage = (ChunkStorage)store.getResource(this.chunkStorageResourceType);

         try {
            if (chunkStorage.currentProvider != null && !chunkStorage.currentProvider.isSame(chunkStorageProvider)) {
               data.storageData = chunkStorageProvider.migrateFrom(store, chunkStorage.currentProvider);
            } else {
               data.storageData = chunkStorageProvider.initialize(store);
            }

            chunkStorage.currentProvider = chunkStorageProvider;
            data.loader = chunkStorageProvider.getLoader(data.storageData, store);
            data.saver = chunkStorageProvider.getSaver(data.storageData, store);
         } catch (IOException e) {
            throw SneakyThrow.sneakyThrow(e);
         }
      }

      public void onSystemRemovedFromStore(@Nonnull Store<ChunkStore> store) {
         ChunkStore data = store.getExternalData();

         try {
            if (data.loader != null) {
               IChunkLoader oldLoader = data.loader;
               data.loader = null;
               oldLoader.close();
            }

            if (data.saver != null) {
               IChunkSaver oldSaver = data.saver;
               data.saver = null;
               oldSaver.close();
            }

            World world = data.getWorld();
            IChunkStorageProvider<?> chunkStorageProvider = world.getWorldConfig().getChunkStorageProvider();
            chunkStorageProvider.close(data.storageData, store);
         } catch (IOException e) {
            ((HytaleLogger.Api)ChunkStore.LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to close storage!");
         }

      }
   }
}
