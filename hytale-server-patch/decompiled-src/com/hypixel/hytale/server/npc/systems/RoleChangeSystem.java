package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.repulsion.Repulsion;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.components.Timers;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.NPCEntityEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerBlockEventSupport;
import com.hypixel.hytale.server.npc.components.messaging.PlayerEntityEventSupport;
import com.hypixel.hytale.server.npc.decisionmaker.stateevaluator.StateEvaluator;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoleChangeSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final ResourceType<EntityStore, RoleChangeQueue> roleChangeQueueResourceType;
   @Nonnull
   private final ComponentType<EntityStore, BeaconSupport> beaconSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerBlockEventSupport> playerBlockEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, NPCBlockEventSupport> npcBlockEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, PlayerEntityEventSupport> playerEntityEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, NPCEntityEventSupport> npcEntityEventSupportComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Timers> timersComponentType;
   @Nonnull
   private final ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType;
   @Nonnull
   private final ComponentType<EntityStore, ValueStore> valueStoreComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;

   public RoleChangeSystem(@Nonnull ResourceType<EntityStore, RoleChangeQueue> roleChangeQueueResourceType, @Nonnull ComponentType<EntityStore, BeaconSupport> beaconSupportComponentType, @Nonnull ComponentType<EntityStore, PlayerBlockEventSupport> playerBlockEventSupportComponentType, @Nonnull ComponentType<EntityStore, NPCBlockEventSupport> npcBlockEventSupportComponentType, @Nonnull ComponentType<EntityStore, PlayerEntityEventSupport> playerEntityEventSupportComponentType, @Nonnull ComponentType<EntityStore, NPCEntityEventSupport> npcEntityEventSupportComponentType, @Nonnull ComponentType<EntityStore, Timers> timersComponentType, @Nonnull ComponentType<EntityStore, StateEvaluator> stateEvaluatorComponentType, @Nonnull ComponentType<EntityStore, ValueStore> valueStoreComponentType) {
      this.dependencies = Set.of(new SystemDependency(Order.AFTER, NewSpawnStartTickingSystem.class));
      this.roleChangeQueueResourceType = roleChangeQueueResourceType;
      this.beaconSupportComponentType = beaconSupportComponentType;
      this.playerBlockEventSupportComponentType = playerBlockEventSupportComponentType;
      this.npcBlockEventSupportComponentType = npcBlockEventSupportComponentType;
      this.playerEntityEventSupportComponentType = playerEntityEventSupportComponentType;
      this.npcEntityEventSupportComponentType = npcEntityEventSupportComponentType;
      this.timersComponentType = timersComponentType;
      this.stateEvaluatorComponentType = stateEvaluatorComponentType;
      this.valueStoreComponentType = valueStoreComponentType;
   }

   @Nonnull
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      RoleChangeQueue roleChangeQueueResource = (RoleChangeQueue)store.getResource(this.roleChangeQueueResourceType);
      Deque<RoleChangeRequest> requests = roleChangeQueueResource.requests;

      while(true) {
         RoleChangeRequest request;
         NPCEntity npcComponent;
         Ref<EntityStore> npcEntityReference;
         while(true) {
            if (requests.isEmpty()) {
               return;
            }

            request = (RoleChangeRequest)requests.poll();
            if (request.reference.isValid()) {
               Holder<EntityStore> holder = store.removeEntity(request.reference, RemoveReason.UNLOAD);
               npcComponent = (NPCEntity)holder.getComponent(NPCEntity.getComponentType());

               assert npcComponent != null;

               npcComponent.setRole((Role)null);
               holder.tryRemoveComponent(this.beaconSupportComponentType);
               holder.tryRemoveComponent(this.playerBlockEventSupportComponentType);
               holder.tryRemoveComponent(this.npcBlockEventSupportComponentType);
               holder.tryRemoveComponent(this.playerEntityEventSupportComponentType);
               holder.tryRemoveComponent(this.npcEntityEventSupportComponentType);
               holder.tryRemoveComponent(this.timersComponentType);
               holder.tryRemoveComponent(this.stateEvaluatorComponentType);
               holder.tryRemoveComponent(this.valueStoreComponentType);
               holder.tryRemoveComponent(Repulsion.getComponentType());
               npcComponent.setRoleName(NPCPlugin.get().getName(request.roleIndex));
               npcComponent.setRoleIndex(request.roleIndex);

               try {
                  npcEntityReference = store.addEntity(holder, AddReason.LOAD);
                  break;
               } catch (Exception e) {
                  LOGGER.at(Level.SEVERE).log("Failed to change role: %s", e.getMessage());
               }
            }
         }

         if (npcEntityReference != null && npcEntityReference.isValid()) {
            if (request.changeAppearance) {
               Role role = npcComponent.getRole();
               NPCEntity.setAppearance(npcEntityReference, (String)role.getAppearanceName(), store);
            }

            if (request.state != null) {
               Role role = npcComponent.getRole();
               if (role != null) {
                  role.getStateSupport().setState(npcEntityReference, request.state, request.subState, store);
               }
            }
         } else {
            LOGGER.at(Level.SEVERE).log("Failed to change role: Could not re-add NPC entity to store");
         }
      }
   }

   public static void requestRoleChange(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, int roleIndex, boolean changeAppearance, @Nonnull Store<EntityStore> store) {
      requestRoleChange(ref, role, roleIndex, changeAppearance, (String)null, (String)null, store);
   }

   public static void requestRoleChange(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, int roleIndex, boolean changeAppearance, @Nullable String state, @Nullable String subState, @Nonnull ComponentAccessor<EntityStore> store) {
      RoleChangeQueue roleChangeResource = (RoleChangeQueue)store.getResource(RoleChangeSystem.RoleChangeQueue.getResourceType());
      Deque<RoleChangeRequest> queue = roleChangeResource.requests;
      queue.add(new RoleChangeRequest(ref, roleIndex, changeAppearance, state, subState));
      role.setRoleChangeRequested();
   }

   public static class RoleChangeQueue implements Resource<EntityStore> {
      @Nonnull
      private final Deque<RoleChangeRequest> requests = new ArrayDeque();

      @Nonnull
      public static ResourceType<EntityStore, RoleChangeQueue> getResourceType() {
         return NPCPlugin.get().getRoleChangeQueueResourceType();
      }

      @Nonnull
      public Resource<EntityStore> clone() {
         RoleChangeQueue roleChangeQueue = new RoleChangeQueue();
         roleChangeQueue.requests.addAll(this.requests);
         return roleChangeQueue;
      }
   }

   private static class RoleChangeRequest {
      @Nonnull
      private final Ref<EntityStore> reference;
      private final int roleIndex;
      private final boolean changeAppearance;
      @Nullable
      private final String state;
      @Nullable
      private final String subState;

      private RoleChangeRequest(@Nonnull Ref<EntityStore> reference, int roleIndex, boolean changeAppearance, @Nullable String state, @Nullable String subState) {
         this.reference = reference;
         this.roleIndex = roleIndex;
         this.changeAppearance = changeAppearance;
         this.state = state;
         this.subState = subState;
      }
   }
}
