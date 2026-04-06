package com.hypixel.hytale.builtin.hytalegenerator.density;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.EnvironmentProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.TerrainDensityProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.tintproviders.TintProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Density {
   @Nonnull
   private static final Bounds3i DEFAULT_READ_BOUNDS = new Bounds3i();
   public static final double DEFAULT_VALUE = 1.7976931348623157E308;
   public static final double DEFAULT_DENSITY = 0.0;

   public abstract double process(@Nonnull Context var1);

   public void setInputs(Density[] inputs) {
   }

   public static class Context {
      @Nonnull
      public Vector3d position;
      @Nullable
      public Vector3d densityAnchor;
      @Nullable
      public Vector3d positionsAnchor;
      public int switchState;
      public double distanceFromCellWall;
      @Nullable
      public TerrainDensityProvider terrainDensityProvider;
      public double distanceToBiomeEdge;

      public Context() {
         this.position = new Vector3d();
         this.densityAnchor = null;
         this.positionsAnchor = null;
         this.switchState = 0;
         this.distanceFromCellWall = 1.7976931348623157E308;
         this.terrainDensityProvider = null;
         this.distanceToBiomeEdge = 1.7976931348623157E308;
      }

      public Context(@Nonnull Vector3d position, @Nullable Vector3d densityAnchor, int switchState, double distanceFromCellWall, @Nullable TerrainDensityProvider terrainDensityProvider, double distanceToBiomeEdge) {
         this.position = position;
         this.densityAnchor = densityAnchor;
         this.switchState = switchState;
         this.distanceFromCellWall = distanceFromCellWall;
         this.positionsAnchor = null;
         this.terrainDensityProvider = terrainDensityProvider;
         this.distanceToBiomeEdge = distanceToBiomeEdge;
      }

      public Context(@Nonnull Context other) {
         this.position = other.position;
         this.densityAnchor = other.densityAnchor;
         this.switchState = other.switchState;
         this.distanceFromCellWall = other.distanceFromCellWall;
         this.positionsAnchor = other.positionsAnchor;
         this.terrainDensityProvider = other.terrainDensityProvider;
         this.distanceToBiomeEdge = other.distanceToBiomeEdge;
      }

      public Context(@Nonnull VectorProvider.Context context) {
         this.position = context.position;
         this.terrainDensityProvider = context.terrainDensityProvider;
      }

      public Context(@Nonnull TintProvider.Context context) {
         this.position = context.position.toVector3d();
      }

      public Context(@Nonnull EnvironmentProvider.Context context) {
         this.position = context.position.toVector3d();
      }

      public Context(@Nonnull MaterialProvider.Context context) {
         this.position = context.position.toVector3d();
         this.terrainDensityProvider = context.terrainDensityProvider;
         this.distanceToBiomeEdge = context.distanceToBiomeEdge;
      }

      public Context(@Nonnull Pattern.Context context) {
         this.position = context.position.toVector3d();
      }

      public void assign(@Nonnull Context other) {
         this.position = other.position;
         this.densityAnchor = other.densityAnchor;
         this.switchState = other.switchState;
         this.distanceFromCellWall = other.distanceFromCellWall;
         this.positionsAnchor = other.positionsAnchor;
         this.terrainDensityProvider = other.terrainDensityProvider;
         this.distanceToBiomeEdge = other.distanceToBiomeEdge;
      }

      public void assign(@Nonnull VectorProvider.Context context) {
         this.position = context.position;
         this.terrainDensityProvider = context.terrainDensityProvider;
      }

      public void assign(@Nonnull MaterialProvider.Context context) {
         this.position.assign((double)context.position.x, (double)context.position.y, (double)context.position.z);
         this.terrainDensityProvider = context.terrainDensityProvider;
         this.distanceToBiomeEdge = context.distanceToBiomeEdge;
      }

      public void assign(@Nonnull EnvironmentProvider.Context context) {
         this.position.assign((double)context.position.x, (double)context.position.y, (double)context.position.z);
      }

      public void assign(@Nonnull Pattern.Context context) {
         this.position.assign((double)context.position.x, (double)context.position.y, (double)context.position.z);
      }
   }
}
