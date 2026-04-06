package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.protocol.AmbienceTransitionSpeed;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.common.SoundFileValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class AmbienceFXAmbientBed implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXAmbientBed> {
   public static final BuilderCodec<AmbienceFXAmbientBed> CODEC;
   protected String track;
   protected float decibels = 0.0F;
   protected transient float volume = 1.0F;
   protected AmbienceTransitionSpeed transitionSpeed;

   public AmbienceFXAmbientBed(String track, float decibels, AmbienceTransitionSpeed transitionSpeed) {
      this.transitionSpeed = AmbienceTransitionSpeed.Default;
      this.track = track;
      this.decibels = decibels;
      this.transitionSpeed = transitionSpeed;
   }

   protected AmbienceFXAmbientBed() {
      this.transitionSpeed = AmbienceTransitionSpeed.Default;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXAmbientBed toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXAmbientBed packet = new com.hypixel.hytale.protocol.AmbienceFXAmbientBed();
      packet.track = this.track;
      packet.volume = this.volume;
      packet.transitionSpeed = this.transitionSpeed;
      return packet;
   }

   public String getTrack() {
      return this.track;
   }

   public float getDecibels() {
      return this.decibels;
   }

   public float getVolume() {
      return this.volume;
   }

   public AmbienceTransitionSpeed getTransitionSpeed() {
      return this.transitionSpeed;
   }

   protected void processConfig() {
      this.volume = AudioUtil.decibelsToLinearGain(this.decibels);
   }

   @Nonnull
   public String toString() {
      String var10000 = this.track;
      return "AmbienceFXAmbientBed{track='" + var10000 + "', decibels=" + this.decibels + ", volume=" + this.volume + ", transitionSpeed=" + String.valueOf(this.transitionSpeed) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AmbienceFXAmbientBed.class, AmbienceFXAmbientBed::new).appendInherited(new KeyedCodec("Track", Codec.STRING), (ambienceFXAmbientBed, s) -> ambienceFXAmbientBed.track = s, (ambienceFXAmbientBed) -> ambienceFXAmbientBed.track, (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.track = parent.track).addValidator(Validators.nonNull()).addValidator(CommonAssetValidator.SOUNDS).addValidator(SoundFileValidators.STEREO).add()).appendInherited(new KeyedCodec("Volume", Codec.FLOAT), (ambienceFXAmbientBed, f) -> ambienceFXAmbientBed.decibels = f, (ambienceFXAmbientBed) -> ambienceFXAmbientBed.decibels, (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.decibels = parent.decibels).metadata(new UIEditor(new UIEditor.FormattedNumber((Double)null, " dB", (Integer)null))).addValidator(Validators.range(-100.0F, 10.0F)).add()).appendInherited(new KeyedCodec("TransitionSpeed", new EnumCodec(AmbienceTransitionSpeed.class)), (ambienceFXAmbientBed, e) -> ambienceFXAmbientBed.transitionSpeed = e, (ambienceFXAmbientBed) -> ambienceFXAmbientBed.transitionSpeed, (ambienceFXAmbientBed, parent) -> ambienceFXAmbientBed.transitionSpeed = parent.transitionSpeed).documentation("How quickly to transition to this ambient bed and fade out any ambient beds that are stopping. For fading out stopping ambient beds, faster transitions take priority. Fade-ins are already fast by default.").add()).afterDecode(AmbienceFXAmbientBed::processConfig)).build();
   }
}
