package com.hypixel.hytale.server.spawning.assets.spawns.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.spawning.assets.spawns.LightType;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public abstract class NPCSpawn {
   public static final float HOURS_PER_DAY = 24.0F;
   public static final BuilderCodec<NPCSpawn> BASE_CODEC;
   public static final double[] DEFAULT_DAY_TIME_RANGE;
   public static final int[] DEFAULT_MOON_PHASE_RANGE;
   public static final double[] FULL_LIGHT_RANGE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected RoleSpawnParameters[] npcs;
   protected DespawnParameters despawnParameters;
   protected String[] environments;
   protected IntSet environmentIds;
   protected double[] dayTimeRange;
   protected int[] moonPhaseRange;
   protected Map<LightType, double[]> lightTypeMap;
   protected boolean scaleDayTimeRange;

   private static void validateLightRange(@Nonnull ValidationResults results, String parameter, @Nonnull double[] lightRange) {
      for(int i = 0; i < lightRange.length; ++i) {
         double value = lightRange[i];
         if (value < 0.0 || value > 100.0) {
            results.fail(String.format("%s must be between 0 and 100 (inclusive)", parameter));
         }

         if (i > 0 && value < lightRange[i - 1]) {
            results.fail(String.format("Values in %s must be weakly monotonic", parameter));
         }
      }

   }

   public NPCSpawn(String id, RoleSpawnParameters[] npcs, DespawnParameters despawnParameters, String[] environments, IntSet environmentIds, double[] dayTimeRange, int[] moonPhaseRange, Map<LightType, double[]> lightTypeMap, boolean scaleDayTimeRange) {
      this.environmentIds = IntSets.EMPTY_SET;
      this.dayTimeRange = DEFAULT_DAY_TIME_RANGE;
      this.moonPhaseRange = DEFAULT_MOON_PHASE_RANGE;
      this.scaleDayTimeRange = true;
      this.id = id;
      this.npcs = npcs;
      this.despawnParameters = despawnParameters;
      this.environments = environments;
      this.environmentIds = environmentIds;
      this.dayTimeRange = dayTimeRange;
      this.moonPhaseRange = moonPhaseRange;
      this.lightTypeMap = lightTypeMap;
      this.scaleDayTimeRange = scaleDayTimeRange;
   }

   protected NPCSpawn(String id) {
      this.environmentIds = IntSets.EMPTY_SET;
      this.dayTimeRange = DEFAULT_DAY_TIME_RANGE;
      this.moonPhaseRange = DEFAULT_MOON_PHASE_RANGE;
      this.scaleDayTimeRange = true;
      this.id = id;
   }

   protected NPCSpawn() {
      this.environmentIds = IntSets.EMPTY_SET;
      this.dayTimeRange = DEFAULT_DAY_TIME_RANGE;
      this.moonPhaseRange = DEFAULT_MOON_PHASE_RANGE;
      this.scaleDayTimeRange = true;
   }

   public abstract String getId();

   public RoleSpawnParameters[] getNPCs() {
      return this.npcs;
   }

   public DespawnParameters getDespawnParameters() {
      return this.despawnParameters;
   }

   public String[] getEnvironments() {
      return this.environments;
   }

   public IntSet getEnvironmentIds() {
      return this.environmentIds;
   }

   public double[] getDayTimeRange() {
      return this.dayTimeRange;
   }

   public int[] getMoonPhaseRange() {
      return this.moonPhaseRange;
   }

   public double[] getLightRange(LightType lightType) {
      if (this.lightTypeMap != null && !this.lightTypeMap.isEmpty()) {
         double[] array = (double[])this.lightTypeMap.get(lightType);
         if (array != null) {
            return array;
         }
      }

      return FULL_LIGHT_RANGE;
   }

   public boolean isScaleDayTimeRange() {
      return this.scaleDayTimeRange;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "NPCSpawn{id='" + var10000 + "', npcs=" + Arrays.deepToString(this.npcs) + ", despawnParameters=" + (this.despawnParameters != null ? this.despawnParameters.toString() : "Null") + ", environments=" + Arrays.toString(this.environments) + ", dayTimeRange=" + Arrays.toString(this.dayTimeRange) + ", moonPhaseRange=" + Arrays.toString(this.moonPhaseRange) + ", lightTypeMap=" + (this.lightTypeMap != null ? (String)this.lightTypeMap.entrySet().stream().map((entry) -> {
         String var10000 = String.valueOf(entry.getKey());
         return var10000 + "=" + Arrays.toString((double[])entry.getValue());
      }).collect(Collectors.joining(", ", "{", "}")) : "Null") + ", scaleDayTimeRange=" + this.scaleDayTimeRange + "}";
   }

   static {
      BASE_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)AssetBuilderCodec.abstractBuilder(NPCSpawn.class).documentation("A specification for spawning NPCs, including spawn and despawn parameters.")).appendInherited(new KeyedCodec("NPCs", new ArrayCodec(RoleSpawnParameters.CODEC, (x$0) -> new RoleSpawnParameters[x$0])), (spawn, o) -> spawn.npcs = o, (spawn) -> spawn.npcs, (spawn, parent) -> spawn.npcs = parent.npcs).documentation("A required list of **Role Spawn Parameters** defining each NPC that can be spawned and their relative weights.").metadata(new UIPropertyTitle("NPCs")).addValidator(Validators.nonNull()).addValidator(Validators.nonEmptyArray()).add()).appendInherited(new KeyedCodec("Despawn", NPCSpawn.DespawnParameters.CODEC), (spawn, o) -> spawn.despawnParameters = o, (spawn) -> spawn.despawnParameters, (spawn, parent) -> spawn.despawnParameters = parent.despawnParameters).documentation("Optional **Despawn Parameters** to control NPC despawning.").add()).appendInherited(new KeyedCodec("DayTimeRange", Codec.DOUBLE_ARRAY), (spawn, o) -> {
         spawn.dayTimeRange = o;
         double[] var10000 = spawn.dayTimeRange;
         var10000[0] /= 24.0;
         var10000 = spawn.dayTimeRange;
         var10000[1] /= 24.0;
      }, (spawn) -> new double[]{spawn.dayTimeRange[0] * 24.0, spawn.dayTimeRange[1] * 24.0}, (spawn, parent) -> spawn.dayTimeRange = parent.dayTimeRange).documentation("An optional hour range within which the NPCs/beacon will spawn (between 0 and 24).").addValidator(Validators.doubleArraySize(2)).add()).appendInherited(new KeyedCodec("MoonPhaseRange", Codec.INT_ARRAY), (spawn, o) -> spawn.moonPhaseRange = o, (spawn) -> new int[]{spawn.moonPhaseRange[0], spawn.moonPhaseRange[1]}, (spawn, parent) -> spawn.moonPhaseRange = parent.moonPhaseRange).documentation("An optional moon phase range during which the NPCs/beacon will spawn (must be greater than or equal to 0).").addValidator(Validators.intArraySize(2)).add()).appendInherited(new KeyedCodec("LightRanges", (new EnumMapCodec(LightType.class, Codec.DOUBLE_ARRAY)).documentKey(LightType.Light, "Total light level.").documentKey(LightType.SkyLight, "Light level based on how deep under cover the position is relative to the open sky (e.g. inside a cave will be low SkyLight).").documentKey(LightType.Sunlight, "Light level based on time of day (peaks at around noon and is 0 during most of the night).").documentKey(LightType.RedLight, "Red light level.").documentKey(LightType.GreenLight, "Green light level.").documentKey(LightType.BlueLight, "Blue light level.")), (spawn, o) -> spawn.lightTypeMap = o, (spawn) -> spawn.lightTypeMap, (spawn, parent) -> spawn.lightTypeMap = parent.lightTypeMap).documentation("Optional light ranges to spawn the NPCs/beacon in, defined between 0 and 100.").add()).appendInherited(new KeyedCodec("ScaleDayTimeRange", Codec.BOOLEAN), (spawn, b) -> spawn.scaleDayTimeRange = b, (spawn) -> spawn.scaleDayTimeRange, (spawn, parent) -> spawn.scaleDayTimeRange = parent.scaleDayTimeRange).documentation("If set to true, instead of using absolute hour values for DayTimeRange, it will be scaled based on the world's DaytimePortion.\n\n * 0 and 24 will represent the middle of the night portion.\n * 6 will represent the moment of sunrise.\n * 12 will represent the middle of the day portion.\n * 18 will represent the moment of sunset.").add()).afterDecode((spawn) -> {
         if (spawn.environments != null && spawn.environments.length != 0) {
            IntOpenHashSet environmentSet = new IntOpenHashSet();
            IndexedLookupTableAssetMap<String, Environment> environments = Environment.getAssetMap();

            for(String environment : spawn.environments) {
               int index = environments.getIndex(environment);
               if (index != -2147483648) {
                  environmentSet.add(index);
               }
            }

            environmentSet.trim();
            spawn.environmentIds = IntSets.unmodifiable(environmentSet);
         } else {
            spawn.environmentIds = IntSets.EMPTY_SET;
         }

      })).validator((asset, results) -> {
         double[] dayTimeRange = asset.getDayTimeRange();
         if (dayTimeRange[0] < 0.0 || dayTimeRange[1] < 0.0) {
            results.fail("DayTimeRange values must be >=0");
         }

         int[] moonPhaseRange = asset.getMoonPhaseRange();
         if (moonPhaseRange[0] < 0 || moonPhaseRange[1] < 0) {
            results.fail("MoonPhaseRange values must be >=0");
         }

         for(LightType lightType : LightType.VALUES) {
            validateLightRange(results, lightType.name(), asset.getLightRange(lightType));
         }

         DespawnParameters despawnParameters = asset.getDespawnParameters();
         if (despawnParameters != null) {
            dayTimeRange = despawnParameters.getDayTimeRange();
            if (dayTimeRange[0] < 0.0 || dayTimeRange[1] < 0.0) {
               results.fail("Despawn DayTimeRange values must be >=0");
            }

            moonPhaseRange = despawnParameters.getMoonPhaseRange();
            if (moonPhaseRange[0] < 0 || moonPhaseRange[1] < 0) {
               results.fail("Despawn MoonPhaseRange values must be >=0");
            }
         }

      })).build();
      DEFAULT_DAY_TIME_RANGE = new double[]{0.0, 1.7976931348623157E308};
      DEFAULT_MOON_PHASE_RANGE = new int[]{0, 2147483647};
      FULL_LIGHT_RANGE = new double[]{0.0, 100.0};
   }

   public static class DespawnParameters {
      public static final BuilderCodec<DespawnParameters> CODEC;
      protected double[] dayTimeRange;
      protected int[] moonPhaseRange;

      public DespawnParameters(double[] dayTimeRange, int[] moonPhaseRange) {
         this.dayTimeRange = NPCSpawn.DEFAULT_DAY_TIME_RANGE;
         this.moonPhaseRange = NPCSpawn.DEFAULT_MOON_PHASE_RANGE;
         this.dayTimeRange = dayTimeRange;
         this.moonPhaseRange = moonPhaseRange;
      }

      protected DespawnParameters() {
         this.dayTimeRange = NPCSpawn.DEFAULT_DAY_TIME_RANGE;
         this.moonPhaseRange = NPCSpawn.DEFAULT_MOON_PHASE_RANGE;
      }

      public double[] getDayTimeRange() {
         return this.dayTimeRange;
      }

      public int[] getMoonPhaseRange() {
         return this.moonPhaseRange;
      }

      @Nonnull
      public String toString() {
         String var10000 = Arrays.toString(this.dayTimeRange);
         return "DespawnParameters{dayTimeRange=" + var10000 + ", moonPhaseRange=" + Arrays.toString(this.moonPhaseRange) + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DespawnParameters.class, DespawnParameters::new).documentation("A set of parameters that determine if NPCs should despawn.")).append(new KeyedCodec("DayTimeRange", Codec.DOUBLE_ARRAY), (parameters, o) -> {
            parameters.dayTimeRange = o;
            double[] var10000 = parameters.dayTimeRange;
            var10000[0] /= 24.0;
            var10000 = parameters.dayTimeRange;
            var10000[1] /= 24.0;
         }, (parameters) -> new double[]{parameters.dayTimeRange[0] * 24.0, parameters.dayTimeRange[1] * 24.0}).documentation("An optional hour range within which the NPCs will despawn (between 0 and 24). For Spawn Beacons, this refers to the beacon itself.").addValidator(Validators.doubleArraySize(2)).add()).append(new KeyedCodec("MoonPhaseRange", Codec.INT_ARRAY), (parameters, o) -> parameters.moonPhaseRange = o, (parameters) -> new int[]{parameters.moonPhaseRange[0], parameters.moonPhaseRange[1]}).documentation("An optional moon phase range during which the NPCs will despawn (must be greater than or equal to 0). For Spawn Beacons, this refers to the beacon itself.").addValidator(Validators.intArraySize(2)).add()).build();
      }
   }
}
