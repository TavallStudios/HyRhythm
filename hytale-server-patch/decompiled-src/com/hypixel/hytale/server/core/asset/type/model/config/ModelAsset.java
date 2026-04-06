package com.hypixel.hytale.server.core.asset.type.model.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.array.IntArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIButton;
import com.hypixel.hytale.codec.schema.metadata.ui.UICreateButtons;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorPreview;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditorSectionStart;
import com.hypixel.hytale.codec.schema.metadata.ui.UIRebuildCaches;
import com.hypixel.hytale.codec.schema.metadata.ui.UISidebarButtons;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.IntArrayValidator;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.MapUtil;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.EntityPart;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.protocol.Phobia;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraAxis;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.asset.type.trail.config.Trail;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ModelAsset>> {
   public static final BuilderCodec<ModelTrail> MODEL_TRAIL_CODEC;
   public static final ArrayCodec<ModelTrail> MODEL_TRAIL_ARRAY_CODEC;
   public static final AssetBuilderCodec<String, ModelAsset> CODEC;
   public static final ModelAsset DEBUG;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected String model;
   protected String texture;
   protected String gradientSet;
   protected String gradientId;
   protected float eyeHeight;
   protected float crouchOffset;
   protected float sittingOffset;
   protected float sleepingOffset;
   protected Map<String, AnimationSet> animationSetMap = Collections.emptyMap();
   protected CameraSettings camera;
   protected Box boundingBox;
   protected ColorLight light;
   protected ModelParticle[] particles;
   protected ModelTrail[] trails;
   protected PhysicsValues physicsValues = new PhysicsValues(68.0, 0.5, false);
   protected ModelAttachment[] defaultAttachments;
   protected Map<String, Map<String, ModelAttachment>> randomAttachmentSets;
   protected float minScale = 0.95F;
   protected float maxScale = 1.05F;
   protected String icon;
   protected AssetIconProperties iconProperties;
   protected Map<String, DetailBox[]> detailBoxes = Collections.emptyMap();
   protected Map<String, IWeightedMap<String>> weightedRandomAttachmentSets;
   @Nonnull
   protected Phobia phobia;
   protected String phobiaModelAssetId;

   public ModelAsset() {
      this.phobia = Phobia.None;
   }

   public static AssetStore<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, ModelAsset, DefaultAssetMap<String, ModelAsset>>getAssetStore(ModelAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ModelAsset> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public String getModel() {
      return this.model;
   }

   public String getTexture() {
      return this.texture;
   }

   public String getGradientId() {
      return this.gradientId;
   }

   public String getGradientSet() {
      return this.gradientSet;
   }

   public float getEyeHeight() {
      return this.eyeHeight;
   }

   public float getCrouchOffset() {
      return this.crouchOffset;
   }

   public float getSittingOffset() {
      return this.sittingOffset;
   }

   public float getSleepingOffset() {
      return this.sleepingOffset;
   }

   public Map<String, AnimationSet> getAnimationSetMap() {
      return this.animationSetMap;
   }

   public CameraSettings getCamera() {
      return this.camera;
   }

   @Nonnull
   public Box getBoundingBox() {
      return this.boundingBox;
   }

   public ColorLight getLight() {
      return this.light;
   }

   public ModelParticle[] getParticles() {
      return this.particles;
   }

   public ModelTrail[] getTrails() {
      return this.trails;
   }

   public PhysicsValues getPhysicsValues() {
      return this.physicsValues;
   }

   public ModelAttachment[] getDefaultAttachments() {
      return this.defaultAttachments;
   }

   public Map<String, Map<String, ModelAttachment>> getRandomAttachmentSets() {
      return this.randomAttachmentSets;
   }

   public float getMinScale() {
      return this.minScale;
   }

   public float getMaxScale() {
      return this.maxScale;
   }

   public AssetIconProperties getIconProperties() {
      return this.iconProperties;
   }

   public String getIcon() {
      return this.icon;
   }

   public float generateRandomScale() {
      return MathUtil.randomFloat(this.minScale, this.maxScale);
   }

   @Nullable
   public Map<String, String> generateRandomAttachmentIds() {
      if (this.weightedRandomAttachmentSets == null) {
         return null;
      } else {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         Map<String, String> randomAttachmentIds = new Object2ObjectOpenHashMap();

         for(Map.Entry<String, IWeightedMap<String>> entry : this.weightedRandomAttachmentSets.entrySet()) {
            String attachmentSetId = (String)entry.getKey();
            String attachmentId = (String)((IWeightedMap)entry.getValue()).get((Random)random);
            if (attachmentId != null) {
               randomAttachmentIds.put(attachmentSetId, attachmentId);
            }
         }

         return randomAttachmentIds;
      }
   }

   public ModelAttachment[] getAttachments(@Nullable Map<String, String> randomAttachmentIds) {
      if (randomAttachmentIds != null && !randomAttachmentIds.isEmpty() && this.randomAttachmentSets != null) {
         List<ModelAttachment> attachments = new ObjectArrayList((this.defaultAttachments == null ? 0 : this.defaultAttachments.length) + randomAttachmentIds.size());
         if (this.defaultAttachments != null) {
            Collections.addAll(attachments, this.defaultAttachments);
         }

         for(Map.Entry<String, String> entry : randomAttachmentIds.entrySet()) {
            Map<String, ModelAttachment> attachmentSet = (Map)this.randomAttachmentSets.get(entry.getKey());
            if (attachmentSet != null) {
               ModelAttachment attachment = (ModelAttachment)attachmentSet.get(entry.getValue());
               if (attachment != null && attachment.getModel() != null && attachment.getTexture() != null) {
                  attachments.add(attachment);
               }
            }
         }

         return (ModelAttachment[])attachments.toArray((x$0) -> new ModelAttachment[x$0]);
      } else {
         return this.defaultAttachments;
      }
   }

   public Map<String, DetailBox[]> getDetailBoxes() {
      return this.detailBoxes;
   }

   public Phobia getPhobia() {
      return this.phobia;
   }

   public String getPhobiaModelAssetId() {
      return this.phobiaModelAssetId;
   }

   public String toString() {
      String var10000 = this.id;
      return "ModelAsset{id='" + var10000 + "', model='" + this.model + "', texture='" + this.texture + "', gradientSet='" + this.gradientSet + "', gradientId='" + this.gradientId + "', eyeHeight=" + this.eyeHeight + ", crouchOffset=" + this.crouchOffset + ", sittingOffset=" + this.sittingOffset + ", sleepingOffset=" + this.sleepingOffset + ", animationSetMap=" + String.valueOf(this.animationSetMap) + ", camera=" + String.valueOf(this.camera) + ", boundingBox=" + String.valueOf(this.boundingBox) + ", light=" + String.valueOf(this.light) + ", particles=" + Arrays.toString(this.particles) + ", trails=" + Arrays.toString(this.trails) + ", physicsValues=" + String.valueOf(this.physicsValues) + ", defaultAttachments=" + Arrays.toString(this.defaultAttachments) + ", randomAttachmentSets=" + String.valueOf(this.randomAttachmentSets) + ", minScale=" + this.minScale + ", maxScale=" + this.maxScale + ", icon='" + this.icon + "', iconProperties=" + String.valueOf(this.iconProperties) + ", detailBoxes=" + String.valueOf(this.detailBoxes) + ", weightedRandomAttachmentSets=" + String.valueOf(this.weightedRandomAttachmentSets) + ", phobia=" + String.valueOf(this.phobia) + ", phobiaModelAssetId='" + this.phobiaModelAssetId + "'}";
   }

   static {
      MODEL_TRAIL_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ModelTrail.class, ModelTrail::new).append(new KeyedCodec("TrailId", new ContainedAssetCodec(Trail.class, Trail.CODEC)), (trail, s) -> trail.trailId = s, (trail) -> trail.trailId).addValidator(Trail.VALIDATOR_CACHE.getValidator()).add()).addField(new KeyedCodec("TargetEntityPart", new EnumCodec(EntityPart.class)), (trail, s) -> trail.targetEntityPart = s, (trail) -> trail.targetEntityPart)).addField(new KeyedCodec("TargetNodeName", Codec.STRING), (trail, s) -> trail.targetNodeName = s, (trail) -> trail.targetNodeName)).addField(new KeyedCodec("PositionOffset", ProtocolCodecs.VECTOR3F), (trail, s) -> trail.positionOffset = s, (trail) -> trail.positionOffset)).addField(new KeyedCodec("RotationOffset", ProtocolCodecs.DIRECTION), (trail, s) -> trail.rotationOffset = s, (trail) -> trail.rotationOffset)).addField(new KeyedCodec("FixedRotation", Codec.BOOLEAN), (trail, s) -> trail.fixedRotation = s, (trail) -> trail.fixedRotation)).build();
      MODEL_TRAIL_ARRAY_CODEC = new ArrayCodec<ModelTrail>(MODEL_TRAIL_CODEC, (x$0) -> new ModelTrail[x$0]);
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ModelAsset.class, ModelAsset::new, Codec.STRING, (modelAsset, s) -> modelAsset.id = s, (modelAsset) -> modelAsset.id, (modelAsset, data) -> modelAsset.extraData = data, (modelAsset) -> modelAsset.extraData).metadata(new UIEditorPreview(UIEditorPreview.PreviewType.MODEL))).metadata(new UISidebarButtons(new UIButton[]{new UIButton("server.assetEditor.buttons.resetModel", "ResetModel"), new UIButton("server.assetEditor.buttons.useModel", "UseModel")}))).metadata(new UICreateButtons(new UIButton[]{new UIButton("server.assetEditor.buttons.createAndUseModel", "UseModel")}))).appendInherited(new KeyedCodec("Model", Codec.STRING), (model, s) -> model.model = s, (model) -> model.model, (model, parent) -> model.model = parent.model).addValidator(CommonAssetValidator.MODEL_CHARACTER).add()).appendInherited(new KeyedCodec("Texture", Codec.STRING), (model, s) -> model.texture = s, (model) -> model.texture, (model, parent) -> model.texture = parent.texture).addValidator(CommonAssetValidator.TEXTURE_CHARACTER).add()).appendInherited(new KeyedCodec("GradientSet", Codec.STRING), (model, s) -> model.gradientSet = s, (model) -> model.gradientSet, (model, parent) -> model.gradientSet = parent.gradientSet).metadata(new UIEditor(new UIEditor.Dropdown("GradientSets"))).add()).appendInherited(new KeyedCodec("GradientId", Codec.STRING), (model, s) -> model.gradientId = s, (model) -> model.gradientId, (model, parent) -> model.gradientId = parent.gradientId).metadata(new UIEditor(new UIEditor.Dropdown("GradientIds"))).add()).appendInherited(new KeyedCodec("Icon", Codec.STRING), (item, s) -> item.icon = s, (item) -> item.icon, (item, parent) -> item.icon = parent.icon).addValidator(CommonAssetValidator.ICON_MODEL).metadata(new UIEditor(new UIEditor.Icon("Icons/ModelsGenerated/{assetId}.png", 128, 128))).metadata(new UIRebuildCaches(new UIRebuildCaches.ClientCache[]{UIRebuildCaches.ClientCache.ITEM_ICONS})).add()).appendInherited(new KeyedCodec("IconProperties", AssetIconProperties.CODEC), (item, s) -> item.iconProperties = s, (item) -> item.iconProperties, (item, parent) -> item.iconProperties = parent.iconProperties).metadata(UIDisplayMode.HIDDEN).add()).appendInherited(new KeyedCodec("Light", ProtocolCodecs.COLOR_LIGHT), (model, l) -> model.light = l, (model) -> model.light, (model, parent) -> model.light = parent.light).add()).appendInherited(new KeyedCodec("PhysicsValues", PhysicsValues.CODEC), (model, l) -> model.physicsValues = l, (model) -> model.physicsValues, (model, parent) -> model.physicsValues = parent.physicsValues).add()).appendInherited(new KeyedCodec("MinScale", Codec.DOUBLE), (modelAsset, d) -> modelAsset.minScale = d.floatValue(), (modelAsset) -> (double)modelAsset.minScale, (modelAsset, parent) -> modelAsset.minScale = parent.minScale).metadata(new UIEditorSectionStart("Hitbox")).add()).appendInherited(new KeyedCodec("MaxScale", Codec.DOUBLE), (modelAsset, d) -> modelAsset.maxScale = d.floatValue(), (modelAsset) -> (double)modelAsset.maxScale, (modelAsset, parent) -> modelAsset.maxScale = parent.maxScale).add()).appendInherited(new KeyedCodec("EyeHeight", Codec.DOUBLE), (model, d) -> model.eyeHeight = d.floatValue(), (model) -> (double)model.eyeHeight, (model, parent) -> model.eyeHeight = parent.eyeHeight).add()).appendInherited(new KeyedCodec("HitBox", Box.CODEC), (model, o) -> model.boundingBox = o, (model) -> model.boundingBox, (model, parent) -> model.boundingBox = parent.boundingBox).addValidator(Validators.nonNull()).add()).appendInherited(new KeyedCodec("DetailBoxes", new MapCodec(new ArrayCodec(DetailBox.CODEC, (x$0) -> new DetailBox[x$0]), HashMap::new)), (o, i) -> o.detailBoxes = i, (o) -> o.detailBoxes, (o, p) -> o.detailBoxes = p.detailBoxes).add()).appendInherited(new KeyedCodec("CrouchOffset", Codec.DOUBLE), (model, d) -> model.crouchOffset = d.floatValue(), (model) -> (double)model.crouchOffset, (model, parent) -> model.crouchOffset = parent.crouchOffset).add()).appendInherited(new KeyedCodec("SittingOffset", Codec.DOUBLE), (model, d) -> model.sittingOffset = d.floatValue(), (model) -> (double)model.sittingOffset, (model, parent) -> model.sittingOffset = parent.sittingOffset).add()).appendInherited(new KeyedCodec("SleepingOffset", Codec.DOUBLE), (model, d) -> model.sleepingOffset = d.floatValue(), (model) -> (double)model.sleepingOffset, (model, parent) -> model.sleepingOffset = parent.sleepingOffset).metadata(new UIEditorSectionStart("Camera")).add()).appendInherited(new KeyedCodec("Camera", CameraSettings.CODEC), (model, o) -> model.camera = o, (model) -> model.camera, (model, parent) -> model.camera = parent.camera).add()).appendInherited(new KeyedCodec("DefaultAttachments", new ArrayCodec(ModelAttachment.CODEC, (x$0) -> new ModelAttachment[x$0])), (modelAsset, l) -> modelAsset.defaultAttachments = l, (modelAsset) -> modelAsset.defaultAttachments, (modelAsset, parent) -> modelAsset.defaultAttachments = parent.defaultAttachments).metadata(new UIEditorSectionStart("Attachments")).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("RandomAttachmentSets", new MapCodec(new MapCodec(ModelAttachment.CODEC, HashMap::new), HashMap::new)), (modelAsset, l) -> modelAsset.randomAttachmentSets = l, (modelAsset) -> modelAsset.randomAttachmentSets, (modelAsset, parent) -> modelAsset.randomAttachmentSets = parent.randomAttachmentSets).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("AnimationSets", new MapCodec(ModelAsset.AnimationSet.CODEC, HashMap::new)), (model, m) -> model.animationSetMap = MapUtil.<String, AnimationSet>combineUnmodifiable(model.animationSetMap, m), (model) -> model.animationSetMap, (model, parent) -> model.animationSetMap = parent.animationSetMap).metadata(new UIEditorSectionStart("Animations")).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("Particles", ModelParticle.ARRAY_CODEC), (model, l) -> model.particles = l, (model) -> model.particles, (model, parent) -> model.particles = parent.particles).metadata(new UIEditorSectionStart("Physics")).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("Trails", MODEL_TRAIL_ARRAY_CODEC), (model, l) -> model.trails = l, (model) -> model.trails, (model, parent) -> model.trails = parent.trails).metadata(new UIEditorSectionStart("Trails")).metadata(UIDefaultCollapsedState.UNCOLLAPSED).add()).appendInherited(new KeyedCodec("Phobia", new EnumCodec(Phobia.class)), (modelAsset, phobia) -> modelAsset.phobia = phobia, (modelAsset) -> modelAsset.phobia, (modelAsset, parent) -> modelAsset.phobia = parent.phobia).addValidator(Validators.nonNull()).documentation("Enum used to specify if the NPC is part of a phobia (e.g. spider for arachnophobia).").add()).appendInherited(new KeyedCodec("PhobiaModelAssetId", Codec.STRING), (modelAsset, s) -> modelAsset.phobiaModelAssetId = s, (modelAsset) -> modelAsset.phobiaModelAssetId, (modelAsset, parent) -> modelAsset.phobiaModelAssetId = parent.phobiaModelAssetId).documentation("The model to use if the player has the setting with the matching phobia toggled on.").addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late()).add()).afterDecode((modelAsset) -> {
         if (modelAsset.randomAttachmentSets != null && !modelAsset.randomAttachmentSets.isEmpty()) {
            Map<String, IWeightedMap<String>> weightedRandomAttachmentSets = new Object2ObjectOpenHashMap();

            for(Map.Entry<String, Map<String, ModelAttachment>> entry : modelAsset.randomAttachmentSets.entrySet()) {
               WeightedMap.Builder<String> builder = WeightedMap.<String>builder(ArrayUtil.EMPTY_STRING_ARRAY);

               for(Map.Entry<String, ModelAttachment> attachmentEntry : ((Map)entry.getValue()).entrySet()) {
                  builder.put((String)attachmentEntry.getKey(), ((ModelAttachment)attachmentEntry.getValue()).weight);
               }

               weightedRandomAttachmentSets.put((String)entry.getKey(), builder.build());
            }

            modelAsset.weightedRandomAttachmentSets = weightedRandomAttachmentSets;
         }

      })).build();
      DEBUG = new ModelAsset() {
         {
            this.id = "Debug";
            this.model = "Blocks/_Debug/Model.blockymodel";
            this.texture = "Characters/_Debug/Texture.png";
            this.camera = new CameraSettings((Vector3f)null, CameraAxis.STATIC_HEAD, CameraAxis.STATIC_HEAD);
            this.boundingBox = new Box(new Vector3d(0.0, 0.0, 0.0), new Vector3d(1.0, 1.0, 1.0));
            this.minScale = 1.0F;
            this.maxScale = 1.0F;
         }
      };
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ModelAsset::getAssetStore));
   }

   public static class AnimationSet {
      public static final BuilderCodec<AnimationSet> CODEC;
      public static final Rangef DEFAULT_NEXT_ANIMATION_DELAY;
      protected Animation[] animations;
      protected Rangef nextAnimationDelay;

      public AnimationSet(Animation[] animations, Rangef nextAnimationDelay) {
         this.nextAnimationDelay = DEFAULT_NEXT_ANIMATION_DELAY;
         this.animations = animations;
         this.nextAnimationDelay = nextAnimationDelay;
      }

      public AnimationSet() {
         this.nextAnimationDelay = DEFAULT_NEXT_ANIMATION_DELAY;
      }

      public Animation[] getAnimations() {
         return this.animations;
      }

      public Rangef getNextAnimationDelay() {
         return this.nextAnimationDelay;
      }

      @Nonnull
      public com.hypixel.hytale.protocol.AnimationSet toPacket(String id) {
         com.hypixel.hytale.protocol.AnimationSet packet = new com.hypixel.hytale.protocol.AnimationSet();
         packet.id = id;
         packet.animations = (com.hypixel.hytale.protocol.Animation[])ArrayUtil.copyAndMutate(this.animations, Animation::toPacket, (x$0) -> new com.hypixel.hytale.protocol.Animation[x$0]);
         packet.nextAnimationDelay = this.nextAnimationDelay;
         return packet;
      }

      @Nonnull
      public String toString() {
         String var10000 = Arrays.toString(this.animations);
         return "AnimationSet{animations=" + var10000 + ", nextAnimationDelay=" + String.valueOf(this.nextAnimationDelay) + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AnimationSet.class, AnimationSet::new).append(new KeyedCodec("Animations", new ArrayCodec(ModelAsset.Animation.CODEC, (x$0) -> new Animation[x$0])), (animationSet, animations) -> animationSet.animations = animations, (animationSet) -> animationSet.animations).addValidator(Validators.nonEmptyArray()).add()).addField(new KeyedCodec("NextAnimationDelay", ProtocolCodecs.RANGEF), (animationSet, rangef) -> animationSet.nextAnimationDelay = rangef, (animationSet) -> animationSet.nextAnimationDelay)).build();
         DEFAULT_NEXT_ANIMATION_DELAY = new Rangef(2.0F, 10.0F);
      }
   }

   public static class Animation {
      public static final BuilderCodec<Animation> CODEC;
      protected String animation;
      protected float speed = 1.0F;
      protected float blendingDuration = 0.2F;
      protected boolean looping = true;
      protected float weight = 1.0F;
      protected int[] footstepIntervals;
      protected String soundEventId;
      protected transient int soundEventIndex;
      protected int passiveLoopCount;

      public Animation(String id, String animation, float speed, float blendingDuration, boolean looping, float weight, int[] footstepIntervals, String soundEventId) {
         this.footstepIntervals = IntArrayCodec.EMPTY_INT_ARRAY;
         this.passiveLoopCount = 1;
         this.animation = animation;
         this.speed = speed;
         this.blendingDuration = blendingDuration;
         this.looping = looping;
         this.weight = weight;
         this.footstepIntervals = footstepIntervals;
         this.soundEventId = soundEventId;
      }

      protected Animation() {
         this.footstepIntervals = IntArrayCodec.EMPTY_INT_ARRAY;
         this.passiveLoopCount = 1;
      }

      public String getAnimation() {
         return this.animation;
      }

      public float getSpeed() {
         return this.speed;
      }

      public float getBlendingDuration() {
         return this.blendingDuration;
      }

      public boolean isLooping() {
         return this.looping;
      }

      public double getWeight() {
         return (double)this.weight;
      }

      public String getSoundEventId() {
         return this.soundEventId;
      }

      public int getSoundEventIndex() {
         return this.soundEventIndex;
      }

      @Nonnull
      public com.hypixel.hytale.protocol.Animation toPacket() {
         com.hypixel.hytale.protocol.Animation packet = new com.hypixel.hytale.protocol.Animation();
         packet.name = this.animation;
         packet.speed = this.speed;
         packet.blendingDuration = this.blendingDuration;
         packet.looping = this.looping;
         packet.weight = this.weight;
         packet.footstepIntervals = this.footstepIntervals;
         packet.soundEventIndex = this.soundEventIndex;
         packet.passiveLoopCount = this.passiveLoopCount;
         return packet;
      }

      protected void processConfig() {
         if (this.soundEventId != null) {
            this.soundEventIndex = SoundEvent.getAssetMap().getIndex(this.soundEventId);
         }

      }

      @Nonnull
      public String toString() {
         String var10000 = this.animation;
         return "Animation{animation='" + var10000 + "', speed=" + this.speed + ", blendingDuration=" + this.blendingDuration + ", looping=" + this.looping + ", weight=" + this.weight + ", footstepIntervals=" + Arrays.toString(this.footstepIntervals) + ", soundEventId='" + this.soundEventId + "', soundEventIndex=" + this.soundEventIndex + ", passiveLoopCount=" + this.passiveLoopCount + "}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Animation.class, Animation::new).append(new KeyedCodec("Animation", Codec.STRING), (animation, s) -> animation.animation = s, (animation) -> animation.animation).addValidator(Validators.nonNull()).addValidator(CommonAssetValidator.ANIMATION_CHARACTER).add()).append(new KeyedCodec("Speed", Codec.DOUBLE), (animation, s) -> animation.speed = s.floatValue(), (animation) -> (double)animation.speed).addValidator(Validators.greaterThan(0.0)).add()).append(new KeyedCodec("BlendingDuration", Codec.DOUBLE), (animation, s) -> animation.blendingDuration = s.floatValue(), (animation) -> (double)animation.blendingDuration).addValidator(Validators.min(0.0)).add()).addField(new KeyedCodec("Looping", Codec.BOOLEAN), (animation, s) -> animation.looping = s, (animation) -> animation.looping)).addField(new KeyedCodec("Weight", Codec.DOUBLE), (animation, aDouble) -> animation.weight = aDouble.floatValue(), (animation) -> (double)animation.weight)).append(new KeyedCodec("FootstepIntervals", Codec.INT_ARRAY), (animation, a) -> animation.footstepIntervals = a, (animation) -> animation.footstepIntervals).documentation("The intervals (in percentage of the animation duration) at which footsteps are supposed to occur. Only relevant for movement animations (used for timing footstep sound effects).").addValidator(new IntArrayValidator(Validators.range(0, 100))).add()).append(new KeyedCodec("SoundEventId", Codec.STRING), (animation, s) -> animation.soundEventId = s, (animation) -> animation.soundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.MONO).add()).append(new KeyedCodec("PassiveLoopCount", Codec.INTEGER), (animation, integer) -> animation.passiveLoopCount = integer, (animation) -> animation.passiveLoopCount).addValidator(Validators.greaterThan(0)).add()).afterDecode(Animation::processConfig)).build();
      }
   }
}
