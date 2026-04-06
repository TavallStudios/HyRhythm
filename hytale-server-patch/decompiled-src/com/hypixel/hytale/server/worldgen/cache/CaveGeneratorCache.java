package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import java.util.Objects;
import javax.annotation.Nonnull;

public class CaveGeneratorCache extends ExtendedCoordinateCache<CaveType, Cave> {
   public CaveGeneratorCache(@Nonnull CaveFunction caveFunction, int maxSize, long expireAfterSeconds) {
      Objects.requireNonNull(caveFunction);
      super(caveFunction::compute, (ExtendedCoordinateCache.ExtendedCoordinateRemovalListener)null, maxSize, expireAfterSeconds);
   }

   @Nonnull
   protected ExtendedCoordinateCache.ExtendedCoordinateKey<CaveType> localKey() {
      return ChunkGenerator.getResource().cacheCaveCoordinateKey;
   }

   @FunctionalInterface
   public interface CaveFunction {
      Cave compute(CaveType var1, int var2, int var3, int var4);
   }
}
