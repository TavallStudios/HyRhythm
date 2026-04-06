package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MaskOperation extends SequenceBrushOperation {
   public static final BuilderCodec<MaskOperation> CODEC;
   @Nonnull
   public BlockMask operationMaskArg;

   public MaskOperation() {
      super("Set Operation Mask", "Sets the operation mask to only modify blocks that match the mask", false);
      this.operationMaskArg = BlockMask.EMPTY;
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      brushConfig.setOperationMask(this.operationMaskArg);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(MaskOperation.class, MaskOperation::new).append(new KeyedCodec("Mask", BlockMask.CODEC), (op, val) -> op.operationMaskArg = val, (op) -> op.operationMaskArg).documentation("Sets the operation mask").add()).documentation("Sets the operation mask to only modify blocks that match the mask")).build();
   }
}
