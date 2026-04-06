package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.flowcontrol;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class JumpIfBlockTypeOperation extends SequenceBrushOperation {
   public static final BuilderCodec<JumpIfBlockTypeOperation> CODEC;
   @Nonnull
   public BlockMask blockMaskArg;
   @Nonnull
   public String indexVariableNameArg;

   public JumpIfBlockTypeOperation() {
      super("Jump If Block Type Comparison", "Jump the execution of the stack based on a block type comparison", false);
      this.blockMaskArg = BlockMask.EMPTY;
      this.indexVariableNameArg = "Undefined";
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Vector3i currentBrushOrigin = brushConfig.getOrigin();
      if (currentBrushOrigin == null) {
         brushConfig.setErrorFlag("Could not find the origin for the operation.");
      } else {
         int targetBlockId = brushConfigCommandExecutor.getEdit().getBlock(currentBrushOrigin.x, currentBrushOrigin.y, currentBrushOrigin.z);
         int targetFluidId = brushConfigCommandExecutor.getEdit().getFluid(currentBrushOrigin.x, currentBrushOrigin.y, currentBrushOrigin.z);
         if (!this.blockMaskArg.isExcluded(brushConfigCommandExecutor.getEdit().getAccessor(), currentBrushOrigin.x, currentBrushOrigin.y, currentBrushOrigin.z, (Vector3i)null, (Vector3i)null, targetBlockId, targetFluidId)) {
            brushConfigCommandExecutor.loadOperatingIndex(this.indexVariableNameArg);
         }

      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(JumpIfBlockTypeOperation.class, JumpIfBlockTypeOperation::new).append(new KeyedCodec("Mask", BlockMask.CODEC), (op, val) -> op.blockMaskArg = val, (op) -> op.blockMaskArg).documentation("The block mask for the comparison.").add()).append(new KeyedCodec("StoredIndexName", Codec.STRING), (op, val) -> op.indexVariableNameArg = val, (op) -> op.indexVariableNameArg).documentation("The labeled index to jump to, previous or future").add()).documentation("Jump the execution of the stack based on a block type comparison")).build();
   }
}
