package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential.masks;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockFilter;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AppendMaskFromToolArgOperation extends SequenceBrushOperation {
   public static final BuilderCodec<AppendMaskFromToolArgOperation> CODEC;
   @Nonnull
   public String argNameArg = "";
   @Nonnull
   public boolean invertArg = false;
   @Nonnull
   public BlockFilter.FilterType filterTypeArg;
   @Nullable
   public String additionalBlocksArg;

   public AppendMaskFromToolArgOperation() {
      super("Append Mask From Tool Arg", "Append a mask from a Block tool arg with configurable filter type", false);
      this.filterTypeArg = BlockFilter.FilterType.TargetBlock;
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = (Player)componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
      if (builderTool == null) {
         brushConfig.setErrorFlag("AppendMaskFromToolArg: No active builder tool");
      } else {
         ItemStack itemStack = playerComponent.getInventory().getItemInHand();
         if (itemStack == null) {
            brushConfig.setErrorFlag("AppendMaskFromToolArg: No item in hand");
         } else {
            BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
            Map<String, Object> toolArgs = argData.tool();
            if (toolArgs != null && toolArgs.containsKey(this.argNameArg)) {
               Object argValue = toolArgs.get(this.argNameArg);
               if (!(argValue instanceof BlockPattern)) {
                  String var10001 = this.argNameArg;
                  brushConfig.setErrorFlag("AppendMaskFromToolArg: Tool arg '" + var10001 + "' is not a Block type (found " + argValue.getClass().getSimpleName() + ")");
               } else {
                  BlockPattern blockPattern = (BlockPattern)argValue;
                  ArrayList blockNames = new ArrayList();
                  String patternStr = blockPattern.toString();
                  if (!patternStr.isEmpty() && !patternStr.equals("-")) {
                     for(String entry : patternStr.split(",")) {
                        int percentIdx = entry.indexOf(37);
                        String blockName = percentIdx >= 0 ? entry.substring(percentIdx + 1) : entry;
                        if (!blockName.isEmpty()) {
                           blockNames.add(blockName);
                        }
                     }
                  }

                  if (this.additionalBlocksArg != null && !this.additionalBlocksArg.isEmpty()) {
                     for(String block : this.additionalBlocksArg.split(",")) {
                        String trimmed = block.trim();
                        if (!trimmed.isEmpty()) {
                           blockNames.add(trimmed);
                        }
                     }
                  }

                  if (blockNames.isEmpty()) {
                     brushConfig.setErrorFlag("AppendMaskFromToolArg: No blocks to add to mask");
                  } else {
                     BlockFilter filter = new BlockFilter(this.filterTypeArg, (String[])blockNames.toArray(new String[0]), this.invertArg);
                     BlockMask mask = new BlockMask(new BlockFilter[]{filter});
                     brushConfig.appendOperationMask(mask);
                  }
               }
            } else {
               brushConfig.setErrorFlag("AppendMaskFromToolArg: Tool arg '" + this.argNameArg + "' not found");
            }
         }
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AppendMaskFromToolArgOperation.class, AppendMaskFromToolArgOperation::new).append(new KeyedCodec("ArgName", Codec.STRING, true), (op, val) -> op.argNameArg = val, (op) -> op.argNameArg).documentation("The name of the Block tool arg to read the material from").add()).append(new KeyedCodec("FilterType", new EnumCodec(BlockFilter.FilterType.class)), (op, val) -> op.filterTypeArg = val, (op) -> op.filterTypeArg).documentation("The type of block filter mask to apply (e.g., TARGET_BLOCK, ABOVE_BLOCK, BELOW_BLOCK)").add()).append(new KeyedCodec("Invert", Codec.BOOLEAN, true), (op, val) -> op.invertArg = val, (op) -> op.invertArg).documentation("Whether to invert the block filter mask or not").add()).append(new KeyedCodec("AdditionalBlocks", Codec.STRING), (op, val) -> op.additionalBlocksArg = val, (op) -> op.additionalBlocksArg).documentation("Additional block names to append to the mask, comma separated (e.g., Rock_Stone,Rock_Granite)").add()).documentation("Append a mask from a Block tool arg with configurable filter type and optional additional blocks")).build();
   }
}
