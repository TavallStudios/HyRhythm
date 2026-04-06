package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolMaskArg;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import javax.annotation.Nonnull;

public class MaskArg extends ToolArg<BlockMask> {
   public static final MaskArg EMPTY;
   public static final BuilderCodec<MaskArg> CODEC;

   public MaskArg() {
   }

   public MaskArg(BlockMask value) {
      this.value = value;
   }

   public MaskArg(BlockMask value, boolean required) {
      this.value = value;
      this.required = required;
   }

   @Nonnull
   public Codec<BlockMask> getCodec() {
      return BlockMask.CODEC;
   }

   @Nonnull
   public BlockMask fromString(@Nonnull String str) {
      return BlockMask.parse(str);
   }

   @Nonnull
   public BuilderToolMaskArg toMaskArgPacket() {
      return new BuilderToolMaskArg(((BlockMask)this.value).toString());
   }

   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Mask;
      packet.maskArg = this.toMaskArgPacket();
   }

   @Nonnull
   public String toString() {
      return "MaskArg{} " + super.toString();
   }

   static {
      EMPTY = new MaskArg(BlockMask.EMPTY, false);
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(MaskArg.class, MaskArg::new, ToolArg.DEFAULT_CODEC).addField(new KeyedCodec("Default", BlockMask.CODEC), (maskArg, d) -> maskArg.value = d, (maskArg) -> maskArg.value)).build();
   }
}
