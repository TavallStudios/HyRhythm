package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolSetNPCDebug implements Packet, ToServerPacket {
   public static final int PACKET_ID = 423;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public int entityId;
   public boolean enabled;

   public int getId() {
      return 423;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSetNPCDebug() {
   }

   public BuilderToolSetNPCDebug(int entityId, boolean enabled) {
      this.entityId = entityId;
      this.enabled = enabled;
   }

   public BuilderToolSetNPCDebug(@Nonnull BuilderToolSetNPCDebug other) {
      this.entityId = other.entityId;
      this.enabled = other.enabled;
   }

   @Nonnull
   public static BuilderToolSetNPCDebug deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolSetNPCDebug obj = new BuilderToolSetNPCDebug();
      obj.entityId = buf.getIntLE(offset + 0);
      obj.enabled = buf.getByte(offset + 4) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.entityId);
      buf.writeByte(this.enabled ? 1 : 0);
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public BuilderToolSetNPCDebug clone() {
      BuilderToolSetNPCDebug copy = new BuilderToolSetNPCDebug();
      copy.entityId = this.entityId;
      copy.enabled = this.enabled;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof BuilderToolSetNPCDebug)) {
         return false;
      } else {
         BuilderToolSetNPCDebug other = (BuilderToolSetNPCDebug)obj;
         return this.entityId == other.entityId && this.enabled == other.enabled;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.entityId, this.enabled});
   }
}
