package com.hypixel.hytale.server.core.entity;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import javax.annotation.Nonnull;

public class EntitySnapshot {
   @Nonnull
   private final Vector3d position = new Vector3d();
   @Nonnull
   private final Vector3f bodyRotation = new Vector3f();

   public EntitySnapshot() {
   }

   public EntitySnapshot(@Nonnull Vector3d position, @Nonnull Vector3f bodyRotation) {
      this.position.assign(position);
      this.bodyRotation.assign(bodyRotation);
   }

   public void init(@Nonnull Vector3d position, @Nonnull Vector3f bodyRotation) {
      this.position.assign(position);
      this.bodyRotation.assign(bodyRotation);
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   public Vector3f getBodyRotation() {
      return this.bodyRotation;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.position);
      return "EntitySnapshot{position=" + var10000 + ", bodyRotation=" + String.valueOf(this.bodyRotation) + "}";
   }
}
