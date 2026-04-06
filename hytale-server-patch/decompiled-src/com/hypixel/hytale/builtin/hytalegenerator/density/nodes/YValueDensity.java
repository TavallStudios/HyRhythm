package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class YValueDensity extends Density {
   public double process(@Nonnull Density.Context context) {
      return context.position.y;
   }

   public void setInputs(@Nonnull Density[] inputs) {
   }
}
