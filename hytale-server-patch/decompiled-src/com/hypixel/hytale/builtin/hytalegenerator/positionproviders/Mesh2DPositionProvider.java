package com.hypixel.hytale.builtin.hytalegenerator.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.fields.points.PointProvider;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class Mesh2DPositionProvider extends PositionProvider {
   @Nonnull
   private final PointProvider pointGenerator;
   private final int y;

   public Mesh2DPositionProvider(@Nonnull PointProvider positionProvider, int y) {
      this.pointGenerator = positionProvider;
      this.y = y;
   }

   public void positionsIn(@Nonnull PositionProvider.Context context) {
      if (!(context.minInclusive.y > (double)this.y) && !(context.maxExclusive.y <= (double)this.y)) {
         Vector2d min2d = new Vector2d(context.minInclusive.x, context.minInclusive.z);
         Vector2d max2d = new Vector2d(context.maxExclusive.x, context.maxExclusive.z);
         this.pointGenerator.points2d(min2d, max2d, (point) -> context.consumer.accept(new Vector3d(point.x, (double)this.y, point.y)));
      }
   }
}
