package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.random.CoordinateRotator;
import javax.annotation.Nonnull;

public class RotateNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noise;
   protected final CoordinateRotator rotation;

   public RotateNoiseProperty(NoiseProperty noise, CoordinateRotator rotation) {
      this.noise = noise;
      this.rotation = rotation;
   }

   public double get(int seed, double x, double y) {
      double px = this.rotation.rotateX(x, y);
      double py = this.rotation.rotateY(x, y);
      return this.noise.get(seed, px, py);
   }

   public double get(int seed, double x, double y, double z) {
      double px = this.rotation.rotateX(x, y, z);
      double py = this.rotation.rotateY(x, y, z);
      double pz = this.rotation.rotateZ(x, y, z);
      return this.noise.get(seed, px, py, pz);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.noise);
      return "RotateNoiseProperty{noise=" + var10000 + ", rotation=" + String.valueOf(this.rotation) + "}";
   }
}
