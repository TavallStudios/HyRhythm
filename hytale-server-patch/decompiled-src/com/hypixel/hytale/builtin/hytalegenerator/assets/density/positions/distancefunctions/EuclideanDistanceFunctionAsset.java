package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions;

import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.DistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.EuclideanDistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class EuclideanDistanceFunctionAsset extends DistanceFunctionAsset {
   @Nonnull
   public static final BuilderCodec<EuclideanDistanceFunctionAsset> CODEC;

   @Nonnull
   public DistanceFunction build(@Nonnull SeedBox parentSeed, double maxDistance) {
      return new EuclideanDistanceFunction();
   }

   static {
      CODEC = BuilderCodec.builder(EuclideanDistanceFunctionAsset.class, EuclideanDistanceFunctionAsset::new, DistanceFunctionAsset.ABSTRACT_CODEC).build();
      DistanceFunctionAsset.CODEC.register("Euclidean", EuclideanDistanceFunctionAsset.class, CODEC);
   }
}
