package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetUpdateRate implements Packet, ToClientPacket {
   public static final int PACKET_ID = 29;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int updatesPerSecond;

   public int getId() {
      return 29;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetUpdateRate() {
   }

   public SetUpdateRate(int updatesPerSecond) {
      this.updatesPerSecond = updatesPerSecond;
   }

   public SetUpdateRate(@Nonnull SetUpdateRate other) {
      this.updatesPerSecond = other.updatesPerSecond;
   }

   @Nonnull
   public static SetUpdateRate deserialize(@Nonnull ByteBuf buf, int offset) {
      SetUpdateRate obj = new SetUpdateRate();
      obj.updatesPerSecond = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.updatesPerSecond);
   }

   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public SetUpdateRate clone() {
      SetUpdateRate copy = new SetUpdateRate();
      copy.updatesPerSecond = this.updatesPerSecond;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof SetUpdateRate) {
         SetUpdateRate other = (SetUpdateRate)obj;
         return this.updatesPerSecond == other.updatesPerSecond;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.updatesPerSecond});
   }
}
