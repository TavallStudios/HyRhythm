package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import javax.annotation.Nonnull;

public class DistortedNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noiseProperty;
   protected final ICoordinateRandomizer randomizer;

   public DistortedNoiseProperty(NoiseProperty noiseProperty, ICoordinateRandomizer randomizer) {
      this.noiseProperty = noiseProperty;
      this.randomizer = randomizer;
   }

   public NoiseProperty getNoiseProperty() {
      return this.noiseProperty;
   }

   public ICoordinateRandomizer getRandomizer() {
      return this.randomizer;
   }

   public double get(int seed, double x, double y) {
      return this.noiseProperty.get(seed, this.randomizer.randomDoubleX(seed, x, y), this.randomizer.randomDoubleY(seed, x, y));
   }

   public double get(int seed, double x, double y, double z) {
      return this.noiseProperty.get(seed, this.randomizer.randomDoubleX(seed, x, y, z), this.randomizer.randomDoubleY(seed, x, y, z), this.randomizer.randomDoubleZ(seed, x, y, z));
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.noiseProperty);
      return "DistortedNoiseProperty{noiseProperty=" + var10000 + ", randomizer=" + String.valueOf(this.randomizer) + "}";
   }
}
