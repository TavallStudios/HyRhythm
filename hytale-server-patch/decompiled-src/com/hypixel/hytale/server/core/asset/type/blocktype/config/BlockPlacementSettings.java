package com.hypixel.hytale.server.core.asset.type.blocktype.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.BlockPlacementRotationMode;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class BlockPlacementSettings implements NetworkSerializable<com.hypixel.hytale.protocol.BlockPlacementSettings> {
   public static final BuilderCodec<BlockPlacementSettings> CODEC;
   protected String wallPlacementOverrideBlockId;
   protected String floorPlacementOverrideBlockId;
   protected String ceilingPlacementOverrideBlockId;
   private boolean allowRotationKey = true;
   private boolean placeInEmptyBlocks;
   private BlockPreviewVisibility previewVisibility;
   private RotationMode rotationMode;
   protected boolean allowBreakReplace;

   protected BlockPlacementSettings() {
      this.previewVisibility = BlockPlacementSettings.BlockPreviewVisibility.DEFAULT;
      this.rotationMode = BlockPlacementSettings.RotationMode.DEFAULT;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockPlacementSettings toPacket() {
      com.hypixel.hytale.protocol.BlockPlacementSettings packet = new com.hypixel.hytale.protocol.BlockPlacementSettings();
      packet.allowRotationKey = this.allowRotationKey;
      packet.placeInEmptyBlocks = this.placeInEmptyBlocks;
      packet.allowBreakReplace = this.allowBreakReplace;
      BlockPreviewVisibility var2 = this.previewVisibility;
      byte var3 = 0;
      com.hypixel.hytale.protocol.BlockPreviewVisibility var10001;
      //$FF: var3->value
      switch (var2.typeSwitch<invokedynamic>(var2, var3)) {
         case -1 -> var10001 = com.hypixel.hytale.protocol.BlockPreviewVisibility.Default;
         case 0 -> var10001 = com.hypixel.hytale.protocol.BlockPreviewVisibility.Default;
         case 1 -> var10001 = com.hypixel.hytale.protocol.BlockPreviewVisibility.AlwaysHidden;
         case 2 -> var10001 = com.hypixel.hytale.protocol.BlockPreviewVisibility.AlwaysVisible;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      packet.previewVisibility = var10001;
      RotationMode var4 = this.rotationMode;
      var3 = 0;
      BlockPlacementRotationMode var6;
      //$FF: var3->value
      switch (var4.typeSwitch<invokedynamic>(var4, var3)) {
         case -1 -> var6 = BlockPlacementRotationMode.Default;
         case 0 -> var6 = BlockPlacementRotationMode.Default;
         case 1 -> var6 = BlockPlacementRotationMode.FacingPlayer;
         case 2 -> var6 = BlockPlacementRotationMode.StairFacingPlayer;
         case 3 -> var6 = BlockPlacementRotationMode.BlockNormal;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      packet.rotationMode = var6;
      packet.wallPlacementOverrideBlockId = this.wallPlacementOverrideBlockId == null ? -1 : BlockType.getAssetMap().getIndex(this.wallPlacementOverrideBlockId);
      if (packet.wallPlacementOverrideBlockId == -2147483648) {
         throw new IllegalArgumentException("Unknown key! " + this.wallPlacementOverrideBlockId);
      } else {
         packet.floorPlacementOverrideBlockId = this.floorPlacementOverrideBlockId == null ? -1 : BlockType.getAssetMap().getIndex(this.floorPlacementOverrideBlockId);
         if (packet.floorPlacementOverrideBlockId == -2147483648) {
            throw new IllegalArgumentException("Unknown key! " + this.floorPlacementOverrideBlockId);
         } else {
            packet.ceilingPlacementOverrideBlockId = this.ceilingPlacementOverrideBlockId == null ? -1 : BlockType.getAssetMap().getIndex(this.ceilingPlacementOverrideBlockId);
            if (packet.ceilingPlacementOverrideBlockId == -2147483648) {
               throw new IllegalArgumentException("Unknown key! " + this.ceilingPlacementOverrideBlockId);
            } else {
               return packet;
            }
         }
      }
   }

   public String getWallPlacementOverrideBlockId() {
      return this.wallPlacementOverrideBlockId;
   }

   public String getFloorPlacementOverrideBlockId() {
      return this.floorPlacementOverrideBlockId;
   }

   public String getCeilingPlacementOverrideBlockId() {
      return this.ceilingPlacementOverrideBlockId;
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(BlockPlacementSettings.class, BlockPlacementSettings::new).append(new KeyedCodec("AllowRotationKey", Codec.BOOLEAN), (placementSettings, o) -> placementSettings.allowRotationKey = o, (placementSettings) -> placementSettings.allowRotationKey).add()).append(new KeyedCodec("PlaceInEmptyBlocks", Codec.BOOLEAN), (placementSettings, o) -> placementSettings.placeInEmptyBlocks = o, (placementSettings) -> placementSettings.placeInEmptyBlocks).documentation("If this block is allowed to be placed inside other blocks with an Empty Material (destroying them).").add()).append(new KeyedCodec("RotationMode", BlockPlacementSettings.RotationMode.CODEC), (placementSettings, o) -> placementSettings.rotationMode = o, (placementSettings) -> placementSettings.rotationMode).documentation("The mode determining the rotation of this block when placed.").add()).append(new KeyedCodec("BlockPreviewVisibility", BlockPlacementSettings.BlockPreviewVisibility.CODEC), (placementSettings, o) -> placementSettings.previewVisibility = o, (placementSettings) -> placementSettings.previewVisibility).documentation("An override for the block preview visibility").add()).append(new KeyedCodec("WallPlacementOverrideBlockId", Codec.STRING), (placementSettings, o) -> placementSettings.wallPlacementOverrideBlockId = o, (placementSettings) -> placementSettings.wallPlacementOverrideBlockId).add()).append(new KeyedCodec("FloorPlacementOverrideBlockId", Codec.STRING), (placementSettings, o) -> placementSettings.floorPlacementOverrideBlockId = o, (placementSettings) -> placementSettings.floorPlacementOverrideBlockId).add()).append(new KeyedCodec("CeilingPlacementOverrideBlockId", Codec.STRING), (placementSettings, o) -> placementSettings.ceilingPlacementOverrideBlockId = o, (placementSettings) -> placementSettings.ceilingPlacementOverrideBlockId).add()).append(new KeyedCodec("AllowBreakReplace", Codec.BOOLEAN), (o, v) -> o.allowBreakReplace = v, (o) -> o.allowBreakReplace).add()).build();
   }

   public static enum BlockPreviewVisibility {
      ALWAYS_VISIBLE,
      ALWAYS_HIDDEN,
      DEFAULT;

      public static final EnumCodec<BlockPreviewVisibility> CODEC = new EnumCodec<BlockPreviewVisibility>(BlockPreviewVisibility.class);
   }

   public static enum RotationMode {
      FACING_PLAYER,
      BLOCK_NORMAL,
      STAIR_FACING_PLAYER,
      DEFAULT;

      public static final EnumCodec<RotationMode> CODEC = new EnumCodec<RotationMode>(RotationMode.class);
   }
}
