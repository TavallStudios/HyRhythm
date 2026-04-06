package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import java.util.Arrays;
import javax.annotation.Nonnull;

public enum MergedBlockFaces {
   ALL(BlockFace.VALUES),
   BLOCK_SIDES(new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}),
   CARDINAL_DIRECTIONS(new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}),
   HORIZONTAL(new BlockFace[]{BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST}),
   UP_CARDINAL_DIRECTIONS(new BlockFace[]{BlockFace.UP_NORTH, BlockFace.UP_EAST, BlockFace.UP_SOUTH, BlockFace.UP_WEST}),
   DOWN_CARDINAL_DIRECTIONS(new BlockFace[]{BlockFace.DOWN_NORTH, BlockFace.DOWN_EAST, BlockFace.DOWN_SOUTH, BlockFace.DOWN_WEST});

   @Nonnull
   public static EnumCodec<MergedBlockFaces> CODEC = new EnumCodec<MergedBlockFaces>(MergedBlockFaces.class);
   private final BlockFace[] components;

   private MergedBlockFaces(BlockFace... components) {
      this.components = components;
   }

   public BlockFace[] getComponents() {
      return this.components;
   }

   @Nonnull
   public String toString() {
      return "MergedBlockFaces{components=" + Arrays.toString(this.components) + "}";
   }
}
