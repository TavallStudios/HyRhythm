package com.hypixel.hytale.server.core.asset.monitor;

import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class DirectoryHandlerChangeTask implements Runnable {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final long ACCUMULATION_DELAY_MILLIS = 1000L;
   private final AssetMonitor assetMonitor;
   private final Path parent;
   private final AssetMonitorHandler handler;
   @Nonnull
   private final ScheduledFuture<?> task;
   private final AtomicBoolean changed = new AtomicBoolean(true);
   private final Map<Path, PathEvent> paths = new Object2ObjectOpenHashMap();

   public DirectoryHandlerChangeTask(AssetMonitor assetMonitor, Path parent, AssetMonitorHandler handler) {
      this.assetMonitor = assetMonitor;
      this.parent = parent;
      this.handler = handler;
      this.task = AssetMonitor.runTask(this, 1000L);
   }

   public void run() {
      if (!this.changed.getAndSet(false)) {
         this.cancelSchedule();

         try {
            LOGGER.at(Level.FINER).log("run: %s", this.paths);
            ObjectArrayList<Map.Entry<Path, PathEvent>> entries = new ObjectArrayList(this.paths.size());

            for(Map.Entry<Path, PathEvent> entry : this.paths.entrySet()) {
               entries.add(new AbstractMap.SimpleEntry((Path)entry.getKey(), (PathEvent)entry.getValue()));
            }

            this.paths.clear();
            entries.sort(Comparator.comparingLong((value) -> ((PathEvent)value.getValue()).getTimestamp()));
            Set<String> fileNames = new HashSet();
            Map<Path, EventKind> eventPaths = new Object2ObjectOpenHashMap();

            Map.Entry<Path, PathEvent> entry;
            for(ObjectListIterator var4 = entries.iterator(); var4.hasNext(); eventPaths.put((Path)entry.getKey(), ((PathEvent)entry.getValue()).getEventKind())) {
               entry = (Map.Entry)var4.next();
               if (!fileNames.add(((Path)entry.getKey()).getFileName().toString())) {
                  LOGGER.at(Level.FINER).log("run handler.accept(%s)", eventPaths);
                  this.handler.accept(eventPaths);
                  eventPaths = new Object2ObjectOpenHashMap();
                  fileNames.clear();
               }
            }

            if (!eventPaths.isEmpty()) {
               LOGGER.at(Level.FINER).log("run handler.accept(%s)", eventPaths);
               this.handler.accept(eventPaths);
            }
         } catch (Exception e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to run: %s", this);
         }
      }

   }

   public AssetMonitor getAssetMonitor() {
      return this.assetMonitor;
   }

   public Path getParent() {
      return this.parent;
   }

   public AssetMonitorHandler getHandler() {
      return this.handler;
   }

   public void addPath(Path path, PathEvent pathEvent) {
      LOGGER.at(Level.FINEST).log("addPath(%s, %s): %s", path, pathEvent, this);
      this.paths.put(path, pathEvent);
      this.changed.set(true);
   }

   public void removePath(Path path) {
      LOGGER.at(Level.FINEST).log("removePath(%s, %s): %s", path, this);
      this.paths.remove(path);
      if (this.paths.isEmpty()) {
         this.cancelSchedule();
      } else {
         this.changed.set(true);
      }

   }

   public void markChanged() {
      AssetMonitor.LOGGER.at(Level.FINEST).log("markChanged(): %s", this);
      this.changed.set(true);
   }

   public void cancelSchedule() {
      LOGGER.at(Level.FINEST).log("cancelSchedule(): %s", this);
      this.assetMonitor.removeHookChangeTask(this);
      if (this.task != null && !this.task.isDone()) {
         this.task.cancel(false);
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.parent);
      return "DirectoryHandlerChangeTask{parent=" + var10000 + ", handler=" + String.valueOf(this.handler) + ", changed=" + String.valueOf(this.changed) + ", paths=" + String.valueOf(this.paths) + "}";
   }
}
