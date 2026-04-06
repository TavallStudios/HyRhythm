package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionWanderInRect;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import javax.annotation.Nonnull;

public class BodyMotionWanderInRect extends BodyMotionWanderBase {
   public static final int LEFT = 1;
   public static final int RIGHT = 2;
   public static final int BOTTOM = 4;
   public static final int TOP = 8;
   public static final int VERTICAL_MASK = 12;
   public static final int HORIZONTAL_MASK = 3;
   protected final double width;
   protected final double depth;
   protected final double halfWidth;
   protected final double halfDepth;

   public BodyMotionWanderInRect(@Nonnull BuilderBodyMotionWanderInRect builder, @Nonnull BuilderSupport builderSupport) {
      super(builder, builderSupport);
      this.width = builder.getWidth();
      this.halfWidth = this.width / 2.0;
      this.depth = builder.getDepth();
      this.halfDepth = this.depth / 2.0;
   }

   protected double constrainMove(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull Vector3d probePosition, @Nonnull Vector3d targetPosition, double moveDist, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      NPCEntity npcComponent = (NPCEntity)componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      Vector3d leash = npcComponent.getLeashPoint();
      double leashX = leash.getX();
      double leashZ = leash.getZ();
      double endX = targetPosition.x - leashX;
      double endZ = targetPosition.z - leashZ;
      int endCode = this.sectorCode(endX, endZ);
      if (endCode == 0) {
         return moveDist;
      } else {
         double startX = probePosition.x - leashX;
         double startZ = probePosition.z - leashZ;
         int startCode = this.sectorCode(startX, startZ);
         if (startCode != 0) {
            if ((startCode & endCode) != 0) {
               return this.distanceSquared(endX, endZ, endCode) < this.distanceSquared(startX, startZ, startCode) ? moveDist : 0.0;
            } else {
               int or = startCode | endCode;
               if ((or & 12) != 12 && (or & 3) != 3) {
                  return this.distanceSquared(endX, endZ, endCode) < this.distanceSquared(startX, startZ, startCode) ? moveDist : 0.0;
               } else {
                  return 0.0;
               }
            }
         } else {
            double dx = endX - startX;
            double dz = endZ - startZ;
            double scaleX;
            if ((endCode & 1) == 1) {
               scaleX = (-this.halfWidth - startX) / dx;
            } else if ((endCode & 2) == 2) {
               scaleX = (this.halfWidth - startX) / dx;
            } else {
               scaleX = 1.0;
            }

            double scaleZ;
            if ((endCode & 4) == 4) {
               scaleZ = (-this.halfDepth - startZ) / dz;
            } else if ((endCode & 8) == 8) {
               scaleZ = (this.halfDepth - startZ) / dz;
            } else {
               scaleZ = 1.0;
            }

            if (!(scaleX < 0.0) && !(scaleX > 1.0)) {
               if (!(scaleZ < 0.0) && !(scaleZ > 1.0)) {
                  return moveDist * Math.min(scaleX, scaleZ);
               } else {
                  throw new IllegalArgumentException("WanderInRect: Constrained Z outside of allowed range!");
               }
            } else {
               throw new IllegalArgumentException("WanderInRect: Constrained X outside of allowed range!");
            }
         }
      }
   }

   protected int sectorCode(double x, double z) {
      int code = 0;
      if (x < -this.halfWidth) {
         code |= 1;
      } else if (x > this.halfWidth) {
         code |= 2;
      }

      if (z < -this.halfDepth) {
         code |= 4;
      } else if (z > this.halfDepth) {
         code |= 8;
      }

      return code;
   }

   protected double distanceSquared(double x, double z, int sector) {
      double var10000;
      switch (sector) {
         case 1:
            var10000 = (x + this.halfWidth) * (x + this.halfWidth);
            break;
         case 2:
            var10000 = (x - this.halfWidth) * (x - this.halfWidth);
            break;
         case 3:
         case 7:
         default:
            var10000 = 0.0;
            break;
         case 4:
            var10000 = (z + this.halfDepth) * (z + this.halfDepth);
            break;
         case 5:
            var10000 = (x + this.halfWidth) * (x + this.halfWidth) + (z + this.halfDepth) * (z + this.halfDepth);
            break;
         case 6:
            var10000 = (x - this.halfWidth) * (x - this.halfWidth) + (z + this.halfDepth) * (z + this.halfDepth);
            break;
         case 8:
            var10000 = (z - this.halfDepth) * (z - this.halfDepth);
            break;
         case 9:
            var10000 = (x + this.halfWidth) * (x + this.halfWidth) + (z - this.halfDepth) * (z - this.halfDepth);
            break;
         case 10:
            var10000 = (x - this.halfWidth) * (x - this.halfWidth) + (z - this.halfDepth) * (z - this.halfDepth);
      }

      return var10000;
   }
}
