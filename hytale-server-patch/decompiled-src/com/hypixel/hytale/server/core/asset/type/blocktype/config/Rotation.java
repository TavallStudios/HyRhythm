package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum Rotation implements NetworkSerializable<com.hypixel.hytale.protocol.Rotation> {
   None(0, com.hypixel.hytale.protocol.Rotation.None, Axis.Z, Vector3i.NEG_Z),
   Ninety(90, com.hypixel.hytale.protocol.Rotation.Ninety, Axis.X, Vector3i.NEG_X),
   OneEighty(180, com.hypixel.hytale.protocol.Rotation.OneEighty, Axis.Z, Vector3i.POS_Z),
   TwoSeventy(270, com.hypixel.hytale.protocol.Rotation.TwoSeventy, Axis.X, Vector3i.POS_X);

   public static final Rotation[] VALUES = values();
   public static final Rotation[] NORMAL = new Rotation[]{None, Ninety, OneEighty, TwoSeventy};
   public static final Codec<Rotation> CODEC = new EnumCodec<Rotation>(Rotation.class);
   private final int degrees;
   private final com.hypixel.hytale.protocol.Rotation packet;
   private final Axis axisOfAlignment;
   private final Vector3i axisDirection;

   private Rotation(int degrees, com.hypixel.hytale.protocol.Rotation packet, Axis axisOfAlignment, Vector3i axisDirection) {
      this.degrees = degrees;
      this.packet = packet;
      this.axisOfAlignment = axisOfAlignment;
      this.axisDirection = axisDirection;
   }

   public com.hypixel.hytale.protocol.Rotation toPacket() {
      return this.packet;
   }

   public int getDegrees() {
      return this.degrees;
   }

   public double getRadians() {
      return Math.toRadians((double)this.degrees);
   }

   public Axis getAxisOfAlignment() {
      return this.axisOfAlignment;
   }

   public Vector3i getAxisDirection() {
      return this.axisDirection;
   }

   @Nonnull
   public Rotation flip() {
      return this.add(OneEighty);
   }

   @Nonnull
   public Rotation flip(Axis axis) {
      return this.axisOfAlignment != axis ? this : this.flip();
   }

   @Nonnull
   public Rotation subtract(@Nullable Rotation rotation) {
      return rotation == null ? this : ofDegrees(this.degrees - rotation.degrees);
   }

   @Nonnull
   public Rotation add(@Nullable Rotation rotation) {
      return rotation == null ? this : ofDegrees(this.degrees + rotation.degrees);
   }

   public static Rotation add(@Nullable Rotation a, Rotation b) {
      return a == null ? b : a.add(b);
   }

   @Nonnull
   public Vector3i rotatePitch(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      return this.rotateX(in, out);
   }

   @Nonnull
   public Vector3f rotatePitch(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      return this.rotateX(in, out);
   }

   public int rotateX(int filler) {
      int var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = filler;
         case 1 -> var10000 = FillerBlockUtil.pack(FillerBlockUtil.unpackX(filler), -FillerBlockUtil.unpackZ(filler), FillerBlockUtil.unpackY(filler));
         case 2 -> var10000 = FillerBlockUtil.pack(FillerBlockUtil.unpackX(filler), -FillerBlockUtil.unpackY(filler), -FillerBlockUtil.unpackZ(filler));
         case 3 -> var10000 = FillerBlockUtil.pack(FillerBlockUtil.unpackX(filler), FillerBlockUtil.unpackZ(filler), -FillerBlockUtil.unpackY(filler));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3i rotateX(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      Vector3i var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.x, -in.z, in.y);
         case 2 -> var10000 = out.assign(in.x, -in.y, -in.z);
         case 3 -> var10000 = out.assign(in.x, in.z, -in.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3f rotateX(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      Vector3f var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.x, -in.z, in.y);
         case 2 -> var10000 = out.assign(in.x, -in.y, -in.z);
         case 3 -> var10000 = out.assign(in.x, in.z, -in.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3d rotateX(@Nonnull Vector3d in, @Nonnull Vector3d out) {
      Vector3d var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.x, -in.z, in.y);
         case 2 -> var10000 = out.assign(in.x, -in.y, -in.z);
         case 3 -> var10000 = out.assign(in.x, in.z, -in.y);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3i rotateYaw(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      return this.rotateY(in, out);
   }

   @Nonnull
   public Vector3f rotateYaw(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      return this.rotateY(in, out);
   }

   public int rotateY(int filler) {
      int var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = filler;
         case 1 -> var10000 = FillerBlockUtil.pack(FillerBlockUtil.unpackZ(filler), FillerBlockUtil.unpackY(filler), -FillerBlockUtil.unpackX(filler));
         case 2 -> var10000 = FillerBlockUtil.pack(-FillerBlockUtil.unpackX(filler), FillerBlockUtil.unpackY(filler), -FillerBlockUtil.unpackZ(filler));
         case 3 -> var10000 = FillerBlockUtil.pack(-FillerBlockUtil.unpackZ(filler), FillerBlockUtil.unpackY(filler), FillerBlockUtil.unpackX(filler));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3i rotateY(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      Vector3i var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.z, in.y, -in.x);
         case 2 -> var10000 = out.assign(-in.x, in.y, -in.z);
         case 3 -> var10000 = out.assign(-in.z, in.y, in.x);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3f rotateY(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      Vector3f var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.z, in.y, -in.x);
         case 2 -> var10000 = out.assign(-in.x, in.y, -in.z);
         case 3 -> var10000 = out.assign(-in.z, in.y, in.x);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3d rotateY(@Nonnull Vector3d in, @Nonnull Vector3d out) {
      Vector3d var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(in.z, in.y, -in.x);
         case 2 -> var10000 = out.assign(-in.x, in.y, -in.z);
         case 3 -> var10000 = out.assign(-in.z, in.y, in.x);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   private Vector3i rotateRoll(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      return this.rotateZ(in, out);
   }

   @Nonnull
   private Vector3f rotateRoll(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      return this.rotateZ(in, out);
   }

   public int rotateZ(int filler) {
      int var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = filler;
         case 1 -> var10000 = FillerBlockUtil.pack(-FillerBlockUtil.unpackY(filler), FillerBlockUtil.unpackX(filler), FillerBlockUtil.unpackZ(filler));
         case 2 -> var10000 = FillerBlockUtil.pack(-FillerBlockUtil.unpackX(filler), -FillerBlockUtil.unpackY(filler), FillerBlockUtil.unpackZ(filler));
         case 3 -> var10000 = FillerBlockUtil.pack(FillerBlockUtil.unpackY(filler), -FillerBlockUtil.unpackX(filler), FillerBlockUtil.unpackZ(filler));
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3i rotateZ(@Nonnull Vector3i in, @Nonnull Vector3i out) {
      Vector3i var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(-in.y, in.x, in.z);
         case 2 -> var10000 = out.assign(-in.x, -in.y, in.z);
         case 3 -> var10000 = out.assign(in.y, -in.x, in.z);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3f rotateZ(@Nonnull Vector3f in, @Nonnull Vector3f out) {
      Vector3f var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(-in.y, in.x, in.z);
         case 2 -> var10000 = out.assign(-in.x, -in.y, in.z);
         case 3 -> var10000 = out.assign(in.y, -in.x, in.z);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public Vector3d rotateZ(@Nonnull Vector3d in, @Nonnull Vector3d out) {
      Vector3d var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = out.assign(in);
         case 1 -> var10000 = out.assign(-in.y, in.x, in.z);
         case 2 -> var10000 = out.assign(-in.x, -in.y, in.z);
         case 3 -> var10000 = out.assign(in.y, -in.x, in.z);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public static Rotation ofDegrees(int degrees) {
      degrees = Math.floorMod(degrees, 360);
      Rotation var10000;
      switch (degrees) {
         case 0 -> var10000 = None;
         case 90 -> var10000 = Ninety;
         case 180 -> var10000 = OneEighty;
         case 270 -> var10000 = TwoSeventy;
         default -> throw new IllegalArgumentException("Rotation degrees value " + degrees + " cannot be mapped to type Rotation");
      }

      return var10000;
   }

   public static Rotation closestOfDegrees(float degrees) {
      if (degrees < 0.0F) {
         degrees = degrees % 360.0F + 360.0F;
      }

      return VALUES[MathUtil.fastRound(degrees / 90.0F) % VALUES.length];
   }

   @Nonnull
   public static Rotation valueOf(@Nonnull com.hypixel.hytale.protocol.Rotation packet) {
      Rotation var10000;
      switch (packet) {
         case None -> var10000 = None;
         case Ninety -> var10000 = Ninety;
         case OneEighty -> var10000 = OneEighty;
         case TwoSeventy -> var10000 = TwoSeventy;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   @Nonnull
   public static Vector3i rotate(@Nonnull Vector3i vector3i, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch) {
      Vector3i rotated = vector3i.clone();
      rotationPitch.rotatePitch(rotated, rotated);
      rotationYaw.rotateYaw(rotated, rotated);
      return rotated;
   }

   @Nonnull
   public static Vector3i rotate(@Nonnull Vector3i vector3i, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      Vector3i rotated = rotate(vector3i, rotationYaw, rotationPitch);
      rotationRoll.rotateRoll(rotated, rotated);
      return rotated;
   }

   @Nonnull
   public static Vector3f rotate(@Nonnull Vector3f vector3f, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      Vector3f rotated = vector3f.clone();
      rotationPitch.rotatePitch(rotated, rotated);
      rotationYaw.rotateYaw(rotated, rotated);
      rotationRoll.rotateRoll(rotated, rotated);
      return rotated;
   }

   @Nonnull
   public static Vector3d rotate(@Nonnull Vector3d vector3d, @Nonnull Rotation rotationYaw, @Nonnull Rotation rotationPitch, @Nonnull Rotation rotationRoll) {
      Vector3d rotated = vector3d.clone();
      rotationPitch.rotateX(rotated, rotated);
      rotationYaw.rotateY(rotated, rotated);
      rotationRoll.rotateZ(rotated, rotated);
      return rotated;
   }
}
