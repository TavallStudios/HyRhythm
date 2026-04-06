package com.hypixel.hytale.server.core.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.HytaleType;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDisplayMode;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.AccumulationMode;
import com.hypixel.hytale.protocol.ChangeStatBehaviour;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InitialVelocity;
import com.hypixel.hytale.protocol.IntersectionHighlight;
import com.hypixel.hytale.protocol.ItemAnimation;
import com.hypixel.hytale.protocol.RailConfig;
import com.hypixel.hytale.protocol.RailPoint;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.protocol.RangeVector2f;
import com.hypixel.hytale.protocol.RangeVector3f;
import com.hypixel.hytale.protocol.Rangeb;
import com.hypixel.hytale.protocol.Rangef;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.Size;
import com.hypixel.hytale.protocol.UVMotion;
import com.hypixel.hytale.protocol.UVMotionCurveType;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.common.BlockyAnimationCache;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.util.ColorParseUtil;
import com.hypixel.hytale.server.core.codec.protocol.ColorAlphaCodec;
import com.hypixel.hytale.server.core.codec.protocol.ColorCodec;

public final class ProtocolCodecs {
   public static final BuilderCodec<Direction> DIRECTION;
   public static final BuilderCodec<Vector2f> VECTOR2F;
   public static final BuilderCodec<Vector3f> VECTOR3F;
   public static final BuilderCodec<ColorLight> COLOR_LIGHT;
   public static final ColorCodec COLOR;
   public static final ArrayCodec<Color> COLOR_ARRAY;
   public static final ColorAlphaCodec COLOR_AlPHA;
   public static final EnumCodec<GameMode> GAMEMODE;
   public static final EnumCodec<GameMode> GAMEMODE_LEGACY;
   public static final BuilderCodec<Size> SIZE;
   public static final BuilderCodec<Range> RANGE;
   public static final BuilderCodec<Rangeb> RANGEB;
   public static final BuilderCodec<Rangef> RANGEF;
   public static final BuilderCodec<RangeVector2f> RANGE_VECTOR2F;
   public static final BuilderCodec<RangeVector3f> RANGE_VECTOR3F;
   public static final BuilderCodec<InitialVelocity> INITIAL_VELOCITY;
   public static final BuilderCodec<UVMotion> UV_MOTION;
   public static final BuilderCodec<IntersectionHighlight> INTERSECTION_HIGHLIGHT;
   public static final BuilderCodec<SavedMovementStates> SAVED_MOVEMENT_STATES;
   public static final BuilderCodec<ItemAnimation> ITEM_ANIMATION_CODEC;
   public static final EnumCodec<ChangeStatBehaviour> CHANGE_STAT_BEHAVIOUR_CODEC;
   public static final EnumCodec<AccumulationMode> ACCUMULATION_MODE_CODEC;
   public static final EnumCodec<EasingType> EASING_TYPE_CODEC;
   public static final EnumCodec<ChangeVelocityType> CHANGE_VELOCITY_TYPE_CODEC;
   public static final BuilderCodec<RailPoint> RAIL_POINT_CODEC;
   public static final BuilderCodec<RailConfig> RAIL_CONFIG_CODEC;

