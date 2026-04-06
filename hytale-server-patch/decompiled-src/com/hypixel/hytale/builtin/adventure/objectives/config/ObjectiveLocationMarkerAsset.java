package com.hypixel.hytale.builtin.adventure.objectives.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.adventure.objectives.config.markerarea.ObjectiveLocationMarkerArea;
import com.hypixel.hytale.builtin.adventure.objectives.config.objectivesetup.ObjectiveTypeSetup;
import com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition.ObjectiveLocationTriggerCondition;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ObjectiveLocationMarkerAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ObjectiveLocationMarkerAsset> CODEC;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, ObjectiveLocationMarkerAsset, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected ObjectiveTypeSetup objectiveTypeSetup;
   protected ObjectiveLocationMarkerArea area;
   protected String[] environmentIds;
   protected int[] environmentIndexes;
   protected ObjectiveLocationTriggerCondition[] triggerConditions;

   public static AssetStore<String, ObjectiveLocationMarkerAsset, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, ObjectiveLocationMarkerAsset, DefaultAssetMap<String, ObjectiveLocationMarkerAsset>>getAssetStore(ObjectiveLocationMarkerAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ObjectiveLocationMarkerAsset> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public ObjectiveTypeSetup getObjectiveTypeSetup() {
      return this.objectiveTypeSetup;
   }

   public ObjectiveLocationMarkerArea getArea() {
      return this.area;
   }

   public String[] getEnvironmentIds() {
      return this.environmentIds;
   }

   public int[] getEnvironmentIndexes() {
      return this.environmentIndexes;
   }

   public ObjectiveLocationTriggerCondition[] getTriggerConditions() {
      return this.triggerConditions;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "ObjectiveLocationMarkerAsset{id='" + var10000 + "', objectiveTypeSetup=" + String.valueOf(this.objectiveTypeSetup) + ", area=" + String.valueOf(this.area) + ", environmentIds=" + Arrays.toString(this.environmentIds) + ", triggerConditions=" + Arrays.toString(this.triggerConditions) + "}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ObjectiveLocationMarkerAsset.class, ObjectiveLocationMarkerAsset::new, Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (asset, data) -> asset.data = data, (asset) -> asset.data).append(new KeyedCodec("Setup", ObjectiveTypeSetup.CODEC), (objectiveLocationMarkerAsset, objectiveTypeSetup) -> objectiveLocationMarkerAsset.objectiveTypeSetup = objectiveTypeSetup, (objectiveLocationMarkerAsset) -> objectiveLocationMarkerAsset.objectiveTypeSetup).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("Area", ObjectiveLocationMarkerArea.CODEC), (objectiveLocationMarkerAsset, area) -> objectiveLocationMarkerAsset.area = area, (objectiveLocationMarkerAsset) -> objectiveLocationMarkerAsset.area).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("EnvironmentIds", Codec.STRING_ARRAY), (objectiveLocationMarkerAsset, strings) -> objectiveLocationMarkerAsset.environmentIds = strings, (objectiveLocationMarkerAsset) -> objectiveLocationMarkerAsset.environmentIds).addValidator(Environment.VALIDATOR_CACHE.getArrayValidator()).add()).append(new KeyedCodec("TriggerConditions", new ArrayCodec(ObjectiveLocationTriggerCondition.CODEC, (x$0) -> new ObjectiveLocationTriggerCondition[x$0])), (objectiveLocationMarkerAsset, objectiveLocationTriggerConditions) -> objectiveLocationMarkerAsset.triggerConditions = objectiveLocationTriggerConditions, (objectiveLocationMarkerAsset) -> objectiveLocationMarkerAsset.triggerConditions).add()).afterDecode((objectiveLocationMarkerAsset) -> {
         if (objectiveLocationMarkerAsset.environmentIds != null && objectiveLocationMarkerAsset.environmentIds.length > 0) {
            objectiveLocationMarkerAsset.environmentIndexes = new int[objectiveLocationMarkerAsset.environmentIds.length];

            for(int i = 0; i < objectiveLocationMarkerAsset.environmentIds.length; ++i) {
               String key = objectiveLocationMarkerAsset.environmentIds[i];
               int index = Environment.getAssetMap().getIndex(key);
               if (index == -2147483648) {
                  throw new IllegalArgumentException("Unknown key! " + key);
               }

               objectiveLocationMarkerAsset.environmentIndexes[i] = index;
            }

            Arrays.sort(objectiveLocationMarkerAsset.environmentIndexes);
         }

      })).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ObjectiveLocationMarkerAsset::getAssetStore));
   }
}
