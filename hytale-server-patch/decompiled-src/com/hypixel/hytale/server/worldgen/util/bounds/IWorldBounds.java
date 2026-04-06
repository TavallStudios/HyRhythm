package com.hypixel.hytale.server.worldgen.util.bounds;

import com.hypixel.hytale.math.util.ChunkUtil;
import java.util.Random;
import javax.annotation.Nonnull;

public interface IWorldBounds extends IChunkBounds {
   int getLowBoundY();

   int getHighBoundY();

   default boolean intersectsChunk(long chunkIndex) {
      return this.intersectsChunk(ChunkUtil.xOfChunkIndex(chunkIndex), ChunkUtil.zOfChunkIndex(chunkIndex));
   }

   default int randomY(@Nonnull Random random) {
      return random.nextInt(this.getHighBoundY() - this.getLowBoundY()) + this.getLowBoundY();
   }

   default double fractionY(double d) {
      return (double)(this.getHighBoundY() - this.getLowBoundY()) * d + (double)this.getLowBoundY();
   }
}
