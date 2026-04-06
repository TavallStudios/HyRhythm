package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin.RoofConnectedBlockRuleSet;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.builtin.StairConnectedBlockRuleSet;
import javax.annotation.Nonnull;

public class ConnectedBlocksModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(ConnectedBlocksModule.class).depends(EntityModule.class).depends(InteractionModule.class).build();
   private static ConnectedBlocksModule instance;

   public static ConnectedBlocksModule get() {
      return instance;
   }

   public ConnectedBlocksModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   protected void setup() {
      AssetRegistry.register(((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(CustomConnectedBlockTemplateAsset.class, new DefaultAssetMap()).setPath("Item/CustomConnectedBlockTemplates")).setKeyFunction(CustomConnectedBlockTemplateAsset::getId)).setCodec(CustomConnectedBlockTemplateAsset.CODEC)).build());
      this.getEventRegistry().register((Class)LoadedAssetsEvent.class, BlockType.class, ConnectedBlocksModule::onBlockTypesChanged);
      CustomTemplateConnectedBlockPattern.CODEC.register((String)"Custom", CustomConnectedBlockPattern.class, CustomConnectedBlockPattern.CODEC);
      ConnectedBlockRuleSet.CODEC.register((String)"CustomTemplate", CustomTemplateConnectedBlockRuleSet.class, CustomTemplateConnectedBlockRuleSet.CODEC);
      ConnectedBlockRuleSet.CODEC.register((String)"Stair", StairConnectedBlockRuleSet.class, StairConnectedBlockRuleSet.CODEC);
      ConnectedBlockRuleSet.CODEC.register((String)"Roof", RoofConnectedBlockRuleSet.class, RoofConnectedBlockRuleSet.CODEC);
   }

   private static void onBlockTypesChanged(@Nonnull LoadedAssetsEvent<String, BlockType, BlockTypeAssetMap<String, BlockType>> event) {
      for(BlockType blockType : event.getLoadedAssets().values()) {
         ConnectedBlockRuleSet ruleSet = blockType.getConnectedBlockRuleSet();
         if (ruleSet != null) {
            ruleSet.updateCachedBlockTypes(blockType, event.getAssetMap());
         }
      }

   }
}
