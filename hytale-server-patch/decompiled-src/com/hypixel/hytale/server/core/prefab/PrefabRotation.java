package com.hypixel.hytale.server.core.prefab;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector3l;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockRotationUtil;
import javax.annotation.Nonnull;

public enum PrefabRotation {
   ROTATION_0(Rotation.None, new RotationExecutor_0()),
   ROTATION_90(Rotation.Ninety, new RotationExecutor_90()),
   ROTATION_180(Rotation.OneEighty, new RotationExecutor_180()),
   ROTATION_270(Rotation.TwoSeventy, new RotationExecutor_270());

   public static final PrefabRotation[] VALUES = values();
   public static final String PREFIX = "ROTATION_";
   private final Rotation rotation;
   private final RotationExecutor executor;

   @Nonnull
   public static PrefabRotation fromRotation(@Nonnull Rotation rotation) {
      PrefabRotation var10000;
      switch (rotation) {
         case None -> var10000 = ROTATION_0;
         case Ninety -> var10000 = ROTATION_90;
         case OneEighty -> var10000 = ROTATION_180;
         case TwoSeventy -> var10000 = ROTATION_270;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public static PrefabRotation valueOfExtended(@Nonnull String s) {
      return s.startsWith("ROTATION_") ? valueOf(s) : valueOf("ROTATION_" + s);
   }

   private PrefabRotation(Rotation rotation, RotationExecutor executor) {
      this.rotation = rotation;
      this.executor = executor;
   }

   public PrefabRotation add(@Nonnull PrefabRotation other) {
      int val = this.rotation.getDegrees() + other.rotation.getDegrees();
      return VALUES[val % 360 / 90];
   }

   public void rotate(@Nonnull Vector3d v) {
      double x = v.x;
      double z = v.z;
      v.x = this.executor.rotateDoubleX(x, z);
      v.z = this.executor.rotateDoubleZ(x, z);
   }

   public void rotate(@Nonnull Vector3i v) {
      int x = v.x;
      int z = v.z;
      v.x = this.executor.rotateIntX(x, z);
      v.z = this.executor.rotateIntZ(x, z);
   }

   public void rotate(@Nonnull Vector3l v) {
      long x = v.x;
      long z = v.z;
      v.x = this.executor.rotateLongX(x, z);
      v.z = this.executor.rotateLongZ(x, z);
   }

   public int getX(int x, int z) {
      return this.executor.rotateIntX(x, z);
   }

   public int getZ(int x, int z) {
      return this.executor.rotateIntZ(x, z);
   }

   public float getYaw() {
      return this.executor.getYaw();
   }

   public int getRotation(int rotation) {
      if (this.rotation == Rotation.None) {
         return rotation;
      } else {
         RotationTuple inRotation = RotationTuple.get(rotation);
         return RotationTuple.of(inRotation.yaw().add(this.rotation), inRotation.pitch(), inRotation.roll()).index();
      }
   }

   public int getFiller(int filler) {
      return this.rotation == Rotation.None ? filler : BlockRotationUtil.getRotatedFiller(filler, Axis.Y, this.rotation);
   }

   private static class RotationExecutor_0 implements RotationExecutor {
      public float getYaw() {
         return 0.0F;
      }

      public int rotateIntX(int x, int z) {
         return x;
      }

      public long rotateLongX(long x, long z) {
         return x;
      }

      public double rotateDoubleX(double x, double z) {
         return x;
      }

      public int rotateIntZ(int x, int z) {
         return z;
      }

      public long rotateLongZ(long x, long z) {
         return z;
      }

      public double rotateDoubleZ(double x, double z) {
         return z;
      }
   }

   private static class RotationExecutor_90 implements RotationExecutor {
      public float getYaw() {
         return -1.5707964F;
      }

      public int rotateIntX(int x, int z) {
         return z;
      }

      public long rotateLongX(long x, long z) {
         return z;
      }

      public double rotateDoubleX(double x, double z) {
         return z;
      }

      public int rotateIntZ(int x, int z) {
         return -x;
      }

      public long rotateLongZ(long x, long z) {
         return -x;
      }

      public double rotateDoubleZ(double x, double z) {
         return -x;
      }
   }

   private static class RotationExecutor_180 implements RotationExecutor {
      public float getYaw() {
         return -3.1415927F;
      }

      public int rotateIntX(int x, int z) {
         return -x;
      }

      public long rotateLongX(long x, long z) {
         return -x;
      }

      public double rotateDoubleX(double x, double z) {
         return -x;
      }

      public int rotateIntZ(int x, int z) {
         return -z;
      }

      public long rotateLongZ(long x, long z) {
         return -z;
      }

      public double rotateDoubleZ(double x, double z) {
         return -z;
      }
   }

   private static class RotationExecutor_270 implements RotationExecutor {
      public float getYaw() {
         return -4.712389F;
      }

      public int rotateIntX(int x, int z) {
         return -z;
      }

      public long rotateLongX(long x, long z) {
         return -z;
      }

      public double rotateDoubleX(double x, double z) {
         return -z;
      }

      public int rotateIntZ(int x, int z) {
         return x;
      }

      public long rotateLongZ(long x, long z) {
         return x;
      }

      public double rotateDoubleZ(double x, double z) {
         return x;
      }
   }

   private interface RotationExecutor {
      float getYaw();

      int rotateIntX(int var1, int var2);

      long rotateLongX(long var1, long var3);

      double rotateDoubleX(double var1, double var3);

      int rotateIntZ(int var1, int var2);

      long rotateLongZ(long var1, long var3);

      double rotateDoubleZ(double var1, double var3);
   }
}
