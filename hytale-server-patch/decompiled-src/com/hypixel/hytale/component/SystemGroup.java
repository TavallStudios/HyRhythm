package com.hypixel.hytale.component;

import com.hypixel.hytale.component.dependency.Dependency;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SystemGroup<ECS_TYPE> implements Comparable<SystemGroup<ECS_TYPE>> {
   @Nonnull
   private final ComponentRegistry<ECS_TYPE> registry;
   private final int index;
   @Nonnull
   private final Set<Dependency<ECS_TYPE>> dependencies;
   private boolean invalidated;

   SystemGroup(@Nonnull ComponentRegistry<ECS_TYPE> registry, int index, @Nonnull Set<Dependency<ECS_TYPE>> dependencies) {
      this.registry = registry;
      this.index = index;
      this.dependencies = dependencies;
   }

   @Nonnull
   public ComponentRegistry<ECS_TYPE> getRegistry() {
      return this.registry;
   }

   @Nonnull
   public Set<Dependency<ECS_TYPE>> getDependencies() {
      return this.dependencies;
   }

   public int getIndex() {
      return this.index;
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!this.registry.equals(registry)) {
         throw new IllegalArgumentException("SystemGroup is for a different registry! " + String.valueOf(this));
      }
   }

   public void validate() {
      if (this.invalidated) {
         throw new IllegalStateException("SystemGroup is invalid!");
      }
   }

   void invalidate() {
      this.invalidated = true;
   }

   boolean isValid() {
      return !this.invalidated;
   }

   public int compareTo(@Nonnull SystemGroup<ECS_TYPE> o) {
      return Integer.compare(this.index, o.getIndex());
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SystemGroup<?> that = (SystemGroup)o;
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
      return "SystemGroup{registry=" + var10000 + "@" + this.registry.hashCode() + ", index=" + this.index + "}";
   }
}
