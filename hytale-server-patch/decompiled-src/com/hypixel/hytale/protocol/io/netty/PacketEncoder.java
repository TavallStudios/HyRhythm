package com.hypixel.hytale.protocol.io.netty;

import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.annotation.Nonnull;

@Sharable
public class PacketEncoder extends MessageToByteEncoder<Packet> {
   protected void encode(@Nonnull ChannelHandlerContext ctx, @Nonnull Packet packet, @Nonnull ByteBuf out) {
      Class<? extends Packet> packetClass;
      if (packet instanceof CachedPacket<?> cached) {
         packetClass = cached.getPacketType();
      } else {
         packetClass = packet.getClass();
      }

      NetworkChannel channelAttr = (NetworkChannel)ctx.channel().attr(ProtocolUtil.STREAM_CHANNEL_KEY).get();
      if (channelAttr != null && channelAttr != packet.getChannel()) {
         String var10002 = String.valueOf(packet.getChannel());
         throw new IllegalArgumentException("Packet channel " + var10002 + " does not match stream channel " + String.valueOf(channelAttr));
      } else {
         PacketStatsRecorder statsRecorder = (PacketStatsRecorder)ctx.channel().attr(PacketStatsRecorder.CHANNEL_KEY).get();
         if (statsRecorder == null) {
            statsRecorder = PacketStatsRecorder.NOOP;
         }

         PacketIO.writeFramedPacket(packet, packetClass, out, statsRecorder);
      }
   }
}
