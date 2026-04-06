package com.hypixel.hytale.component.dependency;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.ISystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DependencyGraph<ECS_TYPE> {
   @Nonnull
   private final ISystem<ECS_TYPE>[] systems;
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, List<Edge<ECS_TYPE>>> beforeSystemEdges = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, List<Edge<ECS_TYPE>>> afterSystemEdges = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<ISystem<ECS_TYPE>, Set<Edge<ECS_TYPE>>> afterSystemUnfulfilledEdges = new Object2ObjectOpenHashMap();
   private Edge<ECS_TYPE>[] edges = DependencyGraph.Edge.<ECS_TYPE>emptyArray();

   public DependencyGraph(@Nonnull ISystem<ECS_TYPE>[] systems) {
      this.systems = systems;

      for(int i = 0; i < systems.length; ++i) {
         ISystem<ECS_TYPE> system = systems[i];
         this.beforeSystemEdges.put(system, new ObjectArrayList());
         this.afterSystemEdges.put(system, new ObjectArrayList());
         this.afterSystemUnfulfilledEdges.put(system, new HashSet());
      }

   }

   @Nonnull
   public ISystem<ECS_TYPE>[] getSystems() {
      return this.systems;
   }

   public void resolveEdges(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      for(ISystem<ECS_TYPE> system : this.systems) {
         for(Dependency<ECS_TYPE> dependency : system.getDependencies()) {
            dependency.resolveGraphEdge(registry, system, this);
         }

         if (system.getGroup() != null) {
            for(Dependency<ECS_TYPE> dependency : system.getGroup().getDependencies()) {
               dependency.resolveGraphEdge(registry, system, this);
            }
         }
      }

      for(ISystem<ECS_TYPE> system : this.systems) {
         if (((List)this.afterSystemEdges.get(system)).isEmpty()) {
            int priority = 0;
            List<Edge<ECS_TYPE>> edges = (List)this.beforeSystemEdges.get(system);

            for(Edge<ECS_TYPE> edge : edges) {
               priority += edge.priority / edges.size();
            }

            this.addEdgeFromRoot(system, priority);
         }
      }

   }

   public void addEdgeFromRoot(@Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
      this.addEdge(new Edge((ISystem)null, afterSystem, priority));
   }

   public void addEdge(@Nonnull ISystem<ECS_TYPE> beforeSystem, @Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
      this.addEdge(new Edge(beforeSystem, afterSystem, priority));
   }

   public void addEdge(@Nonnull Edge<ECS_TYPE> edge) {
      int index = Arrays.binarySearch(this.edges, edge);
      int insertionPoint;
      if (index >= 0) {
         for(insertionPoint = index; insertionPoint < this.edges.length && this.edges[insertionPoint].priority == edge.priority; ++insertionPoint) {
         }
      } else {
         insertionPoint = -(index + 1);
      }

      int oldLength = this.edges.length;
      int newLength = oldLength + 1;
      if (oldLength < newLength) {
         this.edges = (Edge[])Arrays.copyOf(this.edges, newLength);
      }

      System.arraycopy(this.edges, insertionPoint, this.edges, insertionPoint + 1, oldLength - insertionPoint);
      this.edges[insertionPoint] = edge;
      if (edge.beforeSystem != null) {
         ((List)this.beforeSystemEdges.get(edge.beforeSystem)).add(edge);
      }

      ((List)this.afterSystemEdges.get(edge.afterSystem)).add(edge);
      if (!edge.fulfilled) {
         ((Set)this.afterSystemUnfulfilledEdges.get(edge.afterSystem)).add(edge);
      }

   }

   public void sort(@Nonnull ISystem<ECS_TYPE>[] sortedSystems) {
      int index = 0;

      label52:
      while(index < this.systems.length) {
         for(Edge<ECS_TYPE> edge : this.edges) {
            if (!edge.resolved && edge.fulfilled) {
               ISystem<ECS_TYPE> system = edge.afterSystem;
               if (((Set)this.afterSystemUnfulfilledEdges.get(system)).isEmpty() && !this.hasEdgeOfLaterPriority(system, edge.priority)) {
                  sortedSystems[index++] = system;
                  this.resolveEdgesFor(system);
                  this.fulfillEdgesFor(system);
                  continue label52;
               }
            }
         }

         for(Edge<ECS_TYPE> edge : this.edges) {
            if (!edge.resolved && edge.fulfilled) {
               ISystem<ECS_TYPE> system = edge.afterSystem;
               if (((Set)this.afterSystemUnfulfilledEdges.get(system)).isEmpty()) {
                  sortedSystems[index++] = system;
                  this.resolveEdgesFor(system);
                  this.fulfillEdgesFor(system);
                  continue label52;
               }
            }
         }

         throw new IllegalArgumentException("Found a cyclic dependency!" + String.valueOf(this));
      }

   }

   private boolean hasEdgeOfLaterPriority(@Nonnull ISystem<ECS_TYPE> system, int priority) {
      for(Edge<ECS_TYPE> edge : (List)this.afterSystemEdges.get(system)) {
         if (!edge.resolved && edge.priority > priority) {
            return true;
         }
      }

      return false;
   }

   private void resolveEdgesFor(@Nonnull ISystem<ECS_TYPE> system) {
      for(Edge<ECS_TYPE> edge : (List)this.afterSystemEdges.get(system)) {
         edge.resolved = true;
      }

   }

   private void fulfillEdgesFor(@Nonnull ISystem<ECS_TYPE> system) {
      for(Edge<ECS_TYPE> edge : (List)this.beforeSystemEdges.get(system)) {
         edge.fulfilled = true;
         ((Set)this.afterSystemUnfulfilledEdges.get(edge.afterSystem)).remove(edge);
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.systems);
      return "DependencyGraph{systems=" + var10000 + ", edges=" + Arrays.toString(this.edges) + "}";
   }

   private static class Edge<ECS_TYPE> implements Comparable<Edge<ECS_TYPE>> {
      @Nonnull
      private static final Edge<?>[] EMPTY_ARRAY = new Edge[0];
      @Nullable
      private final ISystem<ECS_TYPE> beforeSystem;
      private final ISystem<ECS_TYPE> afterSystem;
      private final int priority;
      private boolean fulfilled;
      private boolean resolved;

      public static <ECS_TYPE> Edge<ECS_TYPE>[] emptyArray() {
         return EMPTY_ARRAY;
      }

      public Edge(@Nullable ISystem<ECS_TYPE> beforeSystem, @Nonnull ISystem<ECS_TYPE> afterSystem, int priority) {
         this.beforeSystem = beforeSystem;
         this.afterSystem = afterSystem;
         this.priority = priority;
         this.fulfilled = beforeSystem == null;
      }

      public int compareTo(@Nonnull Edge<ECS_TYPE> o) {
         return Integer.compare(this.priority, o.priority);
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.beforeSystem);
         return "Edge{beforeSystem=" + var10000 + ", afterSystem=" + String.valueOf(this.afterSystem) + ", priority=" + this.priority + ", fulfilled=" + this.fulfilled + ", resolved=" + this.resolved + "}";
      }
   }
}
