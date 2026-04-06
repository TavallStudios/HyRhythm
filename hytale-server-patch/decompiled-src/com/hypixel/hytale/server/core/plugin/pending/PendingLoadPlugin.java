package com.hypixel.hytale.server.core.plugin.pending;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PendingLoadPlugin {
   @Nonnull
   private final PluginIdentifier identifier;
   @Nonnull
   private final PluginManifest manifest;
   @Nullable
   private final Path path;

   PendingLoadPlugin(@Nullable Path path, @Nonnull PluginManifest manifest) {
      this.path = path;
      this.identifier = new PluginIdentifier(manifest);
      this.manifest = manifest;
   }

   @Nonnull
   public PluginIdentifier getIdentifier() {
      return this.identifier;
   }

   @Nonnull
   public PluginManifest getManifest() {
      return this.manifest;
   }

   @Nullable
   public Path getPath() {
      return this.path;
   }

   public abstract PendingLoadPlugin createSubPendingLoadPlugin(PluginManifest var1);

   @Nonnull
   public abstract PluginBase load() throws Exception;

   @Nonnull
   public List<PendingLoadPlugin> createSubPendingLoadPlugins() {
      List<PluginManifest> subPlugins = this.manifest.getSubPlugins();
      if (subPlugins.isEmpty()) {
         return Collections.emptyList();
      } else {
         ObjectArrayList<PendingLoadPlugin> plugins = new ObjectArrayList(subPlugins.size());

         for(PluginManifest subManifest : subPlugins) {
            subManifest.inherit(this.manifest);
            plugins.add(this.createSubPendingLoadPlugin(subManifest));
         }

         return plugins;
      }
   }

   public boolean dependsOn(PluginIdentifier identifier) {
      return this.manifest.getDependencies().containsKey(identifier) || this.manifest.getOptionalDependencies().containsKey(identifier);
   }

   public abstract boolean isInServerClassPath();

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PendingLoadPlugin that = (PendingLoadPlugin)o;
         if (!this.identifier.equals(that.identifier)) {
            return false;
         } else {
            return !this.manifest.equals(that.manifest) ? false : Objects.equals(this.path, that.path);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.identifier.hashCode();
      result = 31 * result + this.manifest.hashCode();
      result = 31 * result + (this.path != null ? this.path.hashCode() : 0);
      return result;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.identifier);
      return "PendingLoadPlugin{identifier=" + var10000 + ", manifest=" + String.valueOf(this.manifest) + ", path=" + String.valueOf(this.path) + "}";
   }

   @Nonnull
   public static List<PendingLoadPlugin> calculateLoadOrder(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending) {
      HashMap<PluginIdentifier, EntryNode> nodes = new HashMap(pending.size());

      for(Map.Entry<PluginIdentifier, PendingLoadPlugin> entry : pending.entrySet()) {
         nodes.put((PluginIdentifier)entry.getKey(), new EntryNode((PendingLoadPlugin)entry.getValue()));
      }

      HashSet<PluginIdentifier> classpathPlugins = new HashSet();

      for(Map.Entry<PluginIdentifier, PendingLoadPlugin> entry : pending.entrySet()) {
         if (((PendingLoadPlugin)entry.getValue()).isInServerClassPath() && "Hytale".equals(((PluginIdentifier)entry.getKey()).getGroup())) {
            classpathPlugins.add((PluginIdentifier)entry.getKey());
         }
      }

      HashMap<PluginIdentifier, Set<PluginIdentifier>> missingDependencies = new HashMap();

      for(EntryNode node : nodes.values()) {
         PluginManifest manifest = node.plugin.manifest;

         for(PluginIdentifier depId : manifest.getDependencies().keySet()) {
            if (nodes.containsKey(depId)) {
               node.edge.add(depId);
            } else {
               ((Set)missingDependencies.computeIfAbsent(node.plugin.identifier, (k) -> new HashSet())).add(depId);
            }
         }

         for(PluginIdentifier identifier : manifest.getOptionalDependencies().keySet()) {
            EntryNode dep = (EntryNode)nodes.get(identifier);
            if (dep != null) {
               node.edge.add(identifier);
            }
         }

         if (!node.plugin.isInServerClassPath()) {
            node.edge.addAll(classpathPlugins);
         }
      }

      HashMap<PluginIdentifier, Set<PluginIdentifier>> missingLoadBefore = new HashMap();

      for(Map.Entry<PluginIdentifier, PendingLoadPlugin> entry : pending.entrySet()) {
         PluginManifest manifest = ((PendingLoadPlugin)entry.getValue()).manifest;

         for(PluginIdentifier targetId : manifest.getLoadBefore().keySet()) {
            EntryNode targetNode = (EntryNode)nodes.get(targetId);
            if (targetNode != null) {
               targetNode.edge.add((PluginIdentifier)entry.getKey());
            } else {
               ((Set)missingLoadBefore.computeIfAbsent((PluginIdentifier)entry.getKey(), (k) -> new HashSet())).add(targetId);
            }
         }
      }

      if (missingDependencies.isEmpty() && missingLoadBefore.isEmpty()) {
         ObjectArrayList<PendingLoadPlugin> loadOrder = new ObjectArrayList(nodes.size());

         while(!nodes.isEmpty()) {
            boolean didWork = false;
            Iterator<Map.Entry<PluginIdentifier, EntryNode>> iterator = nodes.entrySet().iterator();

            while(iterator.hasNext()) {
               Map.Entry<PluginIdentifier, EntryNode> entry = (Map.Entry)iterator.next();
               EntryNode node = (EntryNode)entry.getValue();
               if (node.edge.isEmpty()) {
                  didWork = true;
                  iterator.remove();
                  loadOrder.add(node.plugin);
                  PluginIdentifier identifier = (PluginIdentifier)entry.getKey();

                  for(EntryNode otherNode : nodes.values()) {
                     otherNode.edge.remove(identifier);
                  }
               }
            }

            if (!didWork) {
               StringBuilder sb = new StringBuilder("Found cyclic dependency between plugins:\n");

               for(Map.Entry<PluginIdentifier, EntryNode> entry : nodes.entrySet()) {
                  sb.append("  ").append(entry.getKey()).append(" waiting on: ").append(((EntryNode)entry.getValue()).edge).append("\n");
               }

               throw new IllegalArgumentException(sb.toString());
            }
         }

         return loadOrder;
      } else {
         StringBuilder sb = new StringBuilder();
         if (!missingDependencies.isEmpty()) {
            sb.append("Missing required dependencies:\n");

            for(Map.Entry<PluginIdentifier, Set<PluginIdentifier>> entry : missingDependencies.entrySet()) {
               sb.append("  ").append(entry.getKey()).append(" requires: ").append(entry.getValue()).append("\n");
            }
         }

         if (!missingLoadBefore.isEmpty()) {
            sb.append("Missing loadBefore targets:\n");

            for(Map.Entry<PluginIdentifier, Set<PluginIdentifier>> entry : missingLoadBefore.entrySet()) {
               sb.append("  ").append(entry.getKey()).append(" loadBefore: ").append(entry.getValue()).append("\n");
            }
         }

         throw new IllegalArgumentException(sb.toString());
      }
   }

   private static final class EntryNode {
      private final Set<PluginIdentifier> edge = new HashSet();
      private final PendingLoadPlugin plugin;

      private EntryNode(PendingLoadPlugin plugin) {
         this.plugin = plugin;
      }

      @Nonnull
      public String toString() {
         String var10000 = String.valueOf(this.plugin);
         return "EntryNode{plugin=" + var10000 + ", dependencies=" + String.valueOf(this.edge) + "}";
      }
   }
}
