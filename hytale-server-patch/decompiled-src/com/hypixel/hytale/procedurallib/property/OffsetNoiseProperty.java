package com.hypixel.hytale.procedurallib.property;

import javax.annotation.Nonnull;

public class OffsetNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noiseProperty;
   protected final double offsetX;
   protected final double offsetY;
   protected final double offsetZ;

   public OffsetNoiseProperty(NoiseProperty noiseProperty, double offset) {
      this(noiseProperty, offset, offset, offset);
   }

   public OffsetNoiseProperty(NoiseProperty noiseProperty, double offsetX, double offsetY, double offsetZ) {
      this.noiseProperty = noiseProperty;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
   }

   public NoiseProperty getNoiseProperty() {
      return this.noiseProperty;
   }

   public double getOffsetX() {
      return this.offsetX;
   }

   public double getOffsetY() {
      return this.offsetY;
   }

   public double getOffsetZ() {
      return this.offsetZ;
   }

   public double get(int seed, double x, double y) {
      return this.noiseProperty.get(seed, x + this.offsetX, y + this.offsetY);
   }

   public double get(int seed, double x, double y, double z) {
      return this.noiseProperty.get(seed, x + this.offsetX, y + this.offsetY, z + this.offsetZ);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.noiseProperty);
      return "OffsetNoiseProperty{noiseProperty=" + var10000 + ", offsetX=" + this.offsetX + ", offsetY=" + this.offsetY + ", offsetZ=" + this.offsetZ + "}";
   }
}
