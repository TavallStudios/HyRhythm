package com.hypixel.hytale.procedurallib.supplier;

import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FloatRangeNoiseSupplier implements IFloatCoordinateSupplier {
   protected final IFloatRange range;
   @Nonnull
   protected final NoiseProperty noiseProperty;
   @Nonnull
   protected final IDoubleCoordinateSupplier2d supplier2d;
   @Nonnull
   protected final IDoubleCoordinateSupplier3d supplier3d;

   public FloatRangeNoiseSupplier(IFloatRange range, @Nonnull NoiseProperty noiseProperty) {
      this.range = range;
      this.noiseProperty = noiseProperty;
      Objects.requireNonNull(noiseProperty);
      this.supplier2d = noiseProperty::get;
      Objects.requireNonNull(noiseProperty);
      this.supplier3d = noiseProperty::get;
   }

   public float get(int seed, double x, double y) {
      return this.range.getValue(seed, x, y, this.supplier2d);
   }

   public float get(int seed, double x, double y, double z) {
      return this.range.getValue(seed, x, y, z, this.supplier3d);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.range);
      return "FloatRangeNoiseSupplier{range=" + var10000 + ", noiseProperty=" + String.valueOf(this.noiseProperty) + "}";
   }
}
