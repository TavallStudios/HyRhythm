package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemGroupDependency<ECS_TYPE> extends Dependency<ECS_TYPE> {
   @Nonnull
   private final SystemGroup<ECS_TYPE> group;

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group) {
      this(order, group, OrderPriority.NORMAL);
   }

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group, int priority) {
      super(order, priority);
      this.group = group;
   }

   public SystemGroupDependency(@Nonnull Order order, @Nonnull SystemGroup<ECS_TYPE> group, @Nonnull OrderPriority priority) {
      super(order, priority);
      this.group = group;
   }

   @Nonnull
   public SystemGroup<ECS_TYPE> getGroup() {
      return this.group;
   }

   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!registry.hasSystemGroup(this.group)) {
         throw new IllegalArgumentException("System dependency isn't registered: " + String.valueOf(this.group));
      }
   }

   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      switch (this.order) {
         case BEFORE:
            for(ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.group.equals(system.getGroup())) {
                  graph.addEdge(thisSystem, system, -this.priority);
               }
            }
            break;
         case AFTER:
            for(ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.group.equals(system.getGroup())) {
                  graph.addEdge(system, thisSystem, this.priority);
               }
            }
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.group);
      return "SystemGroupDependency{group=" + var10000 + "} " + super.toString();
   }
}
