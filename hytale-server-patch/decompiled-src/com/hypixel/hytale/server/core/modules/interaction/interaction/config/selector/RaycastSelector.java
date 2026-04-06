package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RaycastSelector extends SelectorType {
   public static final BuilderCodec<RaycastSelector> CODEC;
   protected Vector3d offset;
   protected int distance;
   protected boolean ignoreFluids;
   protected boolean ignoreEmptyCollisionMaterial;
   @Nullable
   protected String blockTag;
   protected int blockTagIndex;

   public RaycastSelector() {
      this.offset = Vector3d.ZERO;
      this.distance = 30;
      this.ignoreFluids = false;
      this.ignoreEmptyCollisionMaterial = false;
      this.blockTagIndex = -2147483648;
   }

   @Nonnull
   public Selector newSelector() {
      return new RuntimeSelector();
   }

   @Nonnull
   public Vector3f getOffset() {
      return new Vector3f((float)this.offset.x, (float)this.offset.y, (float)this.offset.z);
   }

   @Nonnull
   public Vector3d selectTargetPosition(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attacker) {
      TransformComponent transformComponent = (TransformComponent)commandBuffer.getComponent(attacker, TransformComponent.getComponentType());
      Vector3d position = transformComponent.getPosition();
      if (this.offset.x != 0.0 || this.offset.y != 0.0 || this.offset.z != 0.0) {
         position = this.offset.clone();
         HeadRotation headRotation = (HeadRotation)commandBuffer.getComponent(attacker, HeadRotation.getComponentType());
         position.rotateY(headRotation.getRotation().getYaw());
         position.add(transformComponent.getPosition());
      }

      return position;
   }

   public com.hypixel.hytale.protocol.Selector toPacket() {
      return new com.hypixel.hytale.protocol.RaycastSelector(this.getOffset(), this.distance, this.blockTagIndex, this.ignoreFluids, this.ignoreEmptyCollisionMaterial);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RaycastSelector.class, RaycastSelector::new, BASE_CODEC).appendInherited(new KeyedCodec("Offset", Vector3d.CODEC), (o, i) -> o.offset = i, (o) -> o.offset, (o, p) -> o.offset = p.offset).documentation("The offset of the area to search for targets in.").add()).appendInherited(new KeyedCodec("Distance", Codec.INTEGER), (o, i) -> o.distance = i, (o) -> o.distance, (o, p) -> o.distance = p.distance).documentation("The max search distance for the raycast").add()).appendInherited(new KeyedCodec("IgnoreFluids", Codec.BOOLEAN), (o, i) -> o.ignoreFluids = i, (o) -> o.ignoreFluids, (o, p) -> o.ignoreFluids = p.ignoreFluids).add()).appendInherited(new KeyedCodec("IgnoreEmptyCollisionMaterial", Codec.BOOLEAN), (o, i) -> o.ignoreEmptyCollisionMaterial = i, (o) -> o.ignoreEmptyCollisionMaterial, (o, p) -> o.ignoreEmptyCollisionMaterial = p.ignoreEmptyCollisionMaterial).add()).appendInherited(new KeyedCodec("BlockTag", Codec.STRING), (o, i) -> o.blockTag = i, (o) -> o.blockTag, (o, p) -> o.blockTag = p.blockTag).documentation("The required tag for the block to have to match for the raycast to hit them").add()).afterDecode((o) -> {
         if (o.blockTag != null) {
            o.blockTagIndex = AssetRegistry.getOrCreateTagIndex(o.blockTag);
         }

      })).build();
   }

   private static class Result {
      public Ref<EntityStore> match;
      public double distance = 1.7976931348623157E308;
      @Nonnull
      public Vector4d hitPosition = new Vector4d();
   }

   private class RuntimeSelector implements Selector {
      private final Result bestMatch = new Result();
      private final Vector2d minMax = new Vector2d();
      @Nullable
      private Vector3i blockPosition;

      public void tick(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, float time, float runTime) {
         Vector3d position = RaycastSelector.this.selectTargetPosition(commandBuffer, ref);
         HeadRotation headRotation = (HeadRotation)commandBuffer.getComponent(ref, HeadRotation.getComponentType());
         Vector3d direction = new Vector3d(headRotation.getRotation().getYaw(), headRotation.getRotation().getPitch());
         IntSet blockTags = RaycastSelector.this.blockTag == null ? null : BlockType.getAssetMap().getIndexesForTag(RaycastSelector.this.blockTagIndex);
         if (SelectInteraction.SHOW_VISUAL_DEBUG) {
            Vector3d dir = direction.clone().scale((double)RaycastSelector.this.distance);
            com.hypixel.hytale.math.vector.Vector3f color = new com.hypixel.hytale.math.vector.Vector3f((float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 10L), (float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 11L), (float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 12L));
            DebugUtils.addArrow(((EntityStore)commandBuffer.getExternalData()).getWorld(), position, dir, color, 5.0F, true);
         }

         this.blockPosition = TargetUtil.getTargetBlock(((EntityStore)commandBuffer.getExternalData()).getWorld(), (id, fluidId) -> {
            if (id == 0) {
               return false;
            } else {
               if (RaycastSelector.this.ignoreFluids || RaycastSelector.this.ignoreEmptyCollisionMaterial) {
                  BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(id);
                  if (RaycastSelector.this.ignoreFluids && blockType.getMaterial() == BlockMaterial.Empty && fluidId != 0) {
                     return false;
                  }

                  if (RaycastSelector.this.ignoreEmptyCollisionMaterial && blockType.getMaterial() == BlockMaterial.Empty) {
                     return false;
                  }
               }

               return blockTags == null || blockTags.contains(id);
            }
         }, position.x, position.y, position.z, direction.x, direction.y, direction.z, (double)RaycastSelector.this.distance);
         Vector3d searchPosition = new Vector3d(position.x + direction.x * (double)RaycastSelector.this.distance * 0.5, position.y + direction.y * (double)RaycastSelector.this.distance * 0.5, position.z + direction.z * (double)RaycastSelector.this.distance * 0.5);
         Selector.selectNearbyEntities(commandBuffer, searchPosition, (double)RaycastSelector.this.distance * 0.6, (entityRef) -> {
            BoundingBox boundingBox = (BoundingBox)commandBuffer.getComponent(entityRef, BoundingBox.getComponentType());
            if (boundingBox != null) {
               TransformComponent transform = (TransformComponent)commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());
               Vector3d ePos = transform.getPosition();
               if (CollisionMath.intersectRayAABB(position, direction, ePos.getX(), ePos.getY(), ePos.getZ(), boundingBox.getBoundingBox(), this.minMax)) {
                  double hitPosX = position.x + direction.x * this.minMax.x;
                  double hitPosY = position.y + direction.y * this.minMax.x;
                  double hitPosZ = position.z + direction.z * this.minMax.x;
                  double matchDistance = position.distanceSquaredTo(hitPosX, hitPosY, hitPosZ);
                  if (!(matchDistance >= this.bestMatch.distance)) {
                     this.bestMatch.match = entityRef;
                     this.bestMatch.distance = matchDistance;
                     this.bestMatch.hitPosition.assign(hitPosX, hitPosY, hitPosZ, 0.0);
                  }
               }
            }
         }, (e) -> !e.equals(ref));
         if (this.bestMatch.match != null && this.blockPosition != null) {
            double blockDistance = position.distanceSquaredTo((double)this.blockPosition.x + 0.5, (double)this.blockPosition.y + 0.5, (double)this.blockPosition.z + 0.5);
            if (!(blockDistance < this.bestMatch.distance)) {
               this.blockPosition = null;
            }
         }
      }

      public void selectTargetEntities(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attacker, @Nonnull BiConsumer<Ref<EntityStore>, Vector4d> consumer, Predicate<Ref<EntityStore>> filter) {
         if (this.blockPosition == null && this.bestMatch.match != null) {
            if (this.bestMatch.match.isValid()) {
               consumer.accept(this.bestMatch.match, this.bestMatch.hitPosition);
            }
         }
      }

      public void selectTargetBlocks(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attacker, @Nonnull TriIntConsumer consumer) {
         if (this.blockPosition != null) {
            consumer.accept(this.blockPosition.x, this.blockPosition.y, this.blockPosition.z);
         }

      }
   }
}
