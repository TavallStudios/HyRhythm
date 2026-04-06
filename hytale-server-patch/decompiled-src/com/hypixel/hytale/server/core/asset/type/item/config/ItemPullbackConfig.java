package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ItemPullbackConfiguration;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemPullbackConfig implements NetworkSerializable<ItemPullbackConfiguration> {
   public static final BuilderCodec<ItemPullbackConfig> CODEC;
   @Nullable
   protected Vector3f leftOffsetOverride;
   @Nullable
   protected Vector3f leftRotationOverride;
   @Nullable
   protected Vector3f rightOffsetOverride;
   @Nullable
   protected Vector3f rightRotationOverride;

   ItemPullbackConfig() {
   }

   public ItemPullbackConfig(Vector3f leftOffsetOverride, Vector3f leftRotationOverride, Vector3f rightOffsetOverride, Vector3f rightRotationOverride) {
      this.leftOffsetOverride = leftOffsetOverride;
      this.leftRotationOverride = leftRotationOverride;
      this.rightOffsetOverride = rightOffsetOverride;
      this.rightRotationOverride = rightRotationOverride;
   }

   @Nonnull
   public ItemPullbackConfiguration toPacket() {
      ItemPullbackConfiguration packet = new ItemPullbackConfiguration();
      packet.leftOffsetOverride = this.leftOffsetOverride;
      packet.leftRotationOverride = this.leftRotationOverride;
      packet.rightOffsetOverride = this.rightOffsetOverride;
      packet.rightRotationOverride = this.rightRotationOverride;
      return packet;
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ItemPullbackConfig.class, ItemPullbackConfig::new).append(new KeyedCodec("LeftOffsetOverride", Vector3d.AS_ARRAY_CODEC), (pullbackConfig, offOverride) -> pullbackConfig.leftOffsetOverride = offOverride == null ? null : new Vector3f((float)offOverride.getX(), (float)offOverride.getY(), (float)offOverride.getZ()), (pullbackConfig) -> pullbackConfig.leftOffsetOverride == null ? null : new Vector3d((double)pullbackConfig.leftOffsetOverride.x, (double)pullbackConfig.leftOffsetOverride.y, (double)pullbackConfig.leftOffsetOverride.z)).add()).append(new KeyedCodec("LeftRotationOverride", Vector3d.AS_ARRAY_CODEC), (pullbackConfig, rotOverride) -> pullbackConfig.leftRotationOverride = rotOverride == null ? null : new Vector3f((float)rotOverride.getX(), (float)rotOverride.getY(), (float)rotOverride.getZ()), (pullbackConfig) -> pullbackConfig.leftRotationOverride == null ? null : new Vector3d((double)pullbackConfig.leftRotationOverride.x, (double)pullbackConfig.leftRotationOverride.y, (double)pullbackConfig.leftRotationOverride.z)).add()).append(new KeyedCodec("RightOffsetOverride", Vector3d.AS_ARRAY_CODEC), (pullbackConfig, offOverride) -> pullbackConfig.rightOffsetOverride = offOverride == null ? null : new Vector3f((float)offOverride.getX(), (float)offOverride.getY(), (float)offOverride.getZ()), (pullbackConfig) -> pullbackConfig.rightOffsetOverride == null ? null : new Vector3d((double)pullbackConfig.rightOffsetOverride.x, (double)pullbackConfig.rightOffsetOverride.y, (double)pullbackConfig.rightOffsetOverride.z)).add()).append(new KeyedCodec("RightRotationOverride", Vector3d.AS_ARRAY_CODEC), (pullbackConfig, rotOverride) -> pullbackConfig.rightRotationOverride = rotOverride == null ? null : new Vector3f((float)rotOverride.getX(), (float)rotOverride.getY(), (float)rotOverride.getZ()), (pullbackConfig) -> pullbackConfig.rightRotationOverride == null ? null : new Vector3d((double)pullbackConfig.rightRotationOverride.x, (double)pullbackConfig.rightRotationOverride.y, (double)pullbackConfig.rightRotationOverride.z)).add()).build();
   }
}
