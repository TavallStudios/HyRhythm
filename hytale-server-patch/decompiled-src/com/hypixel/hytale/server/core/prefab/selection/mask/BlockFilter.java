package com.hypixel.hytale.server.core.prefab.selection.mask;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.function.FunctionCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.PlaceFluidInteraction;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockFilter {
   public static final BlockFilter[] EMPTY_ARRAY = new BlockFilter[0];
   public static final Codec<BlockFilter> CODEC;
   public static final String BLOCK_SEPARATOR = "|";
   public static final Pattern BLOCK_SEPARATOR_PATTERN;
   @Nonnull
   private final FilterType blockFilterType;
   @Nonnull
   private final String[] blocks;
   private final boolean inverted;
   @Nonnull
   private final transient String toString0;
   private IntSet resolvedBlocks;
   private IntSet resolvedFluids;

   public BlockFilter(@Nonnull FilterType blockFilterType, @Nonnull String[] blocks, boolean inverted) {
      Objects.requireNonNull(blockFilterType);
      Objects.requireNonNull(blocks);
      this.blockFilterType = blockFilterType;
      this.blocks = blocks;
      this.inverted = inverted;
      this.toString0 = this.toString0();
   }

   public void resolve() {
      if (this.resolvedBlocks == null) {
         BlocksAndFluids result = parseBlocksAndFluids(this.blocks);
         this.resolvedBlocks = result.blocks;
         this.resolvedFluids = result.fluids;
      }

   }

   @Nonnull
   public FilterType getBlockFilterType() {
      return this.blockFilterType;
   }

   @Nonnull
   public String[] getBlocks() {
      return this.blocks;
   }

   public boolean isInverted() {
      return this.inverted;
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId) {
      return this.isExcluded(accessor, x, y, z, min, max, blockId, -1);
   }

   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId, int fluidId) {
      boolean exclude = !this.isIncluded(accessor, x, y, z, min, max, blockId, fluidId);
      return this.inverted != exclude;
   }

   private boolean isIncluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, @Nullable Vector3i min, @Nullable Vector3i max, int blockId) {
      return this.isIncluded(accessor, x, y, z, min, max, blockId, -1);
   }

   private boolean isIncluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, @Nullable Vector3i min, @Nullable Vector3i max, int blockId, int fluidId) {
      switch (this.blockFilterType.ordinal()) {
         case 0:
            this.resolve();
            boolean matchesBlock = this.resolvedBlocks.contains(blockId);
            boolean matchesFluid = fluidId >= 0 && this.resolvedFluids != null && this.resolvedFluids.contains(fluidId);
            return matchesBlock || matchesFluid;
         case 1:
            return this.matchesAt(accessor, x, y - 1, z);
         case 2:
            return this.matchesAt(accessor, x, y + 1, z);
         case 3:
            return this.matchesAt(accessor, x - 1, y, z) || this.matchesAt(accessor, x + 1, y, z) || this.matchesAt(accessor, x, y, z - 1) || this.matchesAt(accessor, x, y, z + 1);
         case 4:
            for(int xo = -1; xo < 2; ++xo) {
               for(int yo = -1; yo < 2; ++yo) {
                  for(int zo = -1; zo < 2; ++zo) {
                     if ((xo != 0 || yo != 0 || zo != 0) && this.matchesAt(accessor, x + xo, y + yo, z + zo)) {
                        return true;
                     }
                  }
               }
            }

            return false;
         case 5:
            return this.matchesAt(accessor, x, y, z - 1);
         case 6:
            return this.matchesAt(accessor, x, y, z + 1);
         case 7:
            return this.matchesAt(accessor, x, y, z + 1);
         case 8:
            return this.matchesAt(accessor, x, y, z - 1);
         case 9:
            return this.matchesAt(accessor, x - 1, y + 1, z) || this.matchesAt(accessor, x - 1, y - 1, z) || this.matchesAt(accessor, x + 1, y + 1, z) || this.matchesAt(accessor, x + 1, y - 1, z);
         case 10:
            return this.matchesAt(accessor, x - 1, y, z + 1) || this.matchesAt(accessor, x - 1, y, z - 1) || this.matchesAt(accessor, x + 1, y, z + 1) || this.matchesAt(accessor, x + 1, y, z - 1);
         case 11:
            return this.matchesAt(accessor, x, y - 1, z + 1) || this.matchesAt(accessor, x, y - 1, z - 1) || this.matchesAt(accessor, x, y + 1, z + 1) || this.matchesAt(accessor, x, y + 1, z - 1);
         case 12:
            if (min != null && max != null) {
               return x >= min.x && y >= min.y && z >= min.z && x <= max.x && y <= max.y && z <= max.z;
            }

            return false;
         default:
            throw new IllegalArgumentException("Unknown filter type: " + String.valueOf(this.blockFilterType));
      }
   }

   private boolean matchesAt(@Nonnull ChunkAccessor accessor, int x, int y, int z) {
      this.resolve();
      if (this.resolvedBlocks.contains(accessor.getBlock(x, y, z))) {
         return true;
      } else {
         return this.resolvedFluids != null && this.resolvedFluids.contains(accessor.getFluidId(x, y, z));
      }
   }

   @Nonnull
   public String toString() {
      return this.toString0;
   }

   @Nonnull
   public String toString0() {
      String var10000 = this.inverted ? "!" : "";
      return var10000 + this.blockFilterType.getPrefix() + String.join("|", this.blocks);
   }

   @Nonnull
   public String informativeToString() {
      StringBuilder builder = new StringBuilder();
      String var10000 = this.inverted ? "!" : "";
      String prefix = var10000 + this.blockFilterType.getPrefix();
      if (this.blocks.length > 1) {
         builder.append("(");
      }

      for(int i = 0; i < this.blocks.length; ++i) {
         builder.append(prefix).append(this.blocks[i]);
         if (i != this.blocks.length - 1) {
            builder.append(" OR ");
         }
      }

      if (this.blocks.length > 1) {
         builder.append(")");
      }

      return builder.toString();
   }

   @Nonnull
   public static BlockFilter parse(@Nonnull String str) {
      ParsedFilterParts parts = parseComponents(str);
      String[] blocks = parts.type.hasBlocks() ? BLOCK_SEPARATOR_PATTERN.split(parts.blocks) : ArrayUtil.EMPTY_STRING_ARRAY;
      return new BlockFilter(parts.type, blocks, parts.inverted);
   }

   @Nonnull
   public static ParsedFilterParts parseComponents(@Nonnull String str) {
      boolean invert = str.startsWith("!");
      int index = invert ? 1 : 0;
      FilterType filterType = BlockFilter.FilterType.parse(str, index);
      index += filterType.getPrefix().length();
      String blocks = str.substring(index);
      return new ParsedFilterParts(filterType, invert, blocks);
   }

   @Nonnull
   public static IntSet parseBlocks(@Nonnull String[] blocksArgs) {
      return parseBlocksAndFluids(blocksArgs).blocks;
   }

   @Nonnull
   private static BlocksAndFluids parseBlocksAndFluids(@Nonnull String[] blocksArgs) {
      IntSet blocks = new IntOpenHashSet();
      IntSet fluids = new IntOpenHashSet();

      label41:
      for(String blockArg : blocksArgs) {
         Item item = (Item)Item.getAssetMap().getAsset(blockArg);
         if (item != null) {
            int fluidId = getFluidIdFromItem(item);
            if (fluidId >= 0) {
               fluids.add(fluidId);
               continue;
            }
         }

         int blockId = BlockPattern.parseBlock(blockArg);
         BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(blockId);
         if (blockType != null && blockType.getBlockListAssetId() != null) {
            BlockTypeListAsset blockTypeListAsset = (BlockTypeListAsset)BlockTypeListAsset.getAssetMap().getAsset(blockType.getBlockListAssetId());
            if (blockTypeListAsset != null && blockTypeListAsset.getBlockPattern() != null) {
               Integer[] var11 = blockTypeListAsset.getBlockPattern().getResolvedKeys();
               int var12 = var11.length;
               int var13 = 0;

               while(true) {
                  if (var13 >= var12) {
                     continue label41;
                  }

                  int resolvedKey = var11[var13];
                  blocks.add(resolvedKey);
                  ++var13;
               }
            }
         }

         blocks.add(blockId);
      }

      return new BlocksAndFluids(IntSets.unmodifiable(blocks), fluids.isEmpty() ? null : IntSets.unmodifiable(fluids));
   }

   private static int getFluidIdFromItem(@Nonnull Item item) {
      Map<InteractionType, String> interactions = item.getInteractions();
      String secondaryRootId = (String)interactions.get(InteractionType.Secondary);
      if (secondaryRootId == null) {
         return -1;
      } else {
         RootInteraction rootInteraction = (RootInteraction)RootInteraction.getAssetMap().getAsset(secondaryRootId);
         if (rootInteraction == null) {
            return -1;
         } else {
            for(String interactionId : rootInteraction.getInteractionIds()) {
               Interaction interaction = (Interaction)Interaction.getAssetMap().getAsset(interactionId);
               if (interaction instanceof PlaceFluidInteraction) {
                  PlaceFluidInteraction placeFluidInteraction = (PlaceFluidInteraction)interaction;
                  String fluidKey = placeFluidInteraction.getFluidKey();
                  if (fluidKey != null) {
                     int fluidId = Fluid.getAssetMap().getIndex(fluidKey);
                     if (fluidId >= 0) {
                        return fluidId;
                     }
                  }
               }
            }

            return -1;
         }
      }
   }

   static {
      CODEC = new FunctionCodec(Codec.STRING, BlockFilter::parse, BlockFilter::toString);
      BLOCK_SEPARATOR_PATTERN = Pattern.compile(Pattern.quote("|"));
   }

   public static record ParsedFilterParts(FilterType type, boolean inverted, String blocks) {
   }

   private static class BlocksAndFluids {
      final IntSet blocks;
      final IntSet fluids;

      BlocksAndFluids(IntSet blocks, IntSet fluids) {
         this.blocks = blocks;
         this.fluids = fluids;
      }
   }

   public static enum FilterType {
      TargetBlock(""),
      AboveBlock(">"),
      BelowBlock("<"),
      AdjacentBlock("~"),
      NeighborBlock("^"),
      NorthBlock("+n"),
      EastBlock("+e"),
      SouthBlock("+s"),
      WestBlock("+w"),
      DiagonalXy("%xy"),
      DiagonalXz("%xz"),
      DiagonalZy("%zy"),
      Selection("#", false);

      public static final String INVERT_PREFIX = "!";
      public static final String TARGET_BLOCK_PREFIX = "";
      public static final String ABOVE_BLOCK_PREFIX = ">";
      public static final String BELOW_BLOCK_PREFIX = "<";
      public static final String ADJACENT_BLOCK_PREFIX = "~";
      public static final String NEIGHBOR_BLOCK_PREFIX = "^";
      public static final String SELECTION_PREFIX = "#";
      public static final String CARDINAL_NORTH_PREFIX = "+n";
      public static final String CARDINAL_EAST_PREFIX = "+e";
      public static final String CARDINAL_SOUTH_PREFIX = "+s";
      public static final String CARDINAL_WEST_PREFIX = "+w";
      public static final String DIAGONAL_XY_PREFIX = "%xy";
      public static final String DIAGONAL_XZ_PREFIX = "%xz";
      public static final String DIAGONAL_ZY_PREFIX = "%zy";
      @Nonnull
      private static final FilterType[] VALUES_TO_PARSE;
      private final String prefix;
      private final boolean hasBlocks;

      private FilterType(String prefix) {
         this.prefix = prefix;
         this.hasBlocks = true;
      }

      private FilterType(String prefix, boolean hasBlocks) {
         this.prefix = prefix;
         this.hasBlocks = hasBlocks;
      }

      public boolean hasBlocks() {
         return this.hasBlocks;
      }

      public String getPrefix() {
         return this.prefix;
      }

      @Nonnull
      public static FilterType parse(@Nonnull String str, int index) {
         for(FilterType filterType : VALUES_TO_PARSE) {
            if (str.startsWith(filterType.prefix, index)) {
               return filterType;
            }
         }

         return TargetBlock;
      }

      static {
         FilterType[] values = values();
         FilterType[] valuesToParse = new FilterType[values.length - 1];
         int i = 0;

         for(FilterType value : values) {
            if (value != TargetBlock) {
               valuesToParse[i++] = value;
            }
         }

         VALUES_TO_PARSE = valuesToParse;
      }
   }
}
