package com.hypixel.hytale.procedurallib.random;

import javax.annotation.Nonnull;

public class RotatedCoordinateRandomizer implements ICoordinateRandomizer {
   protected final ICoordinateRandomizer randomizer;
   protected final CoordinateRotator rotation;

   public RotatedCoordinateRandomizer(ICoordinateRandomizer randomizer, CoordinateRotator rotation) {
      this.randomizer = randomizer;
      this.rotation = rotation;
   }

   public double randomDoubleX(int seed, double x, double y) {
      double px = this.rotation.rotateX(x, y);
      double py = this.rotation.rotateY(x, y);
      return this.randomizer.randomDoubleX(seed, px, py);
   }

   public double randomDoubleY(int seed, double x, double y) {
      double px = this.rotation.rotateX(x, y);
      double py = this.rotation.rotateY(x, y);
      return this.randomizer.randomDoubleY(seed, px, py);
   }

   public double randomDoubleX(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleX(seed, px, py, pz);
   }

   public double randomDoubleY(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleY(seed, px, py, pz);
   }

   public double randomDoubleZ(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.randomizer.randomDoubleZ(seed, px, py, pz);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.randomizer);
      return "RotatedCoordinateRandomizer{randomizer=" + var10000 + ", rotation=" + String.valueOf(this.rotation) + "}";
   }
}
