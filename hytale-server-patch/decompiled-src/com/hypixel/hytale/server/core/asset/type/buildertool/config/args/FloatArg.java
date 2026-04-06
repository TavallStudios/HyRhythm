package com.hypixel.hytale.server.core.asset.type.buildertool.config.args;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArg;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolArgType;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolFloatArg;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class FloatArg extends ToolArg<Float> {
   public static final BuilderCodec<FloatArg> CODEC;
   protected float min;
   protected float max;

   public FloatArg() {
      this.value = 0.0F;
   }

   public FloatArg(float value, float min, float max) {
      this.value = value;
      this.min = min;
      this.max = max;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   @Nonnull
   public Codec<Float> getCodec() {
      return Codec.FLOAT;
   }

   @Nonnull
   public Float fromString(@Nonnull String str) throws ToolArgException {
      float value = Float.parseFloat(str);
      if (!(value < this.min) && !(value > this.max)) {
         return value;
      } else {
         throw new ToolArgException(Message.translation("server.builderTools.toolArgRangeError").param("value", value).param("min", this.min).param("max", this.max));
      }
   }

   @Nonnull
   public BuilderToolFloatArg toFloatArgPacket() {
      return new BuilderToolFloatArg((Float)this.value, this.min, this.max);
   }

   protected void setupPacket(@Nonnull BuilderToolArg packet) {
      packet.argType = BuilderToolArgType.Float;
      packet.floatArg = this.toFloatArgPacket();
   }

   @Nonnull
   public String toString() {
      float var10000 = this.min;
      return "FloatArg{min=" + var10000 + ", max=" + this.max + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(FloatArg.class, FloatArg::new, ToolArg.DEFAULT_CODEC).addField(new KeyedCodec("Default", Codec.DOUBLE), (floatArg, o) -> floatArg.value = o.floatValue(), (floatArg) -> (double)(Float)floatArg.value)).addField(new KeyedCodec("Min", Codec.DOUBLE), (floatArg, o) -> floatArg.min = o.floatValue(), (floatArg) -> (double)floatArg.min)).addField(new KeyedCodec("Max", Codec.DOUBLE), (floatArg, o) -> floatArg.max = o.floatValue(), (floatArg) -> (double)floatArg.max)).build();
   }
}
