package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import java.util.Random;
import javax.annotation.Nonnull;

public class WeighedShader<T> implements Shader<T> {
   @Nonnull
   private final WeightedMap<Shader<T>> childrenWeightedMap = new WeightedMap<Shader<T>>(1);
   private SeedGenerator seedGenerator = new SeedGenerator(System.nanoTime());

   public WeighedShader(@Nonnull Shader<T> initialChild, double weight) {
      this.add(initialChild, weight);
   }

   @Nonnull
   public WeighedShader<T> add(@Nonnull Shader<T> child, double weight) {
      if (weight <= 0.0) {
         throw new IllegalArgumentException("invalid weight");
      } else {
         this.childrenWeightedMap.add(child, weight);
         return this;
      }
   }

   @Nonnull
   public WeighedShader<T> setSeed(long seed) {
      this.seedGenerator = new SeedGenerator(seed);
      return this;
   }

   public T shade(T current, long seed) {
      Random r = new Random(seed);
      return (T)((Shader)this.childrenWeightedMap.pick(r)).shade(current, seed);
   }

   public T shade(T current, long seedA, long seedB) {
      return (T)this.shade(current, this.seedGenerator.seedAt(seedA, seedB));
   }

   public T shade(T current, long seedA, long seedB, long seedC) {
      return (T)this.shade(current, this.seedGenerator.seedAt(seedA, seedB, seedC));
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.childrenWeightedMap);
      return "WeighedShader{childrenWeighedMap=" + var10000 + ", seedGenerator=" + String.valueOf(this.seedGenerator) + "}";
   }
}
