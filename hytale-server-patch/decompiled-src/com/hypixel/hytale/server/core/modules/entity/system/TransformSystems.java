package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.TransformUpdate;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PositionUtil;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TransformSystems {
   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, HeadRotation> headRotationComponentType = HeadRotation.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate() {
         this.query = Query.<EntityStore>and(this.visibleComponentType, this.transformComponentType);
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

         TransformComponent transformComponent = (TransformComponent)archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         HeadRotation headRotationComponent = (HeadRotation)archetypeChunk.getComponent(index, this.headRotationComponentType);
         ModelTransform sentTransform = transformComponent.getSentTransform();
         Vector3d position = transformComponent.getPosition();
         Vector3f headRotation = headRotationComponent != null ? headRotationComponent.getRotation() : Vector3f.ZERO;
         Vector3f bodyRotation = transformComponent.getRotation();
         Position sentPosition = sentTransform.position;
         Direction sentLookOrientation = sentTransform.lookOrientation;
         Direction sentBodyOrientation = sentTransform.bodyOrientation;
         if (PositionUtil.equals(position, sentPosition) && PositionUtil.equals(headRotation, sentLookOrientation) && PositionUtil.equals(bodyRotation, sentBodyOrientation)) {
            if (!visibleComponent.newlyVisibleTo.isEmpty()) {
               queueUpdatesFor(archetypeChunk.getReferenceTo(index), sentTransform, visibleComponent.newlyVisibleTo, true);
            }
         } else {
            PositionUtil.assign(sentPosition, position);
            PositionUtil.assign(sentLookOrientation, headRotation);
            PositionUtil.assign(sentBodyOrientation, bodyRotation);
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), sentTransform, visibleComponent.visibleTo, false);
         }

      }

      private static void queueUpdatesFor(@Nonnull Ref<EntityStore> ref, @Nonnull ModelTransform sentTransform, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo, boolean newlyVisible) {
         TransformUpdate update = new TransformUpdate(sentTransform);

         for(Map.Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
            if (newlyVisible || !ref.equals(entry.getKey())) {
               ((EntityTrackerSystems.EntityViewer)entry.getValue()).queueUpdate(ref, update);
            }
         }

      }
   }

   public static class OnRemove extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();

      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         ((TransformComponent)holder.getComponent(this.transformComponentType)).setChunkLocation((Ref)null, (WorldChunk)null);
      }

      public Query<EntityStore> getQuery() {
         return this.transformComponentType;
      }
   }
}
