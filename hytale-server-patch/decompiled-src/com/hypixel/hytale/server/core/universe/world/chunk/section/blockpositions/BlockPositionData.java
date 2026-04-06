package com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSectionReference;

public class BlockPositionData implements IBlockPositionData {
   private static final double HALF_BLOCK = 0.5;
   private int blockIndex;
   private ChunkSectionReference section;
   private int blockType;

   public BlockPositionData(int blockIndex, ChunkSectionReference section, int blockType) {
      this.blockIndex = blockIndex;
      this.section = section;
      this.blockType = blockType;
   }

   public BlockSection getChunkSection() {
      return this.section.getSection();
   }

   public int getBlockType() {
      return this.blockType;
   }

   public int getX() {
      return ChunkUtil.xFromIndex(this.blockIndex) + (this.section.getChunk().getX() << 5);
   }

   public int getY() {
      return ChunkUtil.yFromIndex(this.blockIndex) + (this.section.getSectionIndex() << 5);
   }

   public int getZ() {
      return ChunkUtil.zFromIndex(this.blockIndex) + (this.section.getChunk().getZ() << 5);
   }

   public double getXCentre() {
      return (double)this.getX() + 0.5;
   }

   public double getYCentre() {
      return (double)this.getY() + 0.5;
   }

   public double getZCentre() {
      return (double)this.getZ() + 0.5;
   }
}
