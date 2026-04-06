package com.hypixel.hytale.server.core.asset.type.modelvfx.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.CurveType;
import com.hypixel.hytale.protocol.EffectDirection;
import com.hypixel.hytale.protocol.LoopOption;
import com.hypixel.hytale.protocol.SwitchTo;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class ModelVFX implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, ModelVFX>>, NetworkSerializable<com.hypixel.hytale.protocol.ModelVFX> {
   public static final AssetBuilderCodec<String, ModelVFX> CODEC;
   public static final Codec<String> CHILD_ASSET_CODEC;
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   private static AssetStore<String, ModelVFX, IndexedLookupTableAssetMap<String, ModelVFX>> STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   protected AssetExtraInfo.Data data;
   protected String id;
   @Nonnull
   private SwitchTo switchTo;
   @Nonnull
   private EffectDirection effectDirection;
   private float animationDuration;
   private Vector2f animationRange;
   @Nonnull
   private LoopOption loopOption;
   @Nonnull
   private CurveType curveType;
   private Color highlightColor;
   private float highlightThickness;
   private boolean useBloomOnHighlight;
   private boolean useProgressiveHighlight;
   private Vector2f noiseScale;
   private Vector2f noiseScrollSpeed;
   private Color postColor;
   private float postColorOpacity;

   public static AssetStore<String, ModelVFX, IndexedLookupTableAssetMap<String, ModelVFX>> getAssetStore() {
      if (STORE == null) {
         STORE = AssetRegistry.<String, ModelVFX, IndexedLookupTableAssetMap<String, ModelVFX>>getAssetStore(ModelVFX.class);
      }

      return STORE;
   }

   public static IndexedLookupTableAssetMap<String, ModelVFX> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   public ModelVFX(String id, SwitchTo switchTo, EffectDirection effectDirection, float animationDuration, Vector2f animationRange, LoopOption loopOption, CurveType curveType, Color highlightColor, float highlightThickness, boolean useBloomOnHighlight, boolean useProgressiveHighlight, Vector2f noiseScale, Vector2f noiseScrollSpeed, Color postColor, float postColorOpacity) {
      this.switchTo = SwitchTo.Disappear;
      this.effectDirection = EffectDirection.None;
      this.animationRange = new Vector2f(0.0F, 1.0F);
      this.loopOption = LoopOption.PlayOnce;
      this.curveType = CurveType.Linear;
      this.highlightColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.noiseScale = new Vector2f(50.0F, 50.0F);
      this.postColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.postColorOpacity = 1.0F;
      this.id = id;
      this.switchTo = switchTo;
      this.effectDirection = effectDirection;
      this.animationDuration = animationDuration;
      this.animationRange = animationRange;
      this.loopOption = loopOption;
      this.curveType = curveType;
      this.highlightColor = highlightColor;
      this.highlightThickness = highlightThickness;
      this.useBloomOnHighlight = useBloomOnHighlight;
      this.useProgressiveHighlight = useProgressiveHighlight;
      this.noiseScale = noiseScale;
      this.noiseScrollSpeed = noiseScrollSpeed;
      this.postColor = postColor;
      this.postColorOpacity = postColorOpacity;
   }

   public ModelVFX(String id) {
      this.switchTo = SwitchTo.Disappear;
      this.effectDirection = EffectDirection.None;
      this.animationRange = new Vector2f(0.0F, 1.0F);
      this.loopOption = LoopOption.PlayOnce;
      this.curveType = CurveType.Linear;
      this.highlightColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.noiseScale = new Vector2f(50.0F, 50.0F);
      this.postColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.postColorOpacity = 1.0F;
      this.id = id;
   }

   protected ModelVFX() {
      this.switchTo = SwitchTo.Disappear;
      this.effectDirection = EffectDirection.None;
      this.animationRange = new Vector2f(0.0F, 1.0F);
      this.loopOption = LoopOption.PlayOnce;
      this.curveType = CurveType.Linear;
      this.highlightColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.noiseScale = new Vector2f(50.0F, 50.0F);
      this.postColor = new Color((byte)-1, (byte)-1, (byte)-1);
      this.postColorOpacity = 1.0F;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ModelVFX toPacket() {
      com.hypixel.hytale.protocol.ModelVFX packet = new com.hypixel.hytale.protocol.ModelVFX();
      packet.id = this.id;
      packet.switchTo = this.switchTo;
      packet.effectDirection = this.effectDirection;
      packet.animationDuration = this.animationDuration;
      if (this.animationRange != null) {
         packet.animationRange = this.animationRange;
      }

      packet.loopOption = this.loopOption;
      packet.curveType = this.curveType;
      packet.highlightColor = this.highlightColor;
      packet.useBloomOnHighlight = this.useBloomOnHighlight;
      packet.useProgessiveHighlight = this.useProgressiveHighlight;
      packet.highlightThickness = this.highlightThickness;
      if (this.noiseScale != null) {
         packet.noiseScale = this.noiseScale;
      }

      if (this.noiseScrollSpeed != null) {
         packet.noiseScrollSpeed = this.noiseScrollSpeed;
      }

      packet.postColor = this.postColor;
      packet.postColorOpacity = this.postColorOpacity;
      return packet;
   }

   public String getId() {
      return this.id;
   }

   public SwitchTo getSwitchTo() {
      return this.switchTo;
   }

   public EffectDirection getEffectDirection() {
      return this.effectDirection;
   }

   public float getAnimationDuration() {
      return this.animationDuration;
   }

   public Vector2f getAnimationRange() {
      return this.animationRange;
   }

   public LoopOption getLoopOption() {
      return this.loopOption;
   }

   public CurveType getCurveType() {
      return this.curveType;
   }

   public Color getHighlightColor() {
      return this.highlightColor;
   }

   public boolean useBloomOnHighlight() {
      return this.useBloomOnHighlight;
   }

   public boolean useProgessiveHighlight() {
      return this.useProgressiveHighlight;
   }

   public float getHighlightThickness() {
      return this.highlightThickness;
   }

   public Vector2f getNoiseScale() {
      return this.noiseScale;
   }

   public Vector2f getNoiseScrollSpeed() {
      return this.noiseScrollSpeed;
   }

   public Color getPostColor() {
      return this.postColor;
   }

   public float getPostColorOpacity() {
      return this.postColorOpacity;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "ModelVFX{id='" + var10000 + "'SwitchTo=" + String.valueOf(this.switchTo) + ", effectDirection=" + String.valueOf(this.effectDirection) + ", animationDuration=" + this.animationDuration + ", animationRange=" + String.valueOf(this.animationRange) + ", loopOption=" + String.valueOf(this.loopOption) + ", curveType=" + String.valueOf(this.curveType) + ", highlightColor='" + String.valueOf(this.highlightColor) + "', useBloomOnHighlight=" + this.useBloomOnHighlight + ", useProgressiveHighlight=" + this.useProgressiveHighlight + ", highlightThickness=" + this.highlightThickness + ", noiseScale=" + String.valueOf(this.noiseScale) + ", noiseScrollSpeed" + String.valueOf(this.noiseScrollSpeed) + ", postColor='" + String.valueOf(this.postColor) + "', postColorOpacity" + this.postColorOpacity + "}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ModelVFX.class, ModelVFX::new, Codec.STRING, (modelVFX, s) -> modelVFX.id = s, (modelVFX) -> modelVFX.id, (asset, data) -> asset.data = data, (asset) -> asset.data).append(new KeyedCodec("SwitchTo", new EnumCodec(SwitchTo.class)), (modelVFX, s) -> modelVFX.switchTo = s, (modelVFX) -> modelVFX.switchTo).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("EffectDirection", new EnumCodec(EffectDirection.class)), (modelVFX, s) -> modelVFX.effectDirection = s, (modelVFX) -> modelVFX.effectDirection).addValidator(Validators.nonNull()).add()).addField(new KeyedCodec("AnimationDuration", Codec.FLOAT), (modelVFX, d) -> modelVFX.animationDuration = d, (modelVFX) -> modelVFX.animationDuration)).addField(new KeyedCodec("AnimationRange", ProtocolCodecs.VECTOR2F), (modelVFX, d) -> modelVFX.animationRange = d, (modelVFX) -> modelVFX.animationRange)).append(new KeyedCodec("LoopOption", new EnumCodec(LoopOption.class)), (modelVFX, s) -> modelVFX.loopOption = s, (modelVFX) -> modelVFX.loopOption).addValidator(Validators.nonNull()).add()).append(new KeyedCodec("CurveType", new EnumCodec(CurveType.class)), (modelVFX, s) -> modelVFX.curveType = s, (modelVFX) -> modelVFX.curveType).addValidator(Validators.nonNull()).add()).addField(new KeyedCodec("HighlightColor", ProtocolCodecs.COLOR), (modelVFX, o) -> modelVFX.highlightColor = o, (modelVFX) -> modelVFX.highlightColor)).addField(new KeyedCodec("HighlightThickness", Codec.FLOAT), (modelVFX, d) -> modelVFX.highlightThickness = d, (modelVFX) -> modelVFX.highlightThickness)).addField(new KeyedCodec("UseBloomOnHighlight", Codec.BOOLEAN), (modelVFX, b) -> modelVFX.useBloomOnHighlight = b, (modelVFX) -> modelVFX.useBloomOnHighlight)).addField(new KeyedCodec("UseProgessiveHighlight", Codec.BOOLEAN), (modelVFX, b) -> modelVFX.useProgressiveHighlight = b, (modelVFX) -> modelVFX.useProgressiveHighlight)).addField(new KeyedCodec("NoiseScale", ProtocolCodecs.VECTOR2F), (modelVFX, d) -> modelVFX.noiseScale = d, (modelVFX) -> modelVFX.noiseScale)).addField(new KeyedCodec("NoiseScrollSpeed", ProtocolCodecs.VECTOR2F), (modelVFX, d) -> modelVFX.noiseScrollSpeed = d, (modelVFX) -> modelVFX.noiseScrollSpeed)).addField(new KeyedCodec("PostColor", ProtocolCodecs.COLOR), (modelVFX, o) -> modelVFX.postColor = o, (modelVFX) -> modelVFX.postColor)).append(new KeyedCodec("PostColorOpacity", Codec.FLOAT), (modelVFX, d) -> modelVFX.postColorOpacity = d, (modelVFX) -> modelVFX.postColorOpacity).addValidator(Validators.range(0.0F, 1.0F)).add()).build();
      CHILD_ASSET_CODEC = new ContainedAssetCodec(ModelVFX.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ModelVFX::getAssetStore));
   }
}
