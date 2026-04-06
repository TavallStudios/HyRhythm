package com.hypixel.hytale.component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceType<ECS_TYPE, T extends Resource<ECS_TYPE>> implements Comparable<ResourceType<ECS_TYPE, ?>> {
   @Nonnull
   public static final ResourceType[] EMPTY_ARRAY = new ResourceType[0];
   private ComponentRegistry<ECS_TYPE> registry;
   private Class<? super T> tClass;
   private int index;
   private boolean invalid = true;

   void init(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Class<? super T> tClass, int index) {
      this.registry = registry;
      this.tClass = tClass;
      this.index = index;
      this.invalid = false;
   }

   @Nonnull
   public ComponentRegistry<ECS_TYPE> getRegistry() {
      return this.registry;
   }

   @Nonnull
   public Class<? super T> getTypeClass() {
      return this.tClass;
   }

   public int getIndex() {
      return this.index;
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!this.registry.equals(registry)) {
         throw new IllegalArgumentException("ResourceType is for a different registry! " + String.valueOf(this));
      }
   }

   public void validate() {
      if (this.invalid) {
         throw new IllegalStateException("ResourceType is invalid!");
      }
   }

   void invalidate() {
      this.invalid = true;
   }

   boolean isValid() {
      return !this.invalid;
   }

   public int compareTo(@Nonnull ResourceType<ECS_TYPE, ?> o) {
      return Integer.compare(this.index, o.getIndex());
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ResourceType<?, ?> that = (ResourceType)o;
         return this.index != that.index ? false : this.registry.equals(that.registry);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.registry.hashCode();
      result = 31 * result + this.index;
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.registry.getClass());
      return "ResourceType{registry=" + var10000 + "@" + this.registry.hashCode() + ", typeClass=" + String.valueOf(this.tClass) + ", index=" + this.index + "}";
   }
}
