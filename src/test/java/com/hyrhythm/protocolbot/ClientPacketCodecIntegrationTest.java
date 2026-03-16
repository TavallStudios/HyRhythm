package com.hyrhythm.protocolbot;

import com.hypixel.hytale.protocol.Asset;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.io.netty.PacketDecoder;
import com.hypixel.hytale.protocol.io.netty.PacketEncoder;
import com.hypixel.hytale.protocol.packets.setup.WorldSettings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientPacketCodecIntegrationTest {
    @Test
    void clientEncoderProducesFramesTheServerDecoderAccepts() {
        Connect connect = new Connect(
            -1356075132,
            20,
            "hyrhythm-bot",
            ClientType.Game,
            UUID.nameUUIDFromBytes("hyrhythm:test".getBytes(StandardCharsets.UTF_8)),
            "CodecBot",
            null,
            "en",
            null,
            null
        );

        EmbeddedChannel clientChannel = new EmbeddedChannel(new ClientPacketEncoder());
        assertTrue(clientChannel.writeOutbound(connect));
        ByteBuf encoded = clientChannel.readOutbound();

        EmbeddedChannel serverChannel = new EmbeddedChannel(new PacketDecoder());
        assertTrue(serverChannel.writeInbound(encoded.retain()));
        Packet decoded = serverChannel.readInbound();

        Connect decodedConnect = assertInstanceOf(Connect.class, decoded);
        assertEquals(connect.protocolCrc, decodedConnect.protocolCrc);
        assertEquals(connect.protocolBuildNumber, decodedConnect.protocolBuildNumber);
        assertEquals(connect.clientVersion, decodedConnect.clientVersion);
        assertEquals(connect.username, decodedConnect.username);
        assertEquals(connect.language, decodedConnect.language);

        encoded.release();
        clientChannel.finishAndReleaseAll();
        serverChannel.finishAndReleaseAll();
    }

    @Test
    void clientEncoderHandlesCompressedClientPackets() {
        var requestAssets = new com.hypixel.hytale.protocol.packets.setup.RequestAssets(new Asset[0]);

        EmbeddedChannel clientChannel = new EmbeddedChannel(new ClientPacketEncoder());
        assertTrue(clientChannel.writeOutbound(requestAssets));
        ByteBuf encoded = clientChannel.readOutbound();

        EmbeddedChannel serverChannel = new EmbeddedChannel(new PacketDecoder());
        assertTrue(serverChannel.writeInbound(encoded.retain()));
        Packet decoded = serverChannel.readInbound();

        var decodedRequest = assertInstanceOf(com.hypixel.hytale.protocol.packets.setup.RequestAssets.class, decoded);
        assertArrayEquals(requestAssets.assets, decodedRequest.assets);

        encoded.release();
        clientChannel.finishAndReleaseAll();
        serverChannel.finishAndReleaseAll();
    }

    @Test
    void clientDecoderReadsServerFramedCompressedAndUncompressedPackets() {
        WorldSettings worldSettings = new WorldSettings(256, new Asset[0]);
        SetClientId setClientId = new SetClientId(42);
        CustomPage customPage = new CustomPage(
            "com.hyrhythm.ui.RhythmSongSelectionPage",
            true,
            false,
            CustomPageLifetime.CanDismiss,
            null,
            null
        );

        EmbeddedChannel serverChannel = new EmbeddedChannel(new PacketEncoder());
        assertTrue(serverChannel.writeOutbound(worldSettings));
        ByteBuf encodedWorldSettings = serverChannel.readOutbound();
        assertTrue(serverChannel.writeOutbound(setClientId));
        ByteBuf encodedSetClientId = serverChannel.readOutbound();
        assertTrue(serverChannel.writeOutbound(customPage));
        ByteBuf encodedCustomPage = serverChannel.readOutbound();

        EmbeddedChannel clientChannel = new EmbeddedChannel(new ClientPacketDecoder());
        assertTrue(clientChannel.writeInbound(encodedWorldSettings.retain()));
        assertTrue(clientChannel.writeInbound(encodedSetClientId.retain()));
        assertTrue(clientChannel.writeInbound(encodedCustomPage.retain()));

        WorldSettings decodedWorldSettings = assertInstanceOf(WorldSettings.class, clientChannel.readInbound());
        SetClientId decodedSetClientId = assertInstanceOf(SetClientId.class, clientChannel.readInbound());
        CustomPage decodedCustomPage = assertInstanceOf(CustomPage.class, clientChannel.readInbound());

        assertEquals(worldSettings.worldHeight, decodedWorldSettings.worldHeight);
        assertArrayEquals(worldSettings.requiredAssets, decodedWorldSettings.requiredAssets);
        assertEquals(setClientId.clientId, decodedSetClientId.clientId);
        assertEquals(customPage.key, decodedCustomPage.key);
        assertEquals(customPage.isInitial, decodedCustomPage.isInitial);
        assertEquals(customPage.lifetime, decodedCustomPage.lifetime);

        encodedWorldSettings.release();
        encodedSetClientId.release();
        encodedCustomPage.release();
        serverChannel.finishAndReleaseAll();
        clientChannel.finishAndReleaseAll();
    }
}
