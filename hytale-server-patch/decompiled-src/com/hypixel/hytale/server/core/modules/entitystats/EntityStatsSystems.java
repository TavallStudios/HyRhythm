package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemTypeDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityStatUpdate;
import com.hypixel.hytale.protocol.EntityStatsUpdate;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatsSystems {
   public static class Setup extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityStatMap> componentType;

      public Setup(ComponentType<EntityStore, EntityStatMap> componentType) {
         this.componentType = componentType;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return AllLegacyLivingEntityTypesQuery.INSTANCE;
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         EntityStatMap stats = (EntityStatMap)holder.getComponent(this.componentType);
         if (stats == null) {
            stats = (EntityStatMap)holder.ensureAndGetComponent(this.componentType);
            stats.update();
         }

      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class Regenerate<EntityType extends LivingEntity> extends EntityTickingSystem<EntityStore> implements StatModifyingSystem {
      private final ComponentType<EntityStore, EntityStatMap> componentType;
      private final ComponentType<EntityStore, EntityType> entityTypeComponent;
      private final Query<EntityStore> query;

      public Regenerate(ComponentType<EntityStore, EntityStatMap> componentType, ComponentType<EntityStore, EntityType> entityTypeComponent) {
         this.componentType = componentType;
         this.entityTypeComponent = entityTypeComponent;
         this.query = Query.<EntityStore>and(componentType, entityTypeComponent);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         EntityStatMap map = (EntityStatMap)archetypeChunk.getComponent(index, this.componentType);

         assert map != null;

         Instant now = ((TimeResource)store.getResource(TimeResource.getResourceType())).getNow();
         int size = map.size();
         if (map.tempRegenerationValues.length < size) {
            map.tempRegenerationValues = new float[size];
         }

         for(int statIndex = 1; statIndex < size; ++statIndex) {
            EntityStatValue value = map.get(statIndex);
            if (value != null) {
               map.tempRegenerationValues[statIndex] = 0.0F;
               RegeneratingValue[] regenerating = value.getRegeneratingValues();
               if (regenerating != null) {
                  for(RegeneratingValue regeneratingValue : regenerating) {
                     if (regeneratingValue.getRegenerating().getAmount() > 0.0F) {
                        if (value.get() >= value.getMax()) {
                           continue;
                        }
                     } else if (value.get() <= value.getMin()) {
                        continue;
                     }

                     float[] var10000 = map.tempRegenerationValues;
                     var10000[statIndex] += regeneratingValue.regenerate(commandBuffer, ref, now, dt, value, map.tempRegenerationValues[statIndex]);
                  }
               }
            }
         }

         EntityType entity = (EntityType)(archetypeChunk.getComponent(index, this.entityTypeComponent));

         assert entity != null;

         ItemContainer armorContainer = entity.getInventory().getArmor();
         short armorContainerCapacity = armorContainer.getCapacity();

         for(short i = 0; i < armorContainerCapacity; ++i) {
            ItemStack itemStack = armorContainer.getItemStack(i);
            if (!ItemStack.isEmpty(itemStack)) {
               Item item = itemStack.getItem();
               if (item.getArmor() != null && item.getArmor().getRegeneratingValues() != null && !item.getArmor().getRegeneratingValues().isEmpty()) {
                  for(int statIndex = 1; statIndex < size; ++statIndex) {
                     EntityStatValue value = map.get(statIndex);
                     if (value != null) {
                        List<RegeneratingValue> regenValues = (List)item.getArmor().getRegeneratingValues().get(statIndex);
                        if (regenValues != null && !regenValues.isEmpty()) {
                           for(RegeneratingValue regeneratingValue : regenValues) {
                              if (regeneratingValue.getRegenerating().getAmount() > 0.0F) {
                                 if (value.get() >= value.getMax()) {
                                    continue;
                                 }
                              } else if (value.get() <= value.getMin()) {
                                 continue;
                              }

                              float[] var32 = map.tempRegenerationValues;
                              var32[statIndex] += regeneratingValue.regenerate(commandBuffer, ref, now, dt, value, map.tempRegenerationValues[statIndex]);
                           }
                        }
                     }
                  }
               }
            }
         }

         for(int statIndex = 1; statIndex < size; ++statIndex) {
            EntityStatValue value = map.get(statIndex);
            if (value != null) {
               float amount = map.tempRegenerationValues[statIndex];
               boolean invulnerable = commandBuffer.getArchetype(ref).contains(Invulnerable.getComponentType());
               if (amount < 0.0F && !value.getIgnoreInvulnerability() && invulnerable) {
                  return;
               }

               if (amount != 0.0F) {
                  map.addStatValue(statIndex, amount);
               }
            }
         }

      }
   }

   public static class Changes extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityStatMap> componentType;
      @Nonnull
      private final Query<EntityStore> query;
      private final Set<Dependency<EntityStore>> dependencies;

      public Changes(ComponentType<EntityStore, EntityStatMap> componentType) {
         this.dependencies = Set.of(new SystemDependency(Order.BEFORE, EntityTrackerUpdate.class), new SystemTypeDependency(Order.AFTER, EntityStatsModule.get().getStatModifyingSystemType()));
         this.componentType = componentType;
         this.query = Query.<EntityStore>and(componentType, InteractionModule.get().getInteractionManagerComponent(), AllLegacyLivingEntityTypesQuery.INSTANCE);
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         EntityStatMap entityStatMapComponent = (EntityStatMap)archetypeChunk.getComponent(index, this.componentType);

         assert entityStatMapComponent != null;

         InteractionManager interactionManagerComponent = (InteractionManager)archetypeChunk.getComponent(index, InteractionModule.get().getInteractionManagerComponent());

         assert interactionManagerComponent != null;

         boolean isDead = archetypeChunk.getArchetype().contains(DeathComponent.getComponentType());
         Int2ObjectMap<List<EntityStatUpdate>> statChanges = entityStatMapComponent.getSelfUpdates();
         Int2ObjectMap<FloatList> statValues = entityStatMapComponent.getSelfStatValues();

         for(int statIndex = 0; statIndex < entityStatMapComponent.size(); ++statIndex) {
            List<EntityStatUpdate> updates = (List)statChanges.get(statIndex);
            if (updates != null && !updates.isEmpty()) {
               FloatList statChangeList = (FloatList)statValues.get(statIndex);
               EntityStatValue entityStatValue = entityStatMapComponent.get(statIndex);
               if (entityStatValue != null) {
                  EntityStatType entityStatType = (EntityStatType)EntityStatType.getAssetMap().getAsset(statIndex);

                  for(int i = 0; i < updates.size(); ++i) {
                     EntityStatUpdate update = (EntityStatUpdate)updates.get(i);
                     float statPrevious = statChangeList.getFloat(i * 2);
                     float statValue = statChangeList.getFloat(i * 2 + 1);
                     if (testMaxValue(statValue, statPrevious, entityStatValue, entityStatType.getMaxValueEffects())) {
                        runInteractions(ref, interactionManagerComponent, entityStatType.getMaxValueEffects(), commandBuffer);
                     }

                     if (testMinValue(statValue, statPrevious, entityStatValue, entityStatType.getMinValueEffects())) {
                        runInteractions(ref, interactionManagerComponent, entityStatType.getMinValueEffects(), commandBuffer);
                     }

                     if (!isDead && statIndex == DefaultEntityStatTypes.getHealth() && !(update.value > 0.0F) && statValue <= entityStatValue.getMin()) {
                        DeathComponent.tryAddComponent(commandBuffer, archetypeChunk.getReferenceTo(index), new Damage(Damage.NULL_SOURCE, DamageCause.COMMAND, 0.0F));
                        isDead = true;
                     }
                  }
               }
            }
         }

      }

      private static boolean testMaxValue(float value, float previousValue, @Nonnull EntityStatValue stat, @Nullable EntityStatType.EntityStatEffects valueEffects) {
         if (valueEffects == null) {
            return false;
         } else if (valueEffects.triggerAtZero() && stat.getMax() > 0.0F) {
            return previousValue < 0.0F && value >= 0.0F;
         } else {
            return previousValue != stat.getMax() && value == stat.getMax();
         }
      }

      private static boolean testMinValue(float value, float previousValue, @Nonnull EntityStatValue stat, @Nullable EntityStatType.EntityStatEffects valueEffects) {
         if (valueEffects == null) {
            return false;
         } else if (valueEffects.triggerAtZero() && stat.getMin() < 0.0F) {
            return previousValue > 0.0F && value < 0.0F;
         } else {
            return previousValue != stat.getMin() && value == stat.getMin();
         }
      }

      private static void runInteractions(@Nonnull Ref<EntityStore> ref, @Nonnull InteractionManager interactionManager, @Nullable EntityStatType.EntityStatEffects valueEffects, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         if (valueEffects != null) {
            String interactions = valueEffects.getInteractions();
            if (interactions != null) {
               InteractionContext context = InteractionContext.forInteraction(interactionManager, ref, InteractionType.EntityStatEffect, componentAccessor);
               InteractionChain chain = interactionManager.initChain(InteractionType.EntityStatEffect, context, RootInteraction.getRootInteractionOrUnknown(interactions), true);
               interactionManager.queueExecuteChain(chain);
            }
         }
      }
   }

   public static class Recalculate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Recalculate(@Nonnull ComponentType<EntityStore, EntityStatMap> entityStatMapComponentType) {
         this.entityStatMapComponentType = entityStatMapComponentType;
         this.query = Query.<EntityStore>and(AllLegacyLivingEntityTypesQuery.INSTANCE, entityStatMapComponentType);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         LivingEntity livingEntity = (LivingEntity)EntityUtils.getEntity(index, archetypeChunk);

         assert livingEntity != null;

         EntityStatMap entityStatMapComponent = (EntityStatMap)archetypeChunk.getComponent(index, this.entityStatMapComponentType);

         assert entityStatMapComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         livingEntity.getStatModifiersManager().recalculateEntityStatModifiers(ref, entityStatMapComponent, commandBuffer);
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityStatMap> componentType;
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;
      private final Set<Dependency<EntityStore>> dependencies;

      public EntityTrackerUpdate(ComponentType<EntityStore, EntityStatMap> componentType) {
         this.dependencies = Set.of(new SystemDependency(Order.BEFORE, EntityTrackerSystems.EffectControllerSystem.class), new SystemTypeDependency(Order.AFTER, EntityStatsModule.get().getStatModifyingSystemType()));
         this.componentType = componentType;
         this.query = Query.<EntityStore>and(this.visibleComponentType, componentType);
      }

      @Nullable
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         EntityTrackerSystems.Visible visible = (EntityTrackerSystems.Visible)archetypeChunk.getComponent(index, this.visibleComponentType);
         EntityStatMap statMap = (EntityStatMap)archetypeChunk.getComponent(index, this.componentType);
         if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesForNewlyVisible(ref, statMap, visible.newlyVisibleTo);
         }

         if (statMap.consumeSelfNetworkOutdated()) {
            EntityTrackerSystems.EntityViewer selfEntityViewer = (EntityTrackerSystems.EntityViewer)visible.visibleTo.get(ref);
            if (selfEntityViewer != null && !visible.newlyVisibleTo.containsKey(ref)) {
               EntityStatsUpdate update = new EntityStatsUpdate(statMap.consumeSelfUpdates());
               selfEntityViewer.queueUpdate(ref, update);
            }
         }

         if (statMap.consumeNetworkOutdated()) {
            EntityStatsUpdate update = new EntityStatsUpdate(statMap.consumeOtherUpdates());

            for(Map.Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visible.visibleTo.entrySet()) {
               Ref<EntityStore> viewerRef = (Ref)entry.getKey();
               if (!visible.newlyVisibleTo.containsKey(viewerRef) && !ref.equals(viewerRef)) {
                  ((EntityTrackerSystems.EntityViewer)entry.getValue()).queueUpdate(ref, update);
               }
            }
         }

      }

      private static void queueUpdatesForNewlyVisible(@Nonnull Ref<EntityStore> ref, @Nonnull EntityStatMap statMap, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> newlyVisibleTo) {
         EntityStatsUpdate update = new EntityStatsUpdate(statMap.createInitUpdate(false));

         for(Map.Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : newlyVisibleTo.entrySet()) {
            if (ref.equals(entry.getKey())) {
               queueUpdateForNewlyVisibleSelf(ref, statMap, (EntityTrackerSystems.EntityViewer)entry.getValue());
            } else {
               ((EntityTrackerSystems.EntityViewer)entry.getValue()).queueUpdate(ref, update);
            }
         }

      }

      private static void queueUpdateForNewlyVisibleSelf(Ref<EntityStore> ref, @Nonnull EntityStatMap statMap, @Nonnull EntityTrackerSystems.EntityViewer viewer) {
         EntityStatsUpdate update = new EntityStatsUpdate(statMap.createInitUpdate(true));
         viewer.queueUpdate(ref, update);
      }
   }

   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, EntityStatMap> {
      private final ComponentType<EntityStore, EntityStatMap> componentType;
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerRemove(ComponentType<EntityStore, EntityStatMap> componentType) {
         this.componentType = componentType;
         this.query = Query.<EntityStore>and(this.visibleComponentType, componentType);
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public ComponentType<EntityStore, EntityStatMap> componentType() {
         return this.componentType;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull EntityStatMap component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, EntityStatMap oldComponent, @Nonnull EntityStatMap newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull EntityStatMap component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         for(EntityTrackerSystems.EntityViewer viewer : ((EntityTrackerSystems.Visible)store.getComponent(ref, this.visibleComponentType)).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.EntityStats);
         }

      }
   }

   public static class ClearChanges extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityStatMap> componentType;
      private final Set<Dependency<EntityStore>> dependencies;

      public ClearChanges(ComponentType<EntityStore, EntityStatMap> componentType) {
         this.dependencies = Set.of(new SystemDependency(Order.AFTER, EntityTrackerUpdate.class));
         this.componentType = componentType;
      }

      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         EntityStatMap statMap = (EntityStatMap)archetypeChunk.getComponent(index, this.componentType);
         statMap.clearUpdates();
      }
   }

   public interface StatModifyingSystem extends ISystem<EntityStore> {
   }
}
