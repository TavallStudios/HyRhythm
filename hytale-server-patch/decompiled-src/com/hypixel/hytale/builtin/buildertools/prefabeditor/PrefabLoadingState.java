package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.server.core.Message;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabLoadingState {
   @Nonnull
   private Phase currentPhase;
   private int totalPrefabs;
   private int loadedPrefabs;
   private int pastedPrefabs;
   @Nullable
   private Path currentPrefabPath;
   @Nonnull
   private final List<LoadingError> errors;
   private long startTimeNanos;
   private long lastUpdateTimeNanos;
   private long lastNotifyTimeNanos;

   public PrefabLoadingState() {
      this.currentPhase = PrefabLoadingState.Phase.INITIALIZING;
      this.errors = new ObjectArrayList();
      this.startTimeNanos = System.nanoTime();
      this.lastUpdateTimeNanos = this.startTimeNanos;
   }

   public void setTotalPrefabs(int totalPrefabs) {
      this.totalPrefabs = totalPrefabs;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void setPhase(@Nonnull Phase phase) {
      this.currentPhase = phase;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void onPrefabLoaded(@Nullable Path path) {
      ++this.loadedPrefabs;
      this.currentPrefabPath = path;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void onPrefabPasted(@Nullable Path path) {
      ++this.pastedPrefabs;
      this.currentPrefabPath = path;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void addError(@Nonnull LoadingError error) {
      this.errors.add(error);
      this.currentPhase = PrefabLoadingState.Phase.ERROR;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public void addError(@Nonnull String translationKey) {
      this.addError(new LoadingError(translationKey));
   }

   public void addError(@Nonnull String translationKey, @Nullable String details) {
      this.addError(new LoadingError(translationKey, details));
   }

   @Nonnull
   public Phase getCurrentPhase() {
      return this.currentPhase;
   }

   public int getTotalPrefabs() {
      return this.totalPrefabs;
   }

   public int getLoadedPrefabs() {
      return this.loadedPrefabs;
   }

   public int getPastedPrefabs() {
      return this.pastedPrefabs;
   }

   @Nullable
   public Path getCurrentPrefabPath() {
      return this.currentPrefabPath;
   }

   @Nonnull
   public List<LoadingError> getErrors() {
      return this.errors;
   }

   public boolean hasErrors() {
      return !this.errors.isEmpty();
   }

   public boolean isShuttingDown() {
      return this.currentPhase == PrefabLoadingState.Phase.CANCELLING || this.currentPhase == PrefabLoadingState.Phase.SHUTTING_DOWN_WORLD || this.currentPhase == PrefabLoadingState.Phase.DELETING_WORLD;
   }

   public boolean isShutdownComplete() {
      return this.currentPhase == PrefabLoadingState.Phase.SHUTDOWN_COMPLETE;
   }

   public float getProgressPercentage() {
      if (this.totalPrefabs == 0) {
         float var1;
         switch (this.currentPhase.ordinal()) {
            case 0 -> var1 = 0.0F;
            case 1 -> var1 = 0.1F;
            case 2 -> var1 = 0.2F;
            case 3 -> var1 = 0.5F;
            case 4 -> var1 = 0.99F;
            case 5 -> var1 = 1.0F;
            case 6 -> var1 = 0.0F;
            case 7 -> var1 = 0.1F;
            case 8 -> var1 = 0.4F;
            case 9 -> var1 = 0.8F;
            case 10 -> var1 = 1.0F;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var1;
      } else {
         float var10000;
         switch (this.currentPhase.ordinal()) {
            case 0 -> var10000 = 0.0F;
            case 1 -> var10000 = 0.02F;
            case 2 -> var10000 = 0.02F + 0.08F * (float)this.loadedPrefabs / (float)this.totalPrefabs;
            case 3 -> var10000 = 0.1F + 0.89F * (float)this.pastedPrefabs / (float)this.totalPrefabs;
            case 4 -> var10000 = 0.99F;
            case 5 -> var10000 = 1.0F;
            case 6 -> var10000 = 0.0F;
            case 7 -> var10000 = 0.1F;
            case 8 -> var10000 = 0.4F;
            case 9 -> var10000 = 0.8F;
            case 10 -> var10000 = 1.0F;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }
   }

   public long getElapsedTimeMillis() {
      return (System.nanoTime() - this.startTimeNanos) / 1000000L;
   }

   public long getLastNotifyTimeNanos() {
      return this.lastNotifyTimeNanos;
   }

   public void setLastNotifyTimeNanos(long nanos) {
      this.lastNotifyTimeNanos = nanos;
   }

   @Nonnull
   public Message getStatusMessage() {
      if (this.hasErrors()) {
         return ((LoadingError)this.errors.getLast()).toMessage();
      } else {
         Message message = Message.translation(this.currentPhase.getTranslationKey());
         if (this.currentPhase == PrefabLoadingState.Phase.LOADING_PREFABS && this.totalPrefabs > 0) {
            message = message.param("current", this.loadedPrefabs).param("total", this.totalPrefabs);
         } else if (this.currentPhase == PrefabLoadingState.Phase.PASTING_PREFABS && this.totalPrefabs > 0) {
            message = message.param("current", this.pastedPrefabs).param("total", this.totalPrefabs);
         }

         if (this.currentPrefabPath != null) {
            message = message.param("path", this.currentPrefabPath.getFileName().toString());
         }

         return message;
      }
   }

   public void markComplete() {
      this.currentPhase = PrefabLoadingState.Phase.COMPLETE;
      this.lastUpdateTimeNanos = System.nanoTime();
   }

   public static enum Phase {
      INITIALIZING("server.commands.editprefab.loading.phase.initializing"),
      CREATING_WORLD("server.commands.editprefab.loading.phase.creatingWorld"),
      LOADING_PREFABS("server.commands.editprefab.loading.phase.loadingPrefabs"),
      PASTING_PREFABS("server.commands.editprefab.loading.phase.pastingPrefabs"),
      FINALIZING("server.commands.editprefab.loading.phase.finalizing"),
      COMPLETE("server.commands.editprefab.loading.phase.complete"),
      ERROR("server.commands.editprefab.loading.phase.error"),
      CANCELLING("server.commands.editprefab.loading.phase.cancelling"),
      SHUTTING_DOWN_WORLD("server.commands.editprefab.loading.phase.shuttingDownWorld"),
      DELETING_WORLD("server.commands.editprefab.loading.phase.deletingWorld"),
      SHUTDOWN_COMPLETE("server.commands.editprefab.loading.phase.shutdownComplete");

      private final String translationKey;

      private Phase(String translationKey) {
         this.translationKey = translationKey;
      }

      @Nonnull
      public String getTranslationKey() {
         return this.translationKey;
      }
   }

   public static record LoadingError(@Nonnull String translationKey, @Nullable String details) {
      public LoadingError(@Nonnull String translationKey) {
         this(translationKey, (String)null);
      }

      @Nonnull
      public Message toMessage() {
         Message message = Message.translation(this.translationKey);
         if (this.details != null) {
            message = message.param("details", this.details);
         }

         return message;
      }
   }
}
