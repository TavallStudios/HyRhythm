package com.hypixel.hytale.server.core.asset.type.ambiencefx.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIEditor;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AmbienceFXMusic implements NetworkSerializable<com.hypixel.hytale.protocol.AmbienceFXMusic> {
   public static final BuilderCodec<AmbienceFXMusic> CODEC;
   protected String[] tracks;
   protected float decibels = 0.0F;
   protected transient float volume = 1.0F;

   public AmbienceFXMusic(String[] tracks, float decibels) {
      this.tracks = tracks;
      this.decibels = decibels;
   }

   protected AmbienceFXMusic() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.AmbienceFXMusic toPacket() {
      com.hypixel.hytale.protocol.AmbienceFXMusic packet = new com.hypixel.hytale.protocol.AmbienceFXMusic();
      if (this.tracks != null && this.tracks.length > 0) {
         packet.tracks = this.tracks;
      }

      packet.volume = this.volume;
      return packet;
   }

   public String[] getTracks() {
      return this.tracks;
   }

   public float getDecibels() {
      return this.decibels;
   }

   public float getVolume() {
      return this.volume;
   }

   protected void processConfig() {
      this.volume = AudioUtil.decibelsToLinearGain(this.decibels);
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.tracks);
      return "AmbienceFXMusic{tracks=" + var10000 + ", decibels=" + this.decibels + ", volume=" + this.volume + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(AmbienceFXMusic.class, AmbienceFXMusic::new).appendInherited(new KeyedCodec("Tracks", Codec.STRING_ARRAY), (ambienceFXMusic, strings) -> ambienceFXMusic.tracks = strings, (ambienceFXMusic) -> ambienceFXMusic.tracks, (ambienceFXMusic, parent) -> ambienceFXMusic.tracks = parent.tracks).addValidator(Validators.nonEmptyArray()).addValidator(new ArrayValidator(CommonAssetValidator.MUSIC)).add()).appendInherited(new KeyedCodec("Volume", Codec.FLOAT), (ambienceFXMusic, f) -> ambienceFXMusic.decibels = f, (ambienceFXMusic) -> ambienceFXMusic.decibels, (ambienceFXMusic, parent) -> ambienceFXMusic.decibels = parent.decibels).metadata(new UIEditor(new UIEditor.FormattedNumber((Double)null, " dB", (Integer)null))).addValidator(Validators.range(-100.0F, 10.0F)).add()).afterDecode(AmbienceFXMusic::processConfig)).build();
   }
}
