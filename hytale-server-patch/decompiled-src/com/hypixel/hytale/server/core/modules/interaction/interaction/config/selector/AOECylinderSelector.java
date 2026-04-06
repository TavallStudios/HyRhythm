package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AOECylinderSelector extends AOECircleSelector {
   @Nonnull
   public static final BuilderCodec<AOECylinderSelector> CODEC;
   private final RuntimeSelector instance = new RuntimeSelector();
   protected float height;

   @Nonnull
   public Selector newSelector() {
      return this.instance;
   }

   public com.hypixel.hytale.protocol.Selector toPacket() {
      return new com.hypixel.hytale.protocol.AOECylinderSelector(this.range, this.height, this.getOffset());
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AOECylinderSelector.class, AOECylinderSelector::new, AOECircleSelector.CODEC).documentation("A selector that selects all entities within a given range and height.")).append(new KeyedCodec("Height", Codec.FLOAT), (aoeCircleEntitySelector, d) -> aoeCircleEntitySelector.height = d, (aoeCircleEntitySelector) -> aoeCircleEntitySelector.height).documentation("The height of the area to search for targets in from the entity position.").add()).build();
   }

   private class RuntimeSelector implements Selector {
      public void tick(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, float time, float runTime) {
         if (SelectInteraction.SHOW_VISUAL_DEBUG) {
            Vector3d position = AOECylinderSelector.this.selectTargetPosition(commandBuffer, ref);
            Vector3f color = new Vector3f((float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 10L), (float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 11L), (float)HashUtil.random((long)ref.getIndex(), (long)this.hashCode(), 12L));
            DebugUtils.addSphere(((EntityStore)commandBuffer.getExternalData()).getWorld(), position, color, (double)(AOECylinderSelector.this.range * 2.0F), 5.0F);
         }

      }

      public void selectTargetEntities(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, @Nonnull BiConsumer<Ref<EntityStore>, Vector4d> consumer, @Nullable Predicate<Ref<EntityStore>> filter) {
         Vector3d position = AOECylinderSelector.this.selectTargetPosition(commandBuffer, ref);
         Vector3d min = new Vector3d(position.x - (double)AOECylinderSelector.this.range, position.y, position.z - (double)AOECylinderSelector.this.range);
         Vector3d max = new Vector3d(position.x + (double)AOECylinderSelector.this.range, position.y + (double)AOECylinderSelector.this.height, position.z + (double)AOECylinderSelector.this.range);

         for(Ref<EntityStore> targetRef : TargetUtil.getAllEntitiesInBox(min, max, commandBuffer)) {
            if (targetRef.isValid() && !targetRef.equals(ref) && (filter == null || filter.test(targetRef))) {
               Archetype<EntityStore> archetype = commandBuffer.getArchetype(targetRef);
               boolean isDead = archetype.contains(DeathComponent.getComponentType());
               boolean isInvulnerable = archetype.contains(Invulnerable.getComponentType());
               if (!isDead && !isInvulnerable) {
                  TransformComponent targetEntityTransformComponent = (TransformComponent)commandBuffer.getComponent(targetRef, TransformComponent.getComponentType());

                  assert targetEntityTransformComponent != null;

                  Vector3d pos = targetEntityTransformComponent.getPosition();
                  double dx = pos.x - position.x;
                  double dy = pos.y - position.y;
                  double dz = pos.z - position.z;
                  if (dx * dx + dz * dz <= (double)(AOECylinderSelector.this.range * AOECylinderSelector.this.range) && dy <= (double)AOECylinderSelector.this.height && dy >= 0.0) {
                     consumer.accept(targetRef, new Vector4d(position.x, position.y, position.z, 1.0));
                  }
               }
            }
         }

      }

      public void selectTargetBlocks(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, @Nonnull TriIntConsumer consumer) {
         Vector3d position = AOECylinderSelector.this.selectTargetPosition(commandBuffer, ref);
         int xStart = MathUtil.floor((double)(-AOECylinderSelector.this.range));
         int yStart = 0;
         int zStart = MathUtil.floor((double)(-AOECylinderSelector.this.range));
         int xEnd = MathUtil.floor((double)AOECylinderSelector.this.range);
         int yEnd = MathUtil.floor((double)AOECylinderSelector.this.height);
         int zEnd = MathUtil.floor((double)AOECylinderSelector.this.range);
         float squaredRange = AOECylinderSelector.this.range * AOECylinderSelector.this.range;

         for(int x = xStart; x < xEnd; ++x) {
            for(int y = yStart; y < yEnd; ++y) {
               for(int z = zStart; z < zEnd; ++z) {
                  if ((float)(x * x + z * z) <= squaredRange) {
                     consumer.accept(MathUtil.floor(position.x + (double)x), MathUtil.floor(position.y + (double)y), MathUtil.floor(position.z + (double)z));
                  }
               }
            }
         }

      }
   }
}
