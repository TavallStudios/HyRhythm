package com.hypixel.hytale.protocol.packets.setup;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ViewRadius implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 32;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int value;

   public int getId() {
      return 32;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ViewRadius() {
   }

   public ViewRadius(int value) {
      this.value = value;
   }

   public ViewRadius(@Nonnull ViewRadius other) {
      this.value = other.value;
   }

   @Nonnull
   public static ViewRadius deserialize(@Nonnull ByteBuf buf, int offset) {
      ViewRadius obj = new ViewRadius();
      obj.value = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.value);
   }

   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public ViewRadius clone() {
      ViewRadius copy = new ViewRadius();
      copy.value = this.value;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof ViewRadius) {
         ViewRadius other = (ViewRadius)obj;
         return this.value == other.value;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.value});
   }
}
