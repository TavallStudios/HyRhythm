package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.EnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.TintProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.VectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DensityAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, DensityAsset>>, Cleanable {
   @Nonnull
   private static final DensityAsset[] EMPTY_INPUTS = new DensityAsset[0];
   @Nonnull
   public static final AssetCodecMapCodec<String, DensityAsset> CODEC;
   @Nonnull
   private static final Map<String, Exported> exportedNodes;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final BuilderCodec<DensityAsset> ABSTRACT_CODEC;
   private String id;
   private AssetExtraInfo.Data data;
   private DensityAsset[] inputs;
   private boolean skip;
   protected String exportName;

   protected DensityAsset() {
      this.inputs = EMPTY_INPUTS;
      this.skip = false;
      this.exportName = "";
   }

   @Nonnull
   public abstract Density build(@Nonnull Argument var1);

   public void cleanUp() {
      this.cleanUpInputs();
   }

   protected void cleanUpInputs() {
      for(DensityAsset input : this.inputs) {
         input.cleanUp();
      }

   }

   @Nonnull
   public static DensityAsset getFallbackAsset() {
      return new ConstantDensityAsset();
   }

   @Nonnull
   public Density buildWithInputs(@Nonnull Argument argument, @Nonnull Density[] inputs) {
      Density node = this.build(argument);
      node.setInputs(inputs);
      return node;
   }

   public DensityAsset[] inputs() {
      return this.inputs;
   }

   @Nonnull
   public List<Density> buildInputs(@Nonnull Argument argument, boolean excludeSkipped) {
      ArrayList<Density> nodes = new ArrayList();

      for(DensityAsset asset : this.inputs) {
         if (!excludeSkipped || !asset.isSkipped()) {
            nodes.add(asset.build(argument));
         }
      }

      return nodes;
   }

   @Nonnull
   public Density[] buildInputsArray(@Nonnull Argument argument) {
      Density[] nodes = new Density[this.inputs.length];
      int i = 0;

      for(DensityAsset asset : this.inputs) {
         nodes[i++] = asset.build(argument);
      }

      return nodes;
   }

   @Nullable
   public DensityAsset firstInput() {
      return this.inputs.length > 0 ? this.inputs[0] : null;
   }

   @Nullable
   public DensityAsset secondInput() {
      return this.inputs.length > 1 ? this.inputs[1] : null;
   }

   @Nullable
   public Density buildFirstInput(@Nonnull Argument argument) {
      return this.firstInput() == null ? null : this.firstInput().build(argument);
   }

   @Nullable
   public Density buildSecondInput(@Nonnull Argument argument) {
      return this.secondInput() == null ? null : this.secondInput().build(argument);
   }

   public boolean isSkipped() {
      return this.skip;
   }

   public static Exported getExportedAsset(@Nonnull String name) {
      return (Exported)exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public static Argument from(@Nonnull VectorProviderAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull MaterialProviderAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull PropAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull PatternAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull PositionProviderAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull AssignmentsAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull WorldStructureAsset.Argument argument, @Nonnull ReferenceBundle referenceBundle) {
      return new Argument(argument.parentSeed, referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull EnvironmentProviderAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static Argument from(@Nonnull TintProviderAsset.Argument argument) {
      return new Argument(argument.parentSeed, argument.referenceBundle, argument.workerId);
   }

   static {
      CODEC = new AssetCodecMapCodec<String, DensityAsset>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.data = data, (t) -> t.data);
      exportedNodes = new ConcurrentHashMap();
      CHILD_ASSET_CODEC = new ContainedAssetCodec(DensityAsset.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      ABSTRACT_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(DensityAsset.class).append(new KeyedCodec("Inputs", new ArrayCodec(CODEC, (x$0) -> new DensityAsset[x$0]), true), (t, k) -> t.inputs = k, (t) -> t.inputs).add()).append(new KeyedCodec("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, (t) -> t.skip).add()).append(new KeyedCodec("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, (t) -> t.exportName).add()).afterDecode((asset) -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            if (exportedNodes.containsKey(asset.exportName)) {
               LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
            }

            boolean isSingleInstance;
            if (asset instanceof ExportedDensityAsset) {
               ExportedDensityAsset exportedAsset = (ExportedDensityAsset)asset;
               isSingleInstance = exportedAsset.isSingleInstance();
            } else {
               isSingleInstance = false;
            }

            Exported exported = new Exported(isSingleInstance, asset);
            exportedNodes.put(asset.exportName, exported);
            LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }

      })).build();
   }

   public static class Exported {
      public boolean isSingleInstance;
      @Nonnull
      public DensityAsset asset;
      @Nonnull
      public Map<WorkerIndexer.Id, Density> threadInstances;

      public Exported(boolean i, @Nonnull DensityAsset asset) {
         this.isSingleInstance = i;
         this.asset = asset;
         this.threadInstances = new ConcurrentHashMap();
      }
   }

   public static class Argument {
      public SeedBox parentSeed;
      public ReferenceBundle referenceBundle;
      public WorkerIndexer.Id workerId;

      public Argument(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
         this.parentSeed = parentSeed;
         this.referenceBundle = referenceBundle;
         this.workerId = workerId;
      }

      public Argument(@Nonnull Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.referenceBundle = argument.referenceBundle;
         this.workerId = argument.workerId;
      }
   }
}
