package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import javax.annotation.Nonnull;

public class RandomCoordinateCondition implements ICoordinateCondition {
   private final double chance;

   public RandomCoordinateCondition(double chance) {
      this.chance = chance;
   }

   public boolean eval(int seed, int x, int y) {
      return HashUtil.random((long)seed, (long)x, (long)y) <= this.chance;
   }

   public boolean eval(int seed, int x, int y, int z) {
      return HashUtil.random((long)seed, (long)x, (long)y, (long)z) <= this.chance;
   }

   @Nonnull
   public String toString() {
      return "RandomCoordinateCondition{chance=" + this.chance + "}";
   }
}
