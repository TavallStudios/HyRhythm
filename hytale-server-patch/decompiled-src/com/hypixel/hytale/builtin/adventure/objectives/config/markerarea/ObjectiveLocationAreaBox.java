package com.hypixel.hytale.builtin.adventure.objectives.config.markerarea;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import javax.annotation.Nonnull;

public class ObjectiveLocationAreaBox extends ObjectiveLocationMarkerArea {
   @Nonnull
   public static final BuilderCodec<ObjectiveLocationAreaBox> CODEC;
   @Nonnull
   private static final Box DEFAULT_ENTRY_BOX;
   @Nonnull
   private static final Box DEFAULT_EXIT_BOX;
   private Box entryArea;
   private Box exitArea;

   public ObjectiveLocationAreaBox(Box entryBox, Box exitBox) {
      this.entryArea = entryBox;
      this.exitArea = exitBox;
      this.computeAreaBoxes();
   }

   protected ObjectiveLocationAreaBox() {
      this(DEFAULT_ENTRY_BOX, DEFAULT_EXIT_BOX);
   }

   public Box getEntryArea() {
      return this.entryArea;
   }

   public Box getExitArea() {
      return this.exitArea;
   }

   public void getPlayersInEntryArea(@Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull List<Ref<EntityStore>> results, @Nonnull Vector3d markerPosition) {
      getPlayersInArea(spatialComponent, results, markerPosition, this.entryArea);
   }

   public void getPlayersInExitArea(@Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull List<Ref<EntityStore>> results, @Nonnull Vector3d markerPosition) {
      getPlayersInArea(spatialComponent, results, markerPosition, this.exitArea);
   }

   public boolean hasPlayerInExitArea(@Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType, @Nonnull Vector3d markerPosition, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
      Ref<EntityStore> reference = spatialComponent.getSpatialStructure().closest(markerPosition);
      if (reference == null) {
         return false;
      } else {
         TransformComponent transformComponent = (TransformComponent)commandBuffer.getComponent(reference, TransformComponent.getComponentType());

         assert transformComponent != null;

         return this.exitArea.containsPosition(markerPosition, transformComponent.getPosition());
      }
   }

   public boolean isPlayerInEntryArea(@Nonnull Vector3d playerPosition, @Nonnull Vector3d markerPosition) {
      return this.entryArea.containsPosition(markerPosition, playerPosition);
   }

   @Nonnull
   public ObjectiveLocationMarkerArea getRotatedArea(float yaw, float pitch) {
      float snappedYaw = (float)Math.round(yaw / 1.5707964F) * 1.5707964F;
      if (Math.abs(snappedYaw % 6.2831855F) > 0.7853982F) {
         Box entry = this.entryArea.clone().rotateY(snappedYaw).normalize();
         Box exit = this.exitArea.clone().rotateY(snappedYaw).normalize();
         return new ObjectiveLocationAreaBox(entry, exit);
      } else {
         return this;
      }
   }

   protected void computeAreaBoxes() {
      this.entryAreaBox = this.entryArea;
      this.exitAreaBox = this.exitArea;
   }

   private static void getPlayersInArea(@Nonnull SpatialResource<Ref<EntityStore>, EntityStore> spatialComponent, @Nonnull List<Ref<EntityStore>> results, @Nonnull Vector3d markerPosition, @Nonnull Box box) {
      spatialComponent.getSpatialStructure().collect(markerPosition, box.getMaximumExtent(), results);
   }

   @Nonnull
   public String toString() {
      return "ObjectiveLocationAreaBox{} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ObjectiveLocationAreaBox.class, ObjectiveLocationAreaBox::new).append(new KeyedCodec("EntryBox", Box.CODEC), (objectiveLocationAreaBox, box) -> objectiveLocationAreaBox.entryArea = box, (objectiveLocationAreaBox) -> objectiveLocationAreaBox.entryArea).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("ExitBox", Box.CODEC), (objectiveLocationAreaBox, box) -> objectiveLocationAreaBox.exitArea = box, (objectiveLocationAreaBox) -> objectiveLocationAreaBox.exitArea).addValidator(Validators.nonNull()).add()).afterDecode(ObjectiveLocationAreaBox::computeAreaBoxes)).build();
      DEFAULT_ENTRY_BOX = new Box(-5.0, -5.0, -5.0, 5.0, 5.0, 5.0);
      DEFAULT_EXIT_BOX = new Box(-10.0, -10.0, -10.0, 10.0, 10.0, 10.0);
   }
}
