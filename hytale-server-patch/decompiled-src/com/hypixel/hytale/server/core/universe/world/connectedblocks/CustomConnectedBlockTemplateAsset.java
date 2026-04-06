package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class CustomConnectedBlockTemplateAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CustomConnectedBlockTemplateAsset>> {
   public static final AssetBuilderCodec<String, CustomConnectedBlockTemplateAsset> CODEC;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, CustomConnectedBlockTemplateAsset, DefaultAssetMap<String, CustomConnectedBlockTemplateAsset>> ASSET_STORE;
   private String id;
   private AssetExtraInfo.Data data;
   protected boolean connectsToOtherMaterials = true;
   private boolean dontUpdateAfterInitialPlacement;
   private String defaultShapeName;
   protected Map<String, ConnectedBlockShape> connectedBlockShapes;

   public static AssetStore<String, CustomConnectedBlockTemplateAsset, DefaultAssetMap<String, CustomConnectedBlockTemplateAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, CustomConnectedBlockTemplateAsset, DefaultAssetMap<String, CustomConnectedBlockTemplateAsset>>getAssetStore(CustomConnectedBlockTemplateAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, CustomConnectedBlockTemplateAsset> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   @Nonnull
   public Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(World world, Vector3i coordinate, CustomTemplateConnectedBlockRuleSet ruleSet, BlockType blockType, int rotation, Vector3i placementNormal, boolean useDefaultShapeIfNoMatch, boolean isPlacement) {
      for(Map.Entry<String, ConnectedBlockShape> entry : this.connectedBlockShapes.entrySet()) {
         ConnectedBlockShape connectedBlockShape = (ConnectedBlockShape)entry.getValue();
         if (connectedBlockShape != null) {
            CustomTemplateConnectedBlockPattern[] patterns = connectedBlockShape.getPatternsToMatchAnyOf();
            if (patterns != null) {
               for(CustomTemplateConnectedBlockPattern connectedBlockPattern : patterns) {
                  Optional<ConnectedBlocksUtil.ConnectedBlockResult> blockRotationIfMatchedOptional = connectedBlockPattern.getConnectedBlockTypeKey((String)entry.getKey(), world, coordinate, ruleSet, blockType, rotation, placementNormal, isPlacement);
                  if (!blockRotationIfMatchedOptional.isEmpty()) {
                     return blockRotationIfMatchedOptional;
                  }
               }
            }
         }
      }

      if (useDefaultShapeIfNoMatch) {
         BlockPattern defaultShapeBlockPattern = (BlockPattern)ruleSet.getShapeNameToBlockPatternMap().get(this.defaultShapeName);
         if (defaultShapeBlockPattern == null) {
            return Optional.empty();
         } else {
            BlockPattern.BlockEntry defaultBlock = defaultShapeBlockPattern.nextBlockTypeKey(ThreadLocalRandom.current());
            return Optional.of(new ConnectedBlocksUtil.ConnectedBlockResult(defaultBlock.blockTypeKey(), rotation));
         }
      } else {
         return Optional.empty();
      }
   }

   public boolean isDontUpdateAfterInitialPlacement() {
      return this.dontUpdateAfterInitialPlacement;
   }

   public String getId() {
      return this.id;
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(CustomConnectedBlockTemplateAsset.class, CustomConnectedBlockTemplateAsset::new, Codec.STRING, (builder, id) -> builder.id = id, (builder) -> builder.id, (builder, data) -> builder.data = data, (builder) -> builder.data).append(new KeyedCodec("DontUpdateAfterInitialPlacement", Codec.BOOLEAN, false), (o, dontUpdateAfterInitialPlacement) -> o.dontUpdateAfterInitialPlacement = dontUpdateAfterInitialPlacement, (o) -> o.dontUpdateAfterInitialPlacement).documentation("Default to false. When true, will not update the connected block after initial placement. Neighboring block updates won't affect this block when true.").add()).append(new KeyedCodec("ConnectsToOtherMaterials", Codec.BOOLEAN, false), (o, connectsToOtherMaterials) -> o.connectsToOtherMaterials = connectsToOtherMaterials, (o) -> o.connectsToOtherMaterials).documentation("Defaults to true. If true, the material will connect to other materials of different block type sets, if false, the material will only connect to its own block types within the material").add()).append(new KeyedCodec("DefaultShape", Codec.STRING, false), (o, defaultShapeName) -> o.defaultShapeName = defaultShapeName, (o) -> o.defaultShapeName).add()).append(new KeyedCodec("Shapes", new MapCodec(ConnectedBlockShape.CODEC, HashMap::new), true), (o, connectedBlockShapes) -> o.connectedBlockShapes = connectedBlockShapes, (o) -> o.connectedBlockShapes).add()).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(CustomConnectedBlockTemplateAsset::getAssetStore));
   }
}
