package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class RespawnConfig {
   @Nonnull
   public static BuilderCodec<RespawnConfig> CODEC;
   protected int radiusLimitRespawnPoint = 500;
   protected int maxRespawnPointsPerPlayer = 3;

   public int getRadiusLimitRespawnPoint() {
      return this.radiusLimitRespawnPoint;
   }

   public int getMaxRespawnPointsPerPlayer() {
      return this.maxRespawnPointsPerPlayer;
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RespawnConfig.class, RespawnConfig::new).append(new KeyedCodec("RadiusLimitRespawnPoint", Codec.INTEGER), (worldConfig, integer) -> worldConfig.radiusLimitRespawnPoint = integer, (worldConfig) -> worldConfig.radiusLimitRespawnPoint).addValidator(Validators.greaterThan(0)).add()).append(new KeyedCodec("MaxRespawnPointsPerPlayer", Codec.INTEGER), (worldConfig, integer) -> worldConfig.maxRespawnPointsPerPlayer = integer, (worldConfig) -> worldConfig.maxRespawnPointsPerPlayer).addValidator(Validators.greaterThan(0)).add()).build();
   }
}
