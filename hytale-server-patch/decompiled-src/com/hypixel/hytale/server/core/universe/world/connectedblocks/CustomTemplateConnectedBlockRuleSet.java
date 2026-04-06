package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.World;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

public class CustomTemplateConnectedBlockRuleSet extends ConnectedBlockRuleSet {
   public static final BuilderCodec<CustomTemplateConnectedBlockRuleSet> CODEC;
   private String shapeAssetId;
   private Map<String, BlockPattern> shapeNameToBlockPatternMap = new Object2ObjectOpenHashMap();
   private final Int2ObjectMap<Set<String>> shapesPerBlockType = new Int2ObjectOpenHashMap();

   public Map<String, BlockPattern> getShapeNameToBlockPatternMap() {
      return this.shapeNameToBlockPatternMap;
   }

   public void updateCachedBlockTypes(BlockType blockType, BlockTypeAssetMap<String, BlockType> assetMap) {
      super.updateCachedBlockTypes(blockType, assetMap);

      for(Map.Entry<String, BlockPattern> entry : this.shapeNameToBlockPatternMap.entrySet()) {
         String name = (String)entry.getKey();
         BlockPattern blockPattern = (BlockPattern)entry.getValue();
         Integer[] var7 = blockPattern.getResolvedKeys();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            int resolvedKey = var7[var9];
            Set<String> shapes = (Set)this.shapesPerBlockType.computeIfAbsent(resolvedKey, (k) -> new ObjectOpenHashSet());
            shapes.add(name);
         }
      }

   }

   @Nullable
   public Set<String> getShapesForBlockType(int blockTypeKey) {
      return (Set)this.shapesPerBlockType.getOrDefault(blockTypeKey, Set.of());
   }

   @Nullable
   public CustomConnectedBlockTemplateAsset getShapeTemplateAsset() {
      return (CustomConnectedBlockTemplateAsset)CustomConnectedBlockTemplateAsset.getAssetMap().getAsset(this.shapeAssetId);
   }

   public boolean onlyUpdateOnPlacement() {
      CustomConnectedBlockTemplateAsset templateAsset = this.getShapeTemplateAsset();
      return templateAsset != null && templateAsset.isDontUpdateAfterInitialPlacement();
   }

   public Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockType(World world, Vector3i testedCoordinate, BlockType blockType, int rotation, Vector3i placementNormal, boolean isPlacement) {
      CustomConnectedBlockTemplateAsset shapeTemplateAsset = this.getShapeTemplateAsset();
      return shapeTemplateAsset == null ? Optional.empty() : shapeTemplateAsset.getConnectedBlockType(world, testedCoordinate, this, blockType, rotation, placementNormal, true, isPlacement);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(CustomTemplateConnectedBlockRuleSet.class, CustomTemplateConnectedBlockRuleSet::new).append(new KeyedCodec("TemplateShapeAssetId", Codec.STRING), (ruleSet, shapeAssetId) -> ruleSet.shapeAssetId = shapeAssetId, (ruleSet) -> ruleSet.shapeAssetId).addValidator(CustomConnectedBlockTemplateAsset.VALIDATOR_CACHE.getValidator()).documentation("The name of a ConnectedBlockTemplateAsset asset").add()).append(new KeyedCodec("TemplateShapeBlockPatterns", new MapCodec(BlockPattern.CODEC, HashMap::new), true), (material, shapeNameToBlockPatternMap) -> material.shapeNameToBlockPatternMap = shapeNameToBlockPatternMap, (material) -> material.shapeNameToBlockPatternMap).documentation("You must specify all shapes as a BlockPattern. The shapes are as outlined in the keys of the ShapeTemplateAsset's map.").add()).build();
   }
}
