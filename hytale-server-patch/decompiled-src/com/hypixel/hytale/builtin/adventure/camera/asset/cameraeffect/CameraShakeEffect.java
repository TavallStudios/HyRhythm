package com.hypixel.hytale.builtin.adventure.camera.asset.cameraeffect;

import com.hypixel.hytale.builtin.adventure.camera.asset.camerashake.CameraShake;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.AccumulationMode;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraShakeEffect extends CameraEffect {
   @Nonnull
   public static final BuilderCodec<CameraShakeEffect> CODEC;
   @Nullable
   protected String cameraShakeId;
   protected int cameraShakeIndex = -2147483648;
   @Nullable
   protected ShakeIntensity intensity;

   @Nonnull
   public AccumulationMode getAccumulationMode() {
      return this.intensity == null ? ShakeIntensity.DEFAULT_ACCUMULATION_MODE : this.intensity.getAccumulationMode();
   }

   public float getDefaultIntensityContext() {
      return this.intensity == null ? 0.0F : this.intensity.getValue();
   }

   public float calculateIntensity(float intensityContext) {
      if (this.intensity == null) {
         return intensityContext;
      } else {
         ShakeIntensity.Modifier modifier = this.intensity.getModifier();
         return modifier == null ? intensityContext : modifier.apply(intensityContext);
      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect createCameraShakePacket() {
      float intensity = this.getDefaultIntensityContext();
      return this.createCameraShakePacket(intensity);
   }

   @Nonnull
   public com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect createCameraShakePacket(float intensityContext) {
      float intensity = this.calculateIntensity(intensityContext);
      AccumulationMode accumulationMode = this.getAccumulationMode();
      return new com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect(this.cameraShakeIndex, intensity, accumulationMode);
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "CameraShakeEffect{id='" + var10000 + "', data=" + String.valueOf(this.data) + ", cameraShakeId='" + this.cameraShakeId + "', cameraShakeIndex=" + this.cameraShakeIndex + ", intensity=" + String.valueOf(this.intensity) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(CameraShakeEffect.class, CameraShakeEffect::new).appendInherited(new KeyedCodec("CameraShake", CameraShake.CHILD_ASSET_CODEC), (cameraShakeEffect, s) -> cameraShakeEffect.cameraShakeId = s, (cameraShakeEffect) -> cameraShakeEffect.cameraShakeId, (cameraShakeEffect, parent) -> cameraShakeEffect.cameraShakeId = parent.cameraShakeId).documentation("The type of camera shake to apply for this effect.").addValidator(CameraShake.VALIDATOR_CACHE.getValidator()).add()).appendInherited(new KeyedCodec("Intensity", ShakeIntensity.CODEC), (cameraShakeEffect, s) -> cameraShakeEffect.intensity = s, (cameraShakeEffect) -> cameraShakeEffect.intensity, (cameraShakeEffect, parent) -> cameraShakeEffect.intensity = parent.intensity).documentation("Controls how intensity-context (such as damage) is interpreted as shake intensity.").add()).afterDecode((cameraShakeEffect) -> {
         if (cameraShakeEffect.cameraShakeId != null) {
            cameraShakeEffect.cameraShakeIndex = CameraShake.getAssetMap().getIndex(cameraShakeEffect.cameraShakeId);
         }

      })).build();
   }
}
