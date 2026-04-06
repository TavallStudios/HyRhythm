package com.hypixel.hytale.server.core.universe.playerdata;

import com.hypixel.hytale.codec.lookup.BuilderCodecMapCodec;

public interface PlayerStorageProvider {
   BuilderCodecMapCodec<PlayerStorageProvider> CODEC = new BuilderCodecMapCodec<PlayerStorageProvider>("Type", true);

   PlayerStorage getPlayerStorage();
}
