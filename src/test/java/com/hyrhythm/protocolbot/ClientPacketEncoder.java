package com.hyrhythm.protocolbot;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

final class ClientPacketEncoder extends MessageToByteEncoder<Packet> {
    private static final int MAX_FRAME_SIZE = 1_677_721_600;
    private static final int COMPRESSION_LEVEL = Integer.getInteger(
        "hytale.protocol.compressionLevel",
        Zstd.defaultCompressionLevel()
    );

    @Override
    protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf out) {
        Integer packetId = PacketRegistry.getId(packet.getClass());
        if (packetId == null) {
            throw new IllegalArgumentException("Unregistered packet type: " + packet.getClass().getName());
        }

        PacketRegistry.PacketInfo info = PacketRegistry.getToServerPacketById(packetId);
        if (info == null) {
            throw new IllegalArgumentException("Packet is not valid for client->server transport: " + packet.getClass().getName());
        }

        ByteBuf payload = Unpooled.buffer(Math.min(Math.max(info.fixedBlockSize(), 1), 65_536));
        try {
            packet.serialize(payload);
            int payloadLength = payload.readableBytes();
            if (payloadLength > info.maxSize()) {
                throw new IllegalArgumentException(
                    "Packet " + info.name() + " serialized to " + payloadLength + " bytes which exceeds " + info.maxSize()
                );
            }

            if (info.compressed() && payloadLength > 0) {
                byte[] compressed = Zstd.compress(ByteBufUtil.getBytes(payload), COMPRESSION_LEVEL);
                if (compressed.length > MAX_FRAME_SIZE) {
                    throw new IllegalArgumentException(
                        "Compressed packet " + info.name() + " exceeded frame limit with " + compressed.length + " bytes"
                    );
                }
                out.writeIntLE(compressed.length);
                out.writeIntLE(packetId);
                out.writeBytes(compressed);
                return;
            }

            if (payloadLength > MAX_FRAME_SIZE) {
                throw new IllegalArgumentException(
                    "Packet " + info.name() + " exceeded frame limit with " + payloadLength + " bytes"
                );
            }

            out.writeIntLE(payloadLength);
            out.writeIntLE(packetId);
            out.writeBytes(payload, payload.readerIndex(), payloadLength);
        } finally {
            payload.release();
        }
    }
}
