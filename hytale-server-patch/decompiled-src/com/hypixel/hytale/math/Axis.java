package com.hypixel.hytale.math;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public enum Axis {
   X(new Vector3i(1, 0, 0)),
   Y(new Vector3i(0, 1, 0)),
   Z(new Vector3i(0, 0, 1));

   private final Vector3i direction;

   private Axis(@Nonnull final Vector3i direction) {
      this.direction = direction;
   }

   @Nonnull
   public Vector3i getDirection() {
      return this.direction.clone();
   }

   public void rotate(@Nonnull Vector3i vector, int angle) {
      if (angle < 0) {
         angle = Math.floorMod(angle, 360);
      }

      for(int i = angle; i > 0; i -= 90) {
         this.rotate(vector);
      }

   }

   public void rotate(@Nonnull Vector3d vector, int angle) {
      if (angle < 0) {
         angle = Math.floorMod(angle, 360);
      }

      for(int i = angle; i > 0; i -= 90) {
         this.rotate(vector);
      }

   }

   public void rotate(@Nonnull Vector3i vector) {
      switch (this.ordinal()) {
         case 0 -> vector.assign(vector.getX(), -vector.getZ(), vector.getY());
         case 1 -> vector.assign(vector.getZ(), vector.getY(), -vector.getX());
         case 2 -> vector.assign(-vector.getY(), vector.getX(), vector.getZ());
      }

   }

   public void rotate(@Nonnull Vector3d vector) {
      switch (this.ordinal()) {
         case 0 -> vector.assign(vector.getX(), -vector.getZ(), vector.getY());
         case 1 -> vector.assign(vector.getZ(), vector.getY(), -vector.getX());
         case 2 -> vector.assign(-vector.getY(), vector.getX(), vector.getZ());
      }

   }

   public void flip(@Nonnull Vector3i vector) {
      switch (this.ordinal()) {
         case 0 -> vector.assign(-vector.getX(), vector.getY(), vector.getZ());
         case 1 -> vector.assign(vector.getX(), -vector.getY(), vector.getZ());
         case 2 -> vector.assign(vector.getX(), vector.getY(), -vector.getZ());
      }

   }

   public void flip(@Nonnull Vector3d vector) {
      switch (this.ordinal()) {
         case 0 -> vector.assign(-vector.getX(), vector.getY(), vector.getZ());
         case 1 -> vector.assign(vector.getX(), -vector.getY(), vector.getZ());
         case 2 -> vector.assign(vector.getX(), vector.getY(), -vector.getZ());
      }

   }

   public void flipRotation(@Nonnull Vector3f rotation) {
      switch (this.ordinal()) {
         case 0 -> rotation.setYaw(-rotation.getYaw());
         case 1 -> rotation.setPitch(-rotation.getPitch());
         case 2 -> rotation.setYaw(3.1415927F - rotation.getYaw());
      }

   }
}
