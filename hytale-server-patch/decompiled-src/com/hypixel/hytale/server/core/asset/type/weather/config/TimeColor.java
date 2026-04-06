package com.hypixel.hytale.server.core.asset.type.weather.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import javax.annotation.Nonnull;

public class TimeColor {
   public static final BuilderCodec<TimeColor> CODEC;
   public static final ArrayCodec<TimeColor> ARRAY_CODEC;
   protected float hour;
   protected Color color;

   public TimeColor(float hour, Color color) {
      this.hour = hour;
      this.color = color;
   }

   protected TimeColor() {
   }

   public float getHour() {
      return this.hour;
   }

   public Color getColor() {
      return this.color;
   }

   @Nonnull
   public String toString() {
      float var10000 = this.hour;
      return "TimeColor{hour=" + var10000 + ", color='" + String.valueOf(this.color) + "'}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(TimeColor.class, TimeColor::new).append(new KeyedCodec("Hour", Codec.DOUBLE), (timeColor, i) -> timeColor.hour = i.floatValue(), (timeColor) -> (double)timeColor.getHour()).addValidator(Validators.range(0.0, 24.0)).add()).addField(new KeyedCodec("Color", ProtocolCodecs.COLOR), (timeColor, o) -> timeColor.color = o, TimeColor::getColor)).build();
      ARRAY_CODEC = new ArrayCodec<TimeColor>(CODEC, (x$0) -> new TimeColor[x$0]);
   }
}
