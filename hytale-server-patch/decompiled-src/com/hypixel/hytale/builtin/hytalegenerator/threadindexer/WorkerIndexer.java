package com.hypixel.hytale.builtin.hytalegenerator.threadindexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class WorkerIndexer {
   private final int workerCount;
   @Nonnull
   private final List<Id> ids;

   public WorkerIndexer(int workerCount) {
      if (workerCount <= 0) {
         throw new IllegalArgumentException("workerCount must be > 0");
      } else {
         this.workerCount = workerCount;
         List<Id> tempIds = new ArrayList(workerCount);

         for(int i = 0; i < workerCount; ++i) {
            tempIds.add(new Id(i));
         }

         this.ids = Collections.unmodifiableList(tempIds);
      }
   }

   public int getWorkerCount() {
      return this.workerCount;
   }

   @Nonnull
   public List<Id> getWorkedIds() {
      return this.ids;
   }

   @Nonnull
   public Session createSession() {
      return new Session();
   }

   public class Session {
      private int index = 0;

      public Id next() {
         if (this.index >= WorkerIndexer.this.workerCount) {
            throw new IllegalStateException("worker count exceeded");
         } else {
            return (Id)WorkerIndexer.this.ids.get(this.index++);
         }
      }

      public boolean hasNext() {
         return this.index < WorkerIndexer.this.workerCount;
      }
   }

   public static class Id {
      public static final int UNKNOWN_THREAD_ID = -1;
      public static final int MAIN_THREAD_ID = 0;
      @Nonnull
      public static final Id UNKNOWN = new Id(-1);
      @Nonnull
      public static final Id MAIN = new Id(0);
      public final int id;

      private Id(int id) {
         this.id = id;
      }

      @Nonnull
      public String toString() {
         return String.valueOf(this.id);
      }
   }

   public static class Data<T> {
      private T[] data;
      private Supplier<T> initialize;

      public Data(int size, @Nonnull Supplier<T> initializer) {
         this.data = (T[])(new Object[size]);
         this.initialize = initializer;
      }

      public Data(@Nonnull Data<?> other, @Nonnull Supplier<T> initializer) {
         this(other.data.length, initializer);
      }

      public Data(@Nonnull WorkerIndexer workerIndexer, @Nonnull Supplier<T> initializer) {
         this(workerIndexer.getWorkerCount(), initializer);
      }

      public boolean isValid(@Nonnull Id id) {
         return id != null && id.id < this.data.length && id.id >= 0;
      }

      @Nonnull
      public T get(@Nonnull Id id) {
         if (!this.isValid(id)) {
            throw new IllegalArgumentException("Invalid thread id " + String.valueOf(id));
         } else {
            if (this.data[id.id] == null) {
               this.data[id.id] = this.initialize.get();

               assert this.data[id.id] != null;
            }

            return (T)this.data[id.id];
         }
      }

      public void set(@Nonnull Id id, T value) {
         if (!this.isValid(id)) {
            throw new IllegalArgumentException("Invalid thread id " + String.valueOf(id));
         } else {
            this.data[id.id] = value;
         }
      }

      public void forEach(@Nonnull BiConsumer<Id, T> consumer) {
         for(int i = 0; i < this.data.length; ++i) {
            Id id = new Id(i);
            consumer.accept(id, this.data[i]);
         }

      }
   }
}
