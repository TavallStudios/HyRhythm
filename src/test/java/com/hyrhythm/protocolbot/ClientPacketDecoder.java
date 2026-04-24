package com.hyrhythm.protocolbot;

import com.github.luben.zstd.Zstd;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

final class ClientPacketDecoder extends ByteToMessageDecoder {
    private static final int MIN_FRAME_SIZE = 8;
    private static final int MAX_FRAME_SIZE = 1_677_721_600;

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < MIN_FRAME_SIZE) {
            return;
        }

        in.markReaderIndex();
        int frameLength = in.readIntLE();
        if (frameLength < 0 || frameLength > MAX_FRAME_SIZE) {
            throw new IllegalArgumentException("Invalid frame length: " + frameLength);
        }

        int packetId = in.readIntLE();
        PacketRegistry.PacketInfo info = PacketRegistry.getToClientPacketById(packetId);
        if (info == null) {
            throw new IllegalArgumentException("Unknown server packet id: " + packetId);
        }
        if (frameLength > info.maxSize()) {
            throw new IllegalArgumentException(
                "Server packet " + info.name() + " frame length " + frameLength + " exceeds " + info.maxSize()
            );
        }
        if (in.readableBytes() < frameLength) {
            in.resetReaderIndex();
            return;
        }

        ByteBuf framePayload = in.readRetainedSlice(frameLength);
        ByteBuf packetPayload = framePayload;
        try {
            if (info.compressed() && frameLength > 0) {
                packetPayload = decompress(framePayload, info.maxSize());
            }
            Packet packet = (Packet) info.deserialize().apply(packetPayload, 0);
            out.add(packet);
        } finally {
            if (packetPayload != framePayload) {
                packetPayload.release();
            }
            framePayload.release();
        }
    }

    private static ByteBuf decompress(ByteBuf compressedPayload, int maxSize) {
        byte[] compressed = ByteBufUtil.getBytes(compressedPayload);
        long contentSize = Zstd.getFrameContentSize(compressed);
        if (contentSize < 0) {
            throw new IllegalArgumentException("Compressed payload did not expose a valid zstd content size");
        }
        if (contentSize > maxSize) {
            throw new IllegalArgumentException(
                "Compressed payload expands to " + contentSize + " bytes which exceeds " + maxSize
            );
        }

        byte[] decompressed = Zstd.decompress(compressed, Math.toIntExact(contentSize));
        return Unpooled.wrappedBuffer(decompressed);
    }
}
