package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents;

import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemory;
import com.hypixel.hytale.builtin.npccombatactionevaluator.memory.TargetMemorySystems;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.role.Role;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatTargetCollector implements ISensorEntityCollector {
   @Nonnull
   private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   @Nullable
   private Role role;
   @Nullable
   private TargetMemory targetMemory;
   private double closestHostileDistanceSquared = 1.7976931348623157E308;

   public void registerWithSupport(@Nonnull Role role) {
      role.getWorldSupport().requireAttitudeCache();
   }

   public void init(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.targetMemory = (TargetMemory)componentAccessor.getComponent(ref, TargetMemory.getComponentType());
      this.role = role;
   }

   public void collectMatching(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (this.targetMemory != null) {
         Attitude attitude = this.role.getWorldSupport().getAttitude(ref, targetRef, componentAccessor);
         switch (attitude) {
            case IGNORE:
            case NEUTRAL:
            default:
               break;
            case HOSTILE:
               Int2FloatOpenHashMap hostiles = this.targetMemory.getKnownHostiles();
               if (hostiles.put(targetRef.getIndex(), this.targetMemory.getRememberFor()) <= 0.0F) {
                  this.targetMemory.getKnownHostilesList().add(targetRef);
                  HytaleLogger.Api context = TargetMemorySystems.LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log("%s: Registered new hostile %s", ref, targetRef);
                  }
               }

               TransformComponent transformComponent = (TransformComponent)componentAccessor.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

               assert transformComponent != null;

               Vector3d selfPos = transformComponent.getPosition();
               TransformComponent targetTransformComponent = (TransformComponent)componentAccessor.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

               assert targetTransformComponent != null;

               Vector3d targetPos = targetTransformComponent.getPosition();
               double distanceSquared = selfPos.distanceSquaredTo(targetPos);
               if (distanceSquared < this.closestHostileDistanceSquared) {
                  this.targetMemory.setClosestHostile(targetRef);
                  this.closestHostileDistanceSquared = distanceSquared;
               }
               break;
            case FRIENDLY:
            case REVERED:
               Int2FloatOpenHashMap friendlies = this.targetMemory.getKnownFriendlies();
               if (friendlies.put(targetRef.getIndex(), this.targetMemory.getRememberFor()) <= 0.0F) {
                  this.targetMemory.getKnownFriendliesList().add(targetRef);
                  HytaleLogger.Api context = TargetMemorySystems.LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log("%s: Registered new friendly %s", ref, targetRef);
                  }
               }
         }

      }
   }

   public void collectNonMatching(@Nonnull Ref<EntityStore> targetRef, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   public boolean terminateOnFirstMatch() {
      return this.targetMemory == null;
   }

   public void cleanup() {
      this.role = null;
      this.targetMemory = null;
      this.closestHostileDistanceSquared = 1.7976931348623157E308;
   }
}
