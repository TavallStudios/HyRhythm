package com.hypixel.hytale.server.core.modules.entitystats.asset.condition;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class GlidingCondition extends Condition {
   @Nonnull
   public static final BuilderCodec<GlidingCondition> CODEC;

   protected GlidingCondition() {
   }

   public GlidingCondition(boolean inverse) {
      super(inverse);
   }

   public boolean eval0(@Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Ref<EntityStore> ref, @Nonnull Instant currentTime) {
      MovementStatesComponent movementStatesComponent = (MovementStatesComponent)componentAccessor.getComponent(ref, MovementStatesComponent.getComponentType());

      assert movementStatesComponent != null;

      return movementStatesComponent.getMovementStates().gliding;
   }

   @Nonnull
   public String toString() {
      return "GlidingCondition{} " + super.toString();
   }

   static {
      CODEC = BuilderCodec.builder(GlidingCondition.class, GlidingCondition::new, Condition.BASE_CODEC).build();
   }
}
