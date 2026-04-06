package com.hypixel.hytale.server.core.universe.playerdata;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DefaultPlayerStorageProvider implements PlayerStorageProvider {
   public static final DefaultPlayerStorageProvider INSTANCE = new DefaultPlayerStorageProvider();
   public static final String ID = "Hytale";
   public static final BuilderCodec<DefaultPlayerStorageProvider> CODEC = BuilderCodec.builder(DefaultPlayerStorageProvider.class, () -> INSTANCE).build();
   public static final DiskPlayerStorageProvider DEFAULT = new DiskPlayerStorageProvider();

   @Nonnull
   public PlayerStorage getPlayerStorage() {
      return DEFAULT.getPlayerStorage();
   }

   @Nonnull
   public String toString() {
      return "DefaultPlayerStorageProvider{}";
   }
}
