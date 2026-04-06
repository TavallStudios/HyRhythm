package com.hypixel.hytale.server.core.asset.type.trail.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ColorAlpha;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import javax.annotation.Nonnull;

public class Edge {
   public static final BuilderCodec<Edge> CODEC;
   private float width;
   private ColorAlpha color = new ColorAlpha((byte)-1, (byte)-1, (byte)-1, (byte)-1);

   @Nonnull
   public com.hypixel.hytale.protocol.Edge toPacket() {
      com.hypixel.hytale.protocol.Edge packet = new com.hypixel.hytale.protocol.Edge();
      packet.color = this.color;
      packet.width = this.width;
      return packet;
   }

   public float getWidth() {
      return this.width;
   }

   public ColorAlpha getColor() {
      return this.color;
   }

   @Nonnull
   public String toString() {
      float var10000 = this.width;
      return "Edge{width=" + var10000 + ", color='" + String.valueOf(this.color) + "'}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(Edge.class, Edge::new).appendInherited(new KeyedCodec("Width", Codec.DOUBLE), (edge, d) -> edge.width = d.floatValue(), (edge) -> (double)edge.width, (edge, parent) -> edge.width = parent.width).add()).appendInherited(new KeyedCodec("Color", ProtocolCodecs.COLOR_AlPHA), (edge, o) -> edge.color = o, (edge) -> edge.color, (edge, parent) -> edge.color = parent.color).add()).build();
   }
}
