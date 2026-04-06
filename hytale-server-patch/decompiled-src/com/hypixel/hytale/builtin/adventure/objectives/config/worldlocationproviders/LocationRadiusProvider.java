package com.hypixel.hytale.builtin.adventure.objectives.config.worldlocationproviders;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LocationRadiusProvider extends WorldLocationProvider {
   @Nonnull
   public static final BuilderCodec<LocationRadiusProvider> CODEC;
   protected int minRadius = 10;
   protected int maxRadius = 50;

   @Nullable
   public Vector3i runCondition(@Nonnull World world, @Nonnull Vector3i position) {
      double angle = Math.random() * 6.2831854820251465;
      int radius = MathUtil.randomInt(this.minRadius, this.maxRadius);
      Vector3i newPosition = position.clone();
      newPosition.add((int)((float)radius * TrigMathUtil.cos(angle)), 0, (int)((float)radius * TrigMathUtil.sin(angle)));
      long chunkIndex = ChunkUtil.indexChunkFromBlock(newPosition.x, newPosition.z);
      WorldChunk worldChunkComponent = world.getChunk(chunkIndex);
      if (worldChunkComponent == null) {
         return null;
      } else {
         newPosition.y = worldChunkComponent.getHeight(newPosition.x, newPosition.z);
         return newPosition;
      }
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LocationRadiusProvider that = (LocationRadiusProvider)o;
         if (this.minRadius != that.minRadius) {
            return false;
         } else {
            return this.maxRadius == that.maxRadius;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.minRadius;
      result = 31 * result + this.maxRadius;
      return result;
   }

   @Nonnull
   public String toString() {
      int var10000 = this.minRadius;
      return "LocationRadiusProvider{minRadius=" + var10000 + ", maxRadius=" + this.maxRadius + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(LocationRadiusProvider.class, LocationRadiusProvider::new, BASE_CODEC).append(new KeyedCodec("MinRadius", Codec.INTEGER), (locationRadiusCondition, integer) -> locationRadiusCondition.minRadius = integer, (locationRadiusCondition) -> locationRadiusCondition.minRadius).addValidator(Validators.greaterThan(0)).add()).append(new KeyedCodec("MaxRadius", Codec.INTEGER), (locationRadiusCondition, integer) -> locationRadiusCondition.maxRadius = integer, (locationRadiusCondition) -> locationRadiusCondition.maxRadius).addValidator(Validators.greaterThanOrEqual(1)).add()).afterDecode((locationRadiusCondition) -> {
         if (locationRadiusCondition.minRadius > locationRadiusCondition.maxRadius) {
            throw new IllegalArgumentException("LocationRadiusCondition.MinRadius (" + locationRadiusCondition.minRadius + ") needs to be greater than LocationRadiusCondition.MaxRadius (" + locationRadiusCondition.maxRadius + ")");
         }
      })).build();
   }
}
