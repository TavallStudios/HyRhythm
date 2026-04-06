package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.ISystem;
import java.util.Set;
import javax.annotation.Nonnull;

public class RootDependency<ECS_TYPE> extends Dependency<ECS_TYPE> {
   private static final RootDependency<?> FIRST;
   private static final RootDependency<?> LAST;
   private static final Set<Dependency<?>> FIRST_SET;
   private static final Set<Dependency<?>> LAST_SET;

   public static <ECS_TYPE> RootDependency<ECS_TYPE> first() {
      return FIRST;
   }

   public static <ECS_TYPE> RootDependency<ECS_TYPE> last() {
      return LAST;
   }

   public static <ECS_TYPE> Set<Dependency<ECS_TYPE>> firstSet() {
      return FIRST_SET;
   }

   public static <ECS_TYPE> Set<Dependency<ECS_TYPE>> lastSet() {
      return LAST_SET;
   }

   public RootDependency(int priority) {
      super(Order.AFTER, priority);
   }

   public RootDependency(@Nonnull OrderPriority priority) {
      super(Order.AFTER, priority);
   }

   public void validate(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
   }

   public void resolveGraphEdge(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull ISystem<ECS_TYPE> thisSystem, @Nonnull DependencyGraph<ECS_TYPE> graph) {
      if (this.order == Order.BEFORE) {
         throw new UnsupportedOperationException("RootDependency can't have Order.BEFORE!");
      } else {
         graph.addEdgeFromRoot(thisSystem, this.priority);
      }
   }

   @Nonnull
   public String toString() {
      return "SystemDependency{} " + super.toString();
   }

   static {
      FIRST = new RootDependency(OrderPriority.CLOSEST);
      LAST = new RootDependency(OrderPriority.FURTHEST);
      FIRST_SET = Set.of(FIRST);
      LAST_SET = Set.of(LAST);
   }
}
