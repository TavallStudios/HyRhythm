package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.sequential;

import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.SequenceBrushOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderTool;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;

public class LoadMaterialFromToolArgOperation extends SequenceBrushOperation {
   public static final BuilderCodec<LoadMaterialFromToolArgOperation> CODEC;
   @Nonnull
   public String argNameArg = "";

   public LoadMaterialFromToolArgOperation() {
      super("Load Material", "Load material pattern from a Block tool arg", false);
   }

   public void modifyBrushConfig(@Nonnull Ref<EntityStore> ref, @Nonnull BrushConfig brushConfig, @Nonnull BrushConfigCommandExecutor brushConfigCommandExecutor, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Player playerComponent = (Player)componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderTool builderTool = BuilderTool.getActiveBuilderTool(playerComponent);
      if (builderTool == null) {
         brushConfig.setErrorFlag("LoadMaterial: No active builder tool");
      } else {
         ItemStack itemStack = playerComponent.getInventory().getItemInHand();
         if (itemStack == null) {
            brushConfig.setErrorFlag("LoadMaterial: No item in hand");
         } else {
            BuilderTool.ArgData argData = builderTool.getItemArgData(itemStack);
            Map<String, Object> toolArgs = argData.tool();
            if (toolArgs != null && toolArgs.containsKey(this.argNameArg)) {
               Object argValue = toolArgs.get(this.argNameArg);
               if (argValue instanceof BlockPattern) {
                  BlockPattern blockPattern = (BlockPattern)argValue;
                  brushConfig.setPattern(blockPattern);
               } else {
                  String var10001 = this.argNameArg;
                  brushConfig.setErrorFlag("LoadMaterial: Tool arg '" + var10001 + "' is not a Block type (found " + argValue.getClass().getSimpleName() + ")");
               }

            } else {
               brushConfig.setErrorFlag("LoadMaterial: Tool arg '" + this.argNameArg + "' not found");
            }
         }
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(LoadMaterialFromToolArgOperation.class, LoadMaterialFromToolArgOperation::new).append(new KeyedCodec("ArgName", Codec.STRING), (op, val) -> op.argNameArg = val, (op) -> op.argNameArg).documentation("The name of the Block tool arg to load the material pattern from").add()).documentation("Load a block pattern from a Block tool arg and set it as the brush material")).build();
   }
}
