package com.hypixel.hytale.builtin.path.entities;

import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.builtin.path.path.PatrolPath;
import com.hypixel.hytale.builtin.path.waypoint.IPrefabPathWaypoint;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PatrolPathMarkerEntity extends Entity implements IPrefabPathWaypoint {
   public static final BuilderCodec<PatrolPathMarkerEntity> CODEC;
   @Nullable
   private UUID pathId;
   private String pathName;
   private int order;
   private double pauseTime;
   private float observationAngle;
   private short tempPathLength;
   private IPrefabPath parentPath;

   @Nullable
   public static ComponentType<EntityStore, PatrolPathMarkerEntity> getComponentType() {
      return EntityModule.get().getComponentType(PatrolPathMarkerEntity.class);
   }

   public PatrolPathMarkerEntity() {
   }

   public PatrolPathMarkerEntity(World world) {
      super(world);
   }

   public void setParentPath(IPrefabPath parentPath) {
      this.parentPath = parentPath;
   }

   @Nullable
   public UUID getPathId() {
      return this.pathId;
   }

   public void setPathId(UUID pathId) {
      this.pathId = pathId;
   }

   public String getPathName() {
      return this.pathName;
   }

   public void setPathName(String pathName) {
      this.pathName = pathName;
   }

   @Nonnull
   public static String generateDisplayName(int worldgenId, PatrolPathMarkerEntity patrolPathMarkerEntity) {
      return String.format("%s.%s (%s) #%s [Wait %ss] <Rotate %.2fdeg>", worldgenId, patrolPathMarkerEntity.pathId, patrolPathMarkerEntity.pathName, patrolPathMarkerEntity.order, patrolPathMarkerEntity.pauseTime, patrolPathMarkerEntity.observationAngle * 57.295776F);
   }

   public short getTempPathLength() {
      return this.tempPathLength;
   }

   public void initialise(@Nonnull UUID id, @Nonnull String pathName, int index, double pauseTime, float observationAngle, int worldGenId, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.pathId = id;
      this.pathName = pathName;
      WorldPathData worldPathData = (WorldPathData)componentAccessor.getResource(WorldPathData.getResourceType());
      this.parentPath = worldPathData.getOrConstructPrefabPath(worldGenId, this.pathId, pathName, PatrolPath::new);
      this.pauseTime = pauseTime;
      this.observationAngle = observationAngle;
      if (index < 0) {
         this.order = this.parentPath.registerNewWaypoint(this, worldGenId);
      } else {
         this.order = index;
         this.parentPath.registerNewWaypointAt(index, this, worldGenId);
      }

      this.tempPathLength = (short)this.parentPath.length();
   }

   public IPath<IPrefabPathWaypoint> getParentPath() {
      return this.parentPath;
   }

   public boolean isCollidable() {
      return false;
   }

   public boolean isHiddenFromLivingEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = (Player)componentAccessor.getComponent(targetRef, Player.getComponentType());
      return playerComponent == null || playerComponent.getGameMode() != GameMode.Creative;
   }

   public int getOrder() {
      return this.order;
   }

   public void setOrder(int order) {
      this.order = order;
      this.markNeedsSave();
   }

   public double getPauseTime() {
      return this.pauseTime;
   }

   public void setPauseTime(double pauseTime) {
      this.pauseTime = pauseTime;
      this.markNeedsSave();
   }

   public float getObservationAngle() {
      return this.observationAngle;
   }

   public void onReplaced() {
      this.pathId = null;
      this.remove();
   }

   public void setObservationAngle(float observationAngle) {
      this.observationAngle = observationAngle;
      this.markNeedsSave();
   }

   @Nonnull
   public Vector3d getWaypointPosition(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> ref = this.getReference();

      assert ref != null && ref.isValid() : "Entity reference is null or invalid";

      TransformComponent transformComponent = (TransformComponent)componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getPosition();
   }

   @Nonnull
   public Vector3f getWaypointRotation(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> ref = this.getReference();

      assert ref != null && ref.isValid() : "Entity reference is null or invalid";

      TransformComponent transformComponent = (TransformComponent)componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return transformComponent.getRotation();
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.pathId);
      return "PatrolPathMarkerEntity{pathId=" + var10000 + ", path='" + this.pathName + "', order=" + this.order + ", pauseTime=" + this.pauseTime + ", observationAngle=" + this.observationAngle + ", tempPathLength=" + this.tempPathLength + ", parentPath=" + String.valueOf(this.parentPath) + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PatrolPathMarkerEntity.class, PatrolPathMarkerEntity::new, Entity.CODEC).append(new KeyedCodec("PathId", Codec.UUID_BINARY), (patrolPathMarkerEntity, uuid) -> patrolPathMarkerEntity.pathId = uuid, (patrolPathMarkerEntity) -> patrolPathMarkerEntity.pathId).setVersionRange(5, 5).add()).append(new KeyedCodec("PathName", Codec.STRING), (patrolPathMarkerEntity, s) -> patrolPathMarkerEntity.pathName = s, (patrolPathMarkerEntity) -> patrolPathMarkerEntity.pathName).setVersionRange(5, 5).add()).append(new KeyedCodec("Path", Codec.STRING), (patrolPathMarkerEntity, s) -> patrolPathMarkerEntity.pathName = s, (patrolPathMarkerEntity) -> patrolPathMarkerEntity.pathName).setVersionRange(0, 4).add()).addField(new KeyedCodec("PathLength", Codec.INTEGER), (patrolPathMarkerEntity, i) -> patrolPathMarkerEntity.tempPathLength = i.shortValue(), (patrolPathMarkerEntity) -> patrolPathMarkerEntity.parentPath != null ? patrolPathMarkerEntity.parentPath.length() : patrolPathMarkerEntity.tempPathLength)).addField(new KeyedCodec("Order", Codec.INTEGER), (patrolPathMarkerEntity, i) -> patrolPathMarkerEntity.order = i, (patrolPathMarkerEntity) -> patrolPathMarkerEntity.order)).addField(new KeyedCodec("PauseTime", Codec.DOUBLE), (patrolPathMarkerEntity, d) -> patrolPathMarkerEntity.pauseTime = d, (patrolPathMarkerEntity) -> patrolPathMarkerEntity.pauseTime)).addField(new KeyedCodec("ObsvAngle", Codec.DOUBLE), (patrolPathMarkerEntity, d) -> patrolPathMarkerEntity.observationAngle = d.floatValue(), (patrolPathMarkerEntity) -> (double)patrolPathMarkerEntity.observationAngle)).build();
   }
}
