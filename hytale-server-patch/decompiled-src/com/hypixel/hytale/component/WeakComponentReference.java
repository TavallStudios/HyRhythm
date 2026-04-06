package com.hypixel.hytale.component;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeakComponentReference<ECS_TYPE, T extends Component<ECS_TYPE>> {
   @Nonnull
   private final Store<ECS_TYPE> store;
   @Nonnull
   private final ComponentType<ECS_TYPE, T> type;
   @Nullable
   private Ref<ECS_TYPE> ref;
   private WeakReference<T> reference;

   WeakComponentReference(@Nonnull Store<ECS_TYPE> store, @Nonnull ComponentType<ECS_TYPE, T> type, @Nonnull Ref<ECS_TYPE> ref, @Nonnull T data) {
      this.store = store;
      this.type = type;
      this.ref = ref;
      this.reference = new WeakReference(data);
   }

   @Nullable
   public T get() {
      T data = (T)(this.reference.get());
      if (data != null) {
         return data;
      } else if (this.ref == null) {
         return null;
      } else {
         data = this.store.getComponent(this.ref, this.type);
         if (data != null) {
            this.reference = new WeakReference(data);
         }

         return data;
      }
   }

   @Nonnull
   public Store<ECS_TYPE> getStore() {
      return this.store;
   }

   @Nonnull
   public ComponentType<ECS_TYPE, T> getType() {
      return this.type;
   }

   @Nullable
   public Ref<ECS_TYPE> getEntityReference() {
      return this.ref;
   }

   void invalidate() {
      this.ref = null;
      this.reference.clear();
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         WeakComponentReference<?, ?> that = (WeakComponentReference)o;
         if (!this.store.equals(that.store)) {
            return false;
         } else {
            return !this.type.equals(that.type) ? false : Objects.equals(this.ref, that.ref);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.store.hashCode();
      result = 31 * result + this.type.hashCode();
      result = 31 * result + (this.ref != null ? this.ref.hashCode() : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.store);
      return "WeakComponentReference{store=" + var10000 + ", type=" + String.valueOf(this.type) + ", entity=" + String.valueOf(this.ref) + ", reference=" + String.valueOf(this.reference) + "}";
   }
}