   static {
      DIRECTION = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Direction.class, Direction::new).metadata(UIDisplayMode.COMPACT)).appendInherited(new KeyedCodec("Yaw", Codec.FLOAT), (o, i) -> o.yaw = i, (o) -> o.yaw, (o, p) -> o.yaw = p.yaw).add()).appendInherited(new KeyedCodec("Pitch", Codec.FLOAT), (o, i) -> o.pitch = i, (o) -> o.pitch, (o, p) -> o.pitch = p.pitch).add()).appendInherited(new KeyedCodec("Roll", Codec.FLOAT), (o, i) -> o.roll = i, (o) -> o.roll, (o, p) -> o.roll = p.roll).add()).build();
      VECTOR2F = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Vector2f.class, Vector2f::new).metadata(UIDisplayMode.COMPACT)).appendInherited(new KeyedCodec("X", Codec.FLOAT), (o, i) -> o.x = i, (o) -> o.x, (o, p) -> o.x = p.x).add()).appendInherited(new KeyedCodec("Y", Codec.FLOAT), (o, i) -> o.y = i, (o) -> o.y, (o, p) -> o.y = p.y).add()).build();
      VECTOR3F = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Vector3f.class, Vector3f::new).metadata(UIDisplayMode.COMPACT)).appendInherited(new KeyedCodec("X", Codec.FLOAT), (o, i) -> o.x = i, (o) -> o.x, (o, p) -> o.x = p.x).add()).appendInherited(new KeyedCodec("Y", Codec.FLOAT), (o, i) -> o.y = i, (o) -> o.y, (o, p) -> o.y = p.y).add()).appendInherited(new KeyedCodec("Z", Codec.FLOAT), (o, i) -> o.z = i, (o) -> o.z, (o, p) -> o.z = p.z).add()).build();
      COLOR_LIGHT = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ColorLight.class, ColorLight::new).appendInherited(new KeyedCodec("Color", Codec.STRING), ColorParseUtil::hexStringToColorLightDirect, ColorParseUtil::colorLightToHexString, (o, p) -> {
         o.red = p.red;
         o.green = p.green;
         o.blue = p.blue;
      }).metadata(new HytaleType("ColorShort")).add()).appendInherited(new KeyedCodec("Radius", Codec.BYTE), (o, i) -> o.radius = i, (o) -> o.radius, (o, p) -> o.radius = p.radius).add()).build();
      COLOR = new ColorCodec();
      COLOR_ARRAY = new ArrayCodec<Color>(COLOR, (x$0) -> new Color[x$0]);
      COLOR_AlPHA = new ColorAlphaCodec();
      GAMEMODE = (new EnumCodec<GameMode>(GameMode.class)).documentKey(GameMode.Creative, "Makes the player invulnerable and grants them the ability to fly.").documentKey(GameMode.Adventure, "The normal gamemode for players playing the game.");
      GAMEMODE_LEGACY = new EnumCodec<GameMode>(GameMode.class, EnumCodec.EnumStyle.LEGACY);
      SIZE = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Size.class, Size::new).metadata(UIDisplayMode.COMPACT)).addField(new KeyedCodec("Width", Codec.INTEGER), (size, i) -> size.width = i, (size) -> size.width)).addField(new KeyedCodec("Height", Codec.INTEGER), (size, i) -> size.height = i, (size) -> size.height)).build();
      RANGE = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Range.class, Range::new).metadata(UIDisplayMode.COMPACT)).addField(new KeyedCodec("Min", Codec.INTEGER), (rangeb, i) -> rangeb.min = i, (rangeb) -> rangeb.min)).addField(new KeyedCodec("Max", Codec.INTEGER), (rangeb, i) -> rangeb.max = i, (rangeb) -> rangeb.max)).build();
      RANGEB = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Rangeb.class, Rangeb::new).metadata(UIDisplayMode.COMPACT)).addField(new KeyedCodec("Min", Codec.BYTE), (rangeb, i) -> rangeb.min = i, (rangeb) -> rangeb.min)).addField(new KeyedCodec("Max", Codec.BYTE), (rangeb, i) -> rangeb.max = i, (rangeb) -> rangeb.max)).build();
      RANGEF = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Rangef.class, Rangef::new).metadata(UIDisplayMode.COMPACT)).addField(new KeyedCodec("Min", Codec.DOUBLE), (rangef, d) -> rangef.min = d.floatValue(), (rangeb) -> (double)rangeb.min)).addField(new KeyedCodec("Max", Codec.DOUBLE), (rangef, d) -> rangef.max = d.floatValue(), (rangeb) -> (double)rangeb.max)).build();
      RANGE_VECTOR2F = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RangeVector2f.class, RangeVector2f::new).addField(new KeyedCodec("X", RANGEF), (rangeVector2f, d) -> rangeVector2f.x = d, (rangeVector2f) -> rangeVector2f.x)).addField(new KeyedCodec("Y", RANGEF), (rangeVector2f, d) -> rangeVector2f.y = d, (rangeVector2f) -> rangeVector2f.y)).build();
      RANGE_VECTOR3F = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RangeVector3f.class, RangeVector3f::new).addField(new KeyedCodec("X", RANGEF), (rangeVector3f, d) -> rangeVector3f.x = d, (rangeVector3f) -> rangeVector3f.x)).addField(new KeyedCodec("Y", RANGEF), (rangeVector3f, d) -> rangeVector3f.y = d, (rangeVector3f) -> rangeVector3f.y)).addField(new KeyedCodec("Z", RANGEF), (rangeVector3f, d) -> rangeVector3f.z = d, (rangeVector3f) -> rangeVector3f.z)).build();
      INITIAL_VELOCITY = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(InitialVelocity.class, InitialVelocity::new).addField(new KeyedCodec("Yaw", RANGEF), (rangeVector3f, d) -> rangeVector3f.yaw = d, (rangeVector3f) -> rangeVector3f.yaw)).addField(new KeyedCodec("Pitch", RANGEF), (rangeVector3f, d) -> rangeVector3f.pitch = d, (rangeVector3f) -> rangeVector3f.pitch)).addField(new KeyedCodec("Speed", RANGEF), (rangeVector3f, d) -> rangeVector3f.speed = d, (rangeVector3f) -> rangeVector3f.speed)).build();
      UV_MOTION = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(UVMotion.class, UVMotion::new).append(new KeyedCodec("Texture", Codec.STRING), (uvMotion, s) -> uvMotion.texture = s, (uvMotion) -> uvMotion.texture).addValidator(CommonAssetValidator.TEXTURE_PARTICLES).add()).append(new KeyedCodec("AddRandomUVOffset", Codec.BOOLEAN), (uvMotion, b) -> uvMotion.addRandomUVOffset = b, (uvMotion) -> uvMotion.addRandomUVOffset).add()).append(new KeyedCodec("SpeedX", Codec.DOUBLE), (uvMotion, s) -> uvMotion.speedX = s.floatValue(), (uvMotion) -> (double)uvMotion.speedX).addValidator(Validators.range(-10.0, 10.0)).add()).append(new KeyedCodec("SpeedY", Codec.DOUBLE), (uvMotion, s) -> uvMotion.speedY = s.floatValue(), (uvMotion) -> (double)uvMotion.speedY).addValidator(Validators.range(-10.0, 10.0)).add()).append(new KeyedCodec("Strength", Codec.DOUBLE), (uvMotion, s) -> uvMotion.strength = s.floatValue(), (uvMotion) -> (double)uvMotion.strength).addValidator(Validators.range(0.0, 50.0)).add()).append(new KeyedCodec("StrengthCurveType", new EnumCodec(UVMotionCurveType.class)), (uvMotion, s) -> uvMotion.strengthCurveType = s, (uvMotion) -> uvMotion.strengthCurveType).add()).append(new KeyedCodec("Scale", Codec.DOUBLE), (uvMotion, s) -> uvMotion.scale = s.floatValue(), (uvMotion) -> (double)uvMotion.scale).addValidator(Validators.range(0.0, 10.0)).add()).build();
      INTERSECTION_HIGHLIGHT = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(IntersectionHighlight.class, IntersectionHighlight::new).append(new KeyedCodec("HighlightThreshold", Codec.FLOAT), (intersectionHighlight, s) -> intersectionHighlight.highlightThreshold = s, (intersectionHighlight) -> intersectionHighlight.highlightThreshold).addValidator(Validators.range(0.0F, 1.0F)).add()).addField(new KeyedCodec("HighlightColor", COLOR), (intersectionHighlight, s) -> intersectionHighlight.highlightColor = s, (intersectionHighlight) -> intersectionHighlight.highlightColor)).build();
      SAVED_MOVEMENT_STATES = ((BuilderCodec.Builder)BuilderCodec.builder(SavedMovementStates.class, SavedMovementStates::new).addField(new KeyedCodec("Flying", Codec.BOOLEAN), (movementStates, flying) -> movementStates.flying = flying, (movementStates) -> movementStates.flying)).build();
      ITEM_ANIMATION_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ItemAnimation.class, ItemAnimation::new).append(new KeyedCodec("ThirdPerson", Codec.STRING), (itemAnimation, s) -> itemAnimation.thirdPerson = s, (itemAnimation) -> itemAnimation.thirdPerson).addValidator(CommonAssetValidator.ANIMATION_ITEM_CHARACTER).add()).append(new KeyedCodec("ThirdPersonMoving", Codec.STRING), (itemAnimation, s) -> itemAnimation.thirdPersonMoving = s, (itemAnimation) -> itemAnimation.thirdPersonMoving).addValidator(CommonAssetValidator.ANIMATION_ITEM_CHARACTER).add()).append(new KeyedCodec("ThirdPersonFace", Codec.STRING), (itemAnimation, s) -> itemAnimation.thirdPersonFace = s, (itemAnimation) -> itemAnimation.thirdPersonFace).addValidator(CommonAssetValidator.ANIMATION_ITEM_CHARACTER).add()).append(new KeyedCodec("FirstPerson", Codec.STRING), (itemAnimation, s) -> itemAnimation.firstPerson = s, (itemAnimation) -> itemAnimation.firstPerson).addValidator(CommonAssetValidator.ANIMATION_ITEM_CHARACTER).add()).append(new KeyedCodec("FirstPersonOverride", Codec.STRING), (itemAnimation, s) -> itemAnimation.firstPersonOverride = s, (itemAnimation) -> itemAnimation.firstPersonOverride).addValidator(CommonAssetValidator.ANIMATION_ITEM_CHARACTER).add()).addField(new KeyedCodec("KeepPreviousFirstPersonAnimation", Codec.BOOLEAN), (itemAnimation, s) -> itemAnimation.keepPreviousFirstPersonAnimation = s, (itemAnimation) -> itemAnimation.keepPreviousFirstPersonAnimation)).addField(new KeyedCodec("Speed", Codec.DOUBLE), (itemAnimation, s) -> itemAnimation.speed = s.floatValue(), (itemAnimation) -> (double)itemAnimation.speed)).addField(new KeyedCodec("BlendingDuration", Codec.DOUBLE), (itemAnimation, s) -> itemAnimation.blendingDuration = s.floatValue(), (itemAnimation) -> (double)itemAnimation.blendingDuration)).addField(new KeyedCodec("Looping", Codec.BOOLEAN), (itemAnimation, s) -> itemAnimation.looping = s, (itemAnimation) -> itemAnimation.looping)).addField(new KeyedCodec("ClipsGeometry", Codec.BOOLEAN), (itemAnimation, s) -> itemAnimation.clipsGeometry = s, (itemAnimation) -> itemAnimation.clipsGeometry)).afterDecode((itemAnimation) -> {
         if (itemAnimation.firstPerson != null) {
            BlockyAnimationCache.get(itemAnimation.firstPerson);
         }

         if (itemAnimation.firstPersonOverride != null) {
            BlockyAnimationCache.get(itemAnimation.firstPersonOverride);
         }

      })).build();
      CHANGE_STAT_BEHAVIOUR_CODEC = (new EnumCodec<ChangeStatBehaviour>(ChangeStatBehaviour.class)).documentKey(ChangeStatBehaviour.Add, "Adds the value to the stat").documentKey(ChangeStatBehaviour.Set, "Changes the stat to the given value");
      ACCUMULATION_MODE_CODEC = (new EnumCodec<AccumulationMode>(AccumulationMode.class)).documentKey(AccumulationMode.Set, "Set the current value to the new one").documentKey(AccumulationMode.Sum, "Add the new value to the current one").documentKey(AccumulationMode.Average, "Average the new value with current one");
      EASING_TYPE_CODEC = new EnumCodec<EasingType>(EasingType.class);
      CHANGE_VELOCITY_TYPE_CODEC = (new EnumCodec<ChangeVelocityType>(ChangeVelocityType.class)).documentKey(ChangeVelocityType.Add, "Adds the velocity to any existing velocity").documentKey(ChangeVelocityType.Set, "Changes the velocity to the given value. Overriding existing values.");
      RAIL_POINT_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RailPoint.class, RailPoint::new).appendInherited(new KeyedCodec("Point", VECTOR3F), (o, v) -> o.point = v, (o) -> o.point, (o, p) -> o.point = p.point).addValidator(Validators.nonNull()).add()).appendInherited(new KeyedCodec("Normal", VECTOR3F), (o, v) -> o.normal = v, (o) -> o.normal, (o, p) -> o.normal = p.normal).addValidator(Validators.nonNull()).add()).afterDecode((o) -> {
         if (o.normal != null) {
            com.hypixel.hytale.math.vector.Vector3f v = new com.hypixel.hytale.math.vector.Vector3f(o.normal.x, o.normal.y, o.normal.z);
            v = v.normalize();
            o.normal.x = v.x;
            o.normal.y = v.y;
            o.normal.z = v.z;
         }

      })).build();
      RAIL_CONFIG_CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(RailConfig.class, RailConfig::new).appendInherited(new KeyedCodec("Points", new ArrayCodec(RAIL_POINT_CODEC, (x$0) -> new RailPoint[x$0])), (o, v) -> o.points = v, (o) -> o.points, (o, p) -> o.points = p.points).addValidator(Validators.nonNull()).addValidator(Validators.arraySizeRange(2, 16)).add()).build();
   }
}
