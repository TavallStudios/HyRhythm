package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public class SystemTypeDependency<ECS_TYPE, T extends ISystem<ECS_TYPE>> extends Dependency<ECS_TYPE> {
   @Nonnull
   private final SystemType<ECS_TYPE, T> systemType;

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType) {
      this(order, systemType, OrderPriority.NORMAL);
   }

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType, int priority) {
      super(order, priority);
      this.systemType = systemType;
   }

   public SystemTypeDependency(@Nonnull Order order, @Nonnull SystemType<ECS_TYPE, T> systemType, @Nonnull OrderPriority priority) {
      super(order, priority);
      this.systemType = systemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, T> getSystemType() {
      return this.systemType;
   }

   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!registry.hasSystemType(this.systemType)) {
         throw new IllegalArgumentException("SystemType dependency isn't registered: " + String.valueOf(this.systemType));
      }
   }

   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      switch (this.order) {
         case BEFORE:
            for(ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.systemType.isType(system)) {
                  graph.addEdge(thisSystem, system, -this.priority);
               }
            }
            break;
         case AFTER:
            for(ISystem<ECS_TYPE> system : graph.getSystems()) {
               if (this.systemType.isType(system)) {
                  graph.addEdge(system, thisSystem, this.priority);
               }
            }
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.systemType);
      return "SystemTypeDependency{systemType=" + var10000 + "} " + super.toString();
   }
}
