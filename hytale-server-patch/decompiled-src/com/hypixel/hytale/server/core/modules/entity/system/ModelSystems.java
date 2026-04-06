package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.common.util.RandomUtil;
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
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.ActiveAnimationsUpdate;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.PropComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ApplyRandomSkinPersistedComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class ModelChange extends RefChangeSystem<EntityStore, ModelComponent> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, PersistentModel> persistentModelComponentType = PersistentModel.getComponentType();

      public Query<EntityStore> getQuery() {
         return this.persistentModelComponentType;
      }

      @Nonnull
      public ComponentType<EntityStore, ModelComponent> componentType() {
         return this.modelComponentType;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, ModelComponent oldComponent, @Nonnull ModelComponent newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         PersistentModel persistentModelComponent = (PersistentModel)store.getComponent(ref, this.persistentModelComponentType);

         assert persistentModelComponent != null;

         persistentModelComponent.setModelReference(newComponent.getModel().toReference());
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         commandBuffer.removeComponent(ref, this.persistentModelComponentType);
      }
   }

   public static class ModelSpawned extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType = BoundingBox.getComponentType();
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public ModelSpawned() {
         this.dependencies = Set.of(new SystemDependency(Order.AFTER, SetRenderedModel.class));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         ModelComponent modelComponent = (ModelComponent)holder.getComponent(this.modelComponentType);

         assert modelComponent != null;

         Model model = modelComponent.getModel();
         if (model == null) {
            ((HytaleLogger.Api)ModelSystems.LOGGER.atWarning()).log("Failed to set bounding box for entity as model is null");
         } else {
            Box modelBoundingBox = model.getBoundingBox();
            if (modelBoundingBox == null) {
               ((HytaleLogger.Api)ModelSystems.LOGGER.atWarning()).log("Failed to set bounding box for entity as model bounding box is null: %s", model.getModel());
            } else {
               BoundingBox boundingBox = (BoundingBox)holder.getComponent(this.boundingBoxComponentType);
               if (boundingBox == null) {
                  boundingBox = new BoundingBox();
                  holder.addComponent(this.boundingBoxComponentType, boundingBox);
               }

               boundingBox.setBoundingBox(modelBoundingBox);
               boundingBox.setDetailBoxes(model.getDetailBoxes());
            }
         }
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      public Query<EntityStore> getQuery() {
         return this.modelComponentType;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class SetRenderedModel extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, PersistentModel> persistentModelComponentType = PersistentModel.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public SetRenderedModel() {
         this.query = Query.<EntityStore>and(this.persistentModelComponentType, Query.not(this.modelComponentType));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PersistentModel persistentModelComponent = (PersistentModel)holder.getComponent(this.persistentModelComponentType);

         assert persistentModelComponent != null;

         Model model = persistentModelComponent.getModelReference().toModel();
         if (model != null) {
            ModelComponent modelComponent = new ModelComponent(model);
            holder.putComponent(this.modelComponentType, modelComponent);
         } else {
            ((HytaleLogger.Api)ModelSystems.LOGGER.atWarning()).log("Failed to load model for entity with PersistentModel: {}", persistentModelComponent.getModelReference());
         }

      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class AssignNetworkIdToProps extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, PropComponent> propComponentType = PropComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public AssignNetworkIdToProps() {
         this.query = Query.<EntityStore>and(this.propComponentType, Query.not(this.networkIdComponentType));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.addComponent(this.networkIdComponentType, new NetworkId(((EntityStore)store.getExternalData()).takeNextNetworkId()));
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class EnsurePropsPrefabCopyable extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, PropComponent> propComponentType = PropComponent.getComponentType();

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(PrefabCopyableComponent.getComponentType());
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.propComponentType;
      }
   }

   public static class ApplyRandomSkin extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, ApplyRandomSkinPersistedComponent> randomSkinComponent = ApplyRandomSkinPersistedComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public ApplyRandomSkin() {
         this.query = Query.<EntityStore>and(this.randomSkinComponent, this.modelComponentType);
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PlayerSkin playerSkin = CosmeticsModule.get().generateRandomSkin(RandomUtil.getSecureRandom());
         holder.putComponent(PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(playerSkin));
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }

   public static class PlayerConnect extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public PlayerConnect() {
         this.query = Query.<EntityStore>and(this.playerComponentType, Query.not(this.modelComponentType));
         this.dependencies = Set.of(new SystemDependency(Order.BEFORE, ModelSpawned.class));
      }

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Player playerComponent = (Player)holder.getComponent(this.playerComponentType);

         assert playerComponent != null;

         DefaultAssetMap<String, ModelAsset> assetMap = ModelAsset.getAssetMap();
         String preset = playerComponent.getPlayerConfigData().getPreset();
         ModelAsset modelAsset = preset != null ? (ModelAsset)assetMap.getAsset(preset) : null;
         if (modelAsset != null) {
            Model model = Model.createUnitScaleModel(modelAsset);
            holder.addComponent(this.modelComponentType, new ModelComponent(model));
         } else {
            ModelAsset defaultModelAsset = assetMap.getAsset("Player");
            if (defaultModelAsset != null) {
               Model defaultModel = Model.createUnitScaleModel(defaultModelAsset);
               holder.addComponent(this.modelComponentType, new ModelComponent(defaultModel));
            }

         }
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class UpdateBoundingBox extends RefChangeSystem<EntityStore, ModelComponent> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType = BoundingBox.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType = MovementStatesComponent.getComponentType();

      public Query<EntityStore> getQuery() {
         return this.boundingBoxComponentType;
      }

      @Nonnull
      public ComponentType<EntityStore, ModelComponent> componentType() {
         return this.modelComponentType;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         BoundingBox boundingBoxComponent = (BoundingBox)commandBuffer.getComponent(ref, this.boundingBoxComponentType);

         assert boundingBoxComponent != null;

         MovementStatesComponent movementStatesComponent = (MovementStatesComponent)commandBuffer.getComponent(ref, this.movementStatesComponentType);

         assert movementStatesComponent != null;

         updateBoundingBox(component.getModel(), boundingBoxComponent, movementStatesComponent);
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, ModelComponent oldComponent, @Nonnull ModelComponent newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         BoundingBox boundingBoxComponent = (BoundingBox)commandBuffer.getComponent(ref, this.boundingBoxComponentType);

         assert boundingBoxComponent != null;

         MovementStatesComponent movementStatesComponent = (MovementStatesComponent)commandBuffer.getComponent(ref, this.movementStatesComponentType);

         assert movementStatesComponent != null;

         updateBoundingBox(newComponent.getModel(), boundingBoxComponent, movementStatesComponent);
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         BoundingBox boundingBoxComponent = (BoundingBox)commandBuffer.getComponent(ref, this.boundingBoxComponentType);

         assert boundingBoxComponent != null;

         boundingBoxComponent.setBoundingBox(new Box());
      }

      protected static void updateBoundingBox(@Nonnull Model model, @Nonnull BoundingBox boundingBox, @Nullable MovementStatesComponent movementStatesComponent) {
         updateBoundingBox(model, boundingBox, movementStatesComponent != null ? movementStatesComponent.getMovementStates() : null);
      }

      protected static void updateBoundingBox(@Nonnull Model model, @Nonnull BoundingBox boundingBox, @Nullable MovementStates movementStates) {
         Box modelBoundingBox = model.getBoundingBox(movementStates);
         if (modelBoundingBox == null) {
            modelBoundingBox = new Box();
         }

         boundingBox.setBoundingBox(modelBoundingBox);
      }
   }

   public static class UpdateMovementStateBoundingBox extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES;
      @Nonnull
      private final ComponentType<EntityStore, MovementStatesComponent> movementStatesComponentType = MovementStatesComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType = BoundingBox.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public UpdateMovementStateBoundingBox() {
         this.query = Query.<EntityStore>and(this.movementStatesComponentType, this.boundingBoxComponentType, this.modelComponentType);
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         MovementStatesComponent movementStatesComponent = (MovementStatesComponent)archetypeChunk.getComponent(index, this.movementStatesComponentType);

         assert movementStatesComponent != null;

         MovementStates newMovementStates = movementStatesComponent.getMovementStates();
         MovementStates sentMovementStates = movementStatesComponent.getSentMovementStates();
         boolean crouchingChanged = newMovementStates.crouching != sentMovementStates.crouching || newMovementStates.forcedCrouching != sentMovementStates.forcedCrouching;
         boolean slidingChanged = newMovementStates.sliding != sentMovementStates.sliding;
         boolean sittingChanged = newMovementStates.sitting != sentMovementStates.sitting;
         boolean sleepingChanged = newMovementStates.sleeping != sentMovementStates.sleeping;
         if (crouchingChanged || slidingChanged || sittingChanged || sleepingChanged) {
            ModelComponent modelComponent = (ModelComponent)archetypeChunk.getComponent(index, this.modelComponentType);

            assert modelComponent != null;

            Model model = modelComponent.getModel();
            BoundingBox boundingBoxComponent = (BoundingBox)archetypeChunk.getComponent(index, this.boundingBoxComponentType);

            assert boundingBoxComponent != null;

            ModelSystems.UpdateBoundingBox.updateBoundingBox(model, boundingBoxComponent, newMovementStates);
         }
      }

      static {
         DEPENDENCIES = Collections.singleton(new SystemDependency(Order.BEFORE, MovementStatesSystems.TickingSystem.class));
      }
   }

   public static class PlayerUpdateMovementManager extends RefChangeSystem<EntityStore, ModelComponent> {
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public PlayerUpdateMovementManager() {
         this.query = Query.<EntityStore>and(this.playerComponentType, MovementManager.getComponentType());
         this.dependencies = Set.of(new SystemDependency(Order.AFTER, UpdateBoundingBox.class));
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      public ComponentType<EntityStore, ModelComponent> componentType() {
         return this.modelComponentType;
      }

      public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         updateMovementController(ref, commandBuffer);
      }

      public void onComponentSet(@Nonnull Ref<EntityStore> ref, ModelComponent oldComponent, @Nonnull ModelComponent newComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         updateMovementController(ref, commandBuffer);
      }

      public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull ModelComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         updateMovementController(ref, commandBuffer);
      }

      private static void updateMovementController(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         MovementManager movementManagerComponent = (MovementManager)componentAccessor.getComponent(ref, MovementManager.getComponentType());

         assert movementManagerComponent != null;

         movementManagerComponent.resetDefaultsAndUpdate(ref, componentAccessor);
      }
   }

   public static class AnimationEntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, ActiveAnimationComponent> activeAnimationComponentType = ActiveAnimationComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public AnimationEntityTrackerUpdate() {
         this.query = Query.<EntityStore>and(this.visibleComponentType, this.activeAnimationComponentType);
      }

      @Nullable
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         EntityTrackerSystems.Visible visibleComponent = (EntityTrackerSystems.Visible)archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         ActiveAnimationComponent activeAnimationComponent = (ActiveAnimationComponent)archetypeChunk.getComponent(index, this.activeAnimationComponentType);

         assert activeAnimationComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (activeAnimationComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(ref, activeAnimationComponent, visibleComponent.visibleTo);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, activeAnimationComponent, visibleComponent.newlyVisibleTo);
         }

      }

      private static void queueUpdatesFor(@Nonnull Ref<EntityStore> ref, @Nonnull ActiveAnimationComponent animationComponent, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo) {
         ActiveAnimationsUpdate update = new ActiveAnimationsUpdate(animationComponent.getActiveAnimations());

         for(Map.Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
            ((EntityTrackerSystems.EntityViewer)entry.getValue()).queueUpdate(ref, update);
         }

      }
   }
}
