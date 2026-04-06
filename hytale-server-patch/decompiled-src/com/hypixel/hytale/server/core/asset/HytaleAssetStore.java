package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetLoadResult;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.event.AssetStoreMonitorEvent;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitorHandler;
import com.hypixel.hytale.server.core.asset.monitor.EventKind;
import com.hypixel.hytale.server.core.asset.packet.AssetPacketGenerator;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HytaleAssetStore<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> extends AssetStore<K, T, M> {
   public static final Queue<Consumer<ToClientPacket>> SETUP_PACKET_CONSUMERS;
   protected final AssetPacketGenerator<K, T, M> packetGenerator;
   protected final Function<K, ItemWithAllMetadata> notificationItemFunction;
   @Nullable
   protected SoftReference<ToClientPacket[]> cachedInitPackets;

   public HytaleAssetStore(@Nonnull Builder<K, T, M> builder) {
      super(builder);
      this.packetGenerator = builder.packetGenerator;
      this.notificationItemFunction = builder.notificationItemFunction;
   }

   public AssetPacketGenerator<K, T, M> getPacketGenerator() {
      return this.packetGenerator;
   }

   public Function<K, ItemWithAllMetadata> getNotificationItemFunction() {
      return this.notificationItemFunction;
   }

   @Nonnull
   protected EventBus getEventBus() {
      return HytaleServer.get().getEventBus();
   }

   public void addFileMonitor(@Nonnull String packKey, @Nonnull Path assetsPath) {
      AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
      if (assetMonitor != null) {
         assetMonitor.monitorDirectoryFiles(assetsPath, new AssetStoreMonitorHandler(packKey));
      }
   }

   public void removeFileMonitor(@Nonnull Path path) {
      AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
      if (assetMonitor != null) {
         assetMonitor.removeMonitorDirectoryFiles(path, this);
      }

   }

   protected void handleRemoveOrUpdate(@Nullable Set<K> toBeRemoved, @Nullable Map<K, T> toBeUpdated, @Nonnull AssetUpdateQuery query) {
      if (this.packetGenerator != null) {
         this.cachedInitPackets = null;
         Universe universe = Universe.get();
         if (universe.getPlayerCount() != 0 || !SETUP_PACKET_CONSUMERS.isEmpty()) {
            if (toBeRemoved != null && !toBeRemoved.isEmpty()) {
               ToClientPacket packet = this.packetGenerator.generateRemovePacket(this.assetMap, toBeRemoved, query);
               universe.broadcastPacketNoCache(packet);

               for(Consumer<ToClientPacket> c : SETUP_PACKET_CONSUMERS) {
                  c.accept(packet);
               }
            }

            if (toBeUpdated != null && !toBeUpdated.isEmpty()) {
               ToClientPacket packet = this.packetGenerator.generateUpdatePacket(this.assetMap, toBeUpdated, query);
               universe.broadcastPacketNoCache(packet);

               for(Consumer<ToClientPacket> c : SETUP_PACKET_CONSUMERS) {
                  c.accept(packet);
               }
            }

         }
      }
   }

   public void sendAssets(@Nonnull Consumer<ToClientPacket[]> packetConsumer) {
      if (this.packetGenerator != null) {
         ToClientPacket[] packets = this.cachedInitPackets == null ? null : (ToClientPacket[])this.cachedInitPackets.get();
         if (packets != null) {
            packetConsumer.accept(packets);
         } else {
            Map<K, T> map = this.assetMap.getAssetMap();
            ToClientPacket packet = this.packetGenerator.generateInitPacket(this.assetMap, map);
            this.cachedInitPackets = new SoftReference(packets = new ToClientPacket[]{packet});
            packetConsumer.accept(packets);
         }
      }
   }

   protected void sendReloadedNotification(@Nonnull AssetLoadResult<K, T> result) {
      this.sendNotificationKeys(Message.translation("server.general.assetstore.reloadAssets").param("class", this.tClass.getSimpleName()).color("#A7AfA7"), "Icons/AssetNotifications/AssetReloaded.png", result.getLoadedAssets().keySet());
      this.sendNotificationKeys(Message.translation("server.general.assetstore.loadFailed").param("class", this.tClass.getSimpleName()), (String)null, result.getFailedToLoadKeys());
      this.sendNotificationPaths(Message.translation("server.general.assetstore.loadFailed").param("class", this.tClass.getSimpleName()), result.getFailedToLoadPaths());
   }

   protected void sendRemovedNotification(@Nonnull Set<K> removedKeys) {
      this.sendNotificationKeys(Message.translation("server.general.assetstore.removedAssets").param("class", this.tClass.getSimpleName()).color("#FF3874"), "Icons/AssetNotifications/Trash.png", removedKeys);
   }

   protected void sendNotificationKeys(Message primaryMessage, @Nullable String icon, @Nonnull Set<K> keys) {
      if (!keys.isEmpty()) {
         if (this.notificationItemFunction != null && keys.size() < 5) {
            for(K key : keys) {
               ItemWithAllMetadata item = (ItemWithAllMetadata)this.notificationItemFunction.apply(key);
               if (item != null) {
                  NotificationUtil.sendNotificationToUniverse(primaryMessage, Message.raw(key.toString()), item, NotificationStyle.Default);
               } else {
                  NotificationUtil.sendNotificationToUniverse(primaryMessage, Message.raw(key.toString()), icon, (ItemWithAllMetadata)null, NotificationStyle.Default);
               }
            }

         } else {
            Message secondaryMessage = Message.translation("server.general.assetstore.removedAssetsSecondaryGeneric").param("count", keys.size());
            NotificationUtil.sendNotificationToUniverse(primaryMessage, secondaryMessage, icon, NotificationStyle.Default);
         }
      }
   }

   protected void sendNotificationPaths(Message primaryMessage, @Nonnull Set<Path> paths) {
      if (!paths.isEmpty()) {
         NotificationUtil.sendNotificationToUniverse(primaryMessage, Message.raw(paths.toString()), NotificationStyle.Warning);
      }
   }

   @Nonnull
   public static <T extends JsonAssetWithMap<String, M>, M extends AssetMap<String, T>> Builder<String, T, M> builder(Class<T> tClass, M assetMap) {
      return new Builder<String, T, M>(String.class, tClass, assetMap);
   }

   @Nonnull
   public static <K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> Builder<K, T, M> builder(Class<K> kClass, Class<T> tClass, M assetMap) {
      return new Builder<K, T, M>(kClass, tClass, assetMap);
   }

   static {
      AssetStore.DISABLE_ASSET_COMPARE = Options.getOptionSet().has(Options.DISABLE_ASSET_COMPARE);
      SETUP_PACKET_CONSUMERS = new ConcurrentLinkedQueue();
   }

   public static class Builder<K, T extends JsonAssetWithMap<K, M>, M extends AssetMap<K, T>> extends AssetStore.Builder<K, T, M, Builder<K, T, M>> {
      protected AssetPacketGenerator<K, T, M> packetGenerator;
      protected Function<K, ItemWithAllMetadata> notificationItemFunction;

      public Builder(Class<K> kClass, Class<T> tClass, M assetMap) {
         super(kClass, tClass, assetMap);
      }

      @Nonnull
      public Builder<K, T, M> setPacketGenerator(AssetPacketGenerator<K, T, M> packetGenerator) {
         this.packetGenerator = (AssetPacketGenerator)Objects.requireNonNull(packetGenerator, "packetGenerator can't be null!");
         return this;
      }

      @Nonnull
      public Builder<K, T, M> setNotificationItemFunction(Function<K, ItemWithAllMetadata> notificationItemFunction) {
         this.notificationItemFunction = (Function)Objects.requireNonNull(notificationItemFunction, "notificationItemFunction can't be null!");
         return this;
      }

      @Nonnull
      public HytaleAssetStore<K, T, M> build() {
         return new HytaleAssetStore<K, T, M>(this);
      }
   }

   private class AssetStoreMonitorHandler implements AssetMonitorHandler {
      private final String packKey;

      public AssetStoreMonitorHandler(String packKey) {
         this.packKey = packKey;
      }

      public Object getKey() {
         return HytaleAssetStore.this;
      }

      public boolean test(Path path, EventKind eventKind) {
         return !Files.isRegularFile(path, new LinkOption[0]) || path.getFileName().toString().endsWith(HytaleAssetStore.this.extension);
      }

      public void accept(Map<Path, EventKind> map) {
         ObjectArrayList<Path> createdOrModifiedFilesToLoad = new ObjectArrayList();
         ObjectArrayList<Path> removedFilesToUnload = new ObjectArrayList();
         ObjectArrayList<Path> createdOrModifiedDirectories = new ObjectArrayList();
         ObjectArrayList<Path> removedFilesAndDirectories = new ObjectArrayList();

         for(Map.Entry<Path, EventKind> entry : map.entrySet()) {
            Path path = (Path)entry.getKey();
            switch ((EventKind)entry.getValue()) {
               case ENTRY_CREATE:
                  if (Files.isDirectory(path, new LinkOption[0])) {
                     HytaleAssetStore.this.logger.at(Level.FINEST).log("Directory Created: %s", path);

                     try {
                        Stream<Path> stream = Files.walk(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY);

                        try {
                           stream.forEach((child) -> {
                              if (Files.isDirectory(child, new LinkOption[0])) {
                                 createdOrModifiedDirectories.add(path);
                              }

                              if (Files.isRegularFile(child, new LinkOption[0]) && child.getFileName().toString().endsWith(HytaleAssetStore.this.extension)) {
                                 createdOrModifiedFilesToLoad.add(child);
                              }

                           });
                        } catch (Throwable var13) {
                           if (stream != null) {
                              try {
                                 stream.close();
                              } catch (Throwable var12) {
                                 var13.addSuppressed(var12);
                              }
                           }

                           throw var13;
                        }

                        if (stream != null) {
                           stream.close();
                        }
                     } catch (IOException e) {
                        ((HytaleLogger.Api)HytaleAssetStore.this.logger.at(Level.SEVERE).withCause(e)).log("Failed to reload assets in directory: %s", path);
                     }
                  } else {
                     HytaleAssetStore.this.logger.at(Level.FINEST).log("File Created: %s", path);
                     createdOrModifiedFilesToLoad.add(path);
                     createdOrModifiedFilesToLoad.add(path);
                  }
                  break;
               case ENTRY_DELETE:
                  removedFilesAndDirectories.add(path);

                  for(Path value : HytaleAssetStore.this.getAssetMap().getPathMap(this.packKey).values()) {
                     if (PathUtil.isChildOf(path, value)) {
                        HytaleAssetStore.this.logger.at(Level.FINEST).log("Deleted: %s", value);
                        removedFilesToUnload.add(value);
                     }
                  }
                  break;
               case ENTRY_MODIFY:
                  if (Files.isDirectory(path, new LinkOption[0])) {
                     HytaleAssetStore.this.logger.at(Level.FINEST).log("Directory Modified: %s", path);
                     createdOrModifiedDirectories.add(path);
                  } else {
                     HytaleAssetStore.this.logger.at(Level.FINEST).log("File Modified: %s", path);
                     createdOrModifiedFilesToLoad.add(path);
                  }
                  break;
               default:
                  throw new IllegalArgumentException("Unknown eventKind " + String.valueOf(entry.getValue()));
            }
         }

         if (!removedFilesAndDirectories.isEmpty() || !createdOrModifiedFilesToLoad.isEmpty() || !createdOrModifiedDirectories.isEmpty()) {
            IEventDispatcher<AssetStoreMonitorEvent, AssetStoreMonitorEvent> dispatchFor = HytaleAssetStore.this.getEventBus().dispatchFor(AssetStoreMonitorEvent.class);
            if (dispatchFor.hasListener()) {
               dispatchFor.dispatch(new AssetStoreMonitorEvent(this.packKey, HytaleAssetStore.this, createdOrModifiedFilesToLoad, removedFilesToUnload, createdOrModifiedDirectories, removedFilesAndDirectories));
            }
         }

         if (!createdOrModifiedFilesToLoad.isEmpty()) {
            HytaleAssetStore.this.logger.at(Level.INFO).log("Reloading assets: %s", createdOrModifiedFilesToLoad);
            long start = System.nanoTime();
            AssetLoadResult<K, T> result = HytaleAssetStore.this.loadAssetsFromPaths(this.packKey, createdOrModifiedFilesToLoad, AssetUpdateQuery.DEFAULT, true);
            HytaleAssetStore.this.logger.at(Level.INFO).log("Took %s to reload assets: %s", FormatUtil.nanosToString(System.nanoTime() - start), createdOrModifiedFilesToLoad);
            HytaleAssetStore.this.sendReloadedNotification(result);
         }

         if (!removedFilesToUnload.isEmpty()) {
            HytaleAssetStore.this.logger.at(Level.INFO).log("Removing deleted assets: %s", removedFilesToUnload);
            Set<K> removedKeys = HytaleAssetStore.this.removeAssetWithPaths(this.packKey, removedFilesToUnload);
            HytaleAssetStore.this.sendRemovedNotification(removedKeys);
         }

      }
   }
}
