package com.hypixel.hytale.server.npc.blackboard.view.combat;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.ChargingInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.role.support.CombatSupport;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public class CombatViewSystems {
   private static void clearCombatData(@Nonnull CombatData combatData, @Nonnull CombatDataPool dataPool) {
      if (combatData.interpreted) {
         List<InterpretedCombatData> dataList = combatData.combatData;

         for(int i = 0; i < dataList.size(); ++i) {
            dataPool.releaseCombatData((InterpretedCombatData)dataList.get(i));
         }

         dataList.clear();
         combatData.interpreted = false;
      }
   }

   @Nonnull
   public static List<InterpretedCombatData> getCombatData(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      CombatData combatDataComponent = (CombatData)store.getComponent(ref, CombatViewSystems.CombatData.getComponentType());
      if (combatDataComponent.interpreted) {
         return combatDataComponent.unmodifiableCombatData;
      } else {
         InteractionManager interactionManager = (InteractionManager)store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
         CombatDataPool combatDataPool = (CombatDataPool)store.getResource(CombatViewSystems.CombatDataPool.getResourceType());
         List<InterpretedCombatData> dataList = combatDataComponent.combatData;
         IndexedLookupTableAssetMap<String, RootInteraction> interactionAssetMap = RootInteraction.getAssetMap();
         Set<String> attackInteractions = interactionAssetMap.getKeysForTag(CombatSupport.ATTACK_TAG_INDEX);
         Set<String> meleeInteractions = interactionAssetMap.getKeysForTag(CombatSupport.MELEE_TAG_INDEX);
         Set<String> rangedInteractions = interactionAssetMap.getKeysForTag(CombatSupport.RANGED_TAG_INDEX);
         Set<String> blockInteractions = interactionAssetMap.getKeysForTag(CombatSupport.BLOCK_TAG_INDEX);
         interactionManager.forEachInteraction((chain, interaction, list) -> {
            String rootId = chain.getRootInteraction().getId();
            if (!attackInteractions.contains(rootId)) {
               return list;
            } else {
               InterpretedCombatData entry = combatDataPool.getEmptyCombatData();
               entry.setAttack(rootId);
               entry.setCurrentElapsedTime(chain.getTimeInSeconds());
               entry.setCharging(interaction instanceof ChargingInteraction);
               entry.setPerformingMeleeAttack(meleeInteractions.contains(rootId));
               entry.setPerformingRangedAttack(rangedInteractions.contains(rootId));
               entry.setPerformingBlock(blockInteractions.contains(rootId));
               list.add(entry);
               return list;
            }
         }, dataList);
         combatDataComponent.interpreted = true;
         return combatDataComponent.unmodifiableCombatData;
      }
   }

   public static class Ensure extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatData> combatDataComponentType;

      public Ensure(ComponentType<EntityStore, CombatData> combatDataComponentType) {
         this.combatDataComponentType = combatDataComponentType;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return AllLegacyEntityTypesQuery.INSTANCE;
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(this.combatDataComponentType);
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class EntityRemoved extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatData> combatDataComponentType;
      private final ResourceType<EntityStore, CombatDataPool> dataPoolResourceType;

      public EntityRemoved(ComponentType<EntityStore, CombatData> combatDataComponentType, ResourceType<EntityStore, CombatDataPool> dataPoolResourceType) {
         this.combatDataComponentType = combatDataComponentType;
         this.dataPoolResourceType = dataPoolResourceType;
      }

      public Query<EntityStore> getQuery() {
         return this.combatDataComponentType;
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         CombatData combatData = (CombatData)holder.getComponent(this.combatDataComponentType);
         CombatDataPool dataPool = (CombatDataPool)store.getResource(this.dataPoolResourceType);
         CombatViewSystems.clearCombatData(combatData, dataPool);
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, CombatData> combatDataComponentType;
      private final ResourceType<EntityStore, CombatDataPool> dataPoolResourceType;

      public Ticking(ComponentType<EntityStore, CombatData> combatDataComponentType, ResourceType<EntityStore, CombatDataPool> dataPoolResourceType) {
         this.combatDataComponentType = combatDataComponentType;
         this.dataPoolResourceType = dataPoolResourceType;
      }

      public Query<EntityStore> getQuery() {
         return this.combatDataComponentType;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         CombatData combatData = (CombatData)archetypeChunk.getComponent(index, this.combatDataComponentType);
         CombatDataPool dataPool = (CombatDataPool)store.getResource(this.dataPoolResourceType);
         CombatViewSystems.clearCombatData(combatData, dataPool);
      }
   }

   public static class CombatData implements Component<EntityStore> {
      private final List<InterpretedCombatData> combatData = new ObjectArrayList();
      private final List<InterpretedCombatData> unmodifiableCombatData;
      private boolean interpreted;

      public CombatData() {
         this.unmodifiableCombatData = Collections.unmodifiableList(this.combatData);
      }

      public static ComponentType<EntityStore, CombatData> getComponentType() {
         return NPCPlugin.get().getCombatDataComponentType();
      }

      @Nonnull
      public Component<EntityStore> clone() {
         CombatData data = new CombatData();
         data.interpreted = this.interpreted;

         for(int i = 0; i < this.combatData.size(); ++i) {
            data.combatData.add(((InterpretedCombatData)this.combatData.get(i)).clone());
         }

         return data;
      }
   }

   public static class CombatDataPool implements Resource<EntityStore> {
      private final ArrayDeque<InterpretedCombatData> combatDataPool = new ArrayDeque();

      public static ResourceType<EntityStore, CombatDataPool> getResourceType() {
         return NPCPlugin.get().getCombatDataPoolResourceType();
      }

      @Nonnull
      public Resource<EntityStore> clone() {
         return new CombatDataPool();
      }

      public InterpretedCombatData getEmptyCombatData() {
         return this.combatDataPool.isEmpty() ? new InterpretedCombatData() : (InterpretedCombatData)this.combatDataPool.poll();
      }

      public void releaseCombatData(@Nonnull InterpretedCombatData combatData) {
         this.combatDataPool.push(combatData);
      }
   }
}
