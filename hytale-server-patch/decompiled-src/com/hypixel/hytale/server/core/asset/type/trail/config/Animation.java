package com.hypixel.hytale.server.core.asset.type.trail.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.protocol.Range;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import javax.annotation.Nonnull;

public class Animation {
   public static final BuilderCodec<Animation> CODEC;
   private Vector2i frameSize;
   private Range frameRange;
   private int frameLifeSpan;

   public Vector2i getFrameSize() {
      return this.frameSize;
   }

   public Range getFrameRange() {
      return this.frameRange;
   }

   public int getFrameLifeSpan() {
      return this.frameLifeSpan;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.frameSize);
      return "Animation{frameSize=" + var10000 + ", frameRange=" + String.valueOf(this.frameRange) + ", frameLifeSpan=" + this.frameLifeSpan + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Animation.class, Animation::new).appendInherited(new KeyedCodec("FrameSize", Vector2i.CODEC), (animation, b) -> animation.frameSize = b, (animation) -> animation.frameSize, (animation, parent) -> animation.frameSize = parent.frameSize).add()).appendInherited(new KeyedCodec("FrameRange", ProtocolCodecs.RANGE), (animation, b) -> animation.frameRange = b, (animation) -> animation.frameRange, (animation, parent) -> animation.frameRange = parent.frameRange).add()).appendInherited(new KeyedCodec("FrameLifeSpan", Codec.INTEGER), (animation, i) -> animation.frameLifeSpan = i, (animation) -> animation.frameLifeSpan, (animation, parent) -> animation.frameLifeSpan = parent.frameLifeSpan).add()).build();
   }
}
