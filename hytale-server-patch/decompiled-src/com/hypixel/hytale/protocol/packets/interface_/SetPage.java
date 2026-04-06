package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetPage implements Packet, ToClientPacket {
   public static final int PACKET_ID = 216;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   @Nonnull
   public Page page;
   public boolean canCloseThroughInteraction;

   public int getId() {
      return 216;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetPage() {
      this.page = Page.None;
   }

   public SetPage(@Nonnull Page page, boolean canCloseThroughInteraction) {
      this.page = Page.None;
      this.page = page;
      this.canCloseThroughInteraction = canCloseThroughInteraction;
   }

   public SetPage(@Nonnull SetPage other) {
      this.page = Page.None;
      this.page = other.page;
      this.canCloseThroughInteraction = other.canCloseThroughInteraction;
   }

   @Nonnull
   public static SetPage deserialize(@Nonnull ByteBuf buf, int offset) {
      SetPage obj = new SetPage();
      obj.page = Page.fromValue(buf.getByte(offset + 0));
      obj.canCloseThroughInteraction = buf.getByte(offset + 1) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.page.getValue());
      buf.writeByte(this.canCloseThroughInteraction ? 1 : 0);
   }

   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public SetPage clone() {
      SetPage copy = new SetPage();
      copy.page = this.page;
      copy.canCloseThroughInteraction = this.canCloseThroughInteraction;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof SetPage)) {
         return false;
      } else {
         SetPage other = (SetPage)obj;
         return Objects.equals(this.page, other.page) && this.canCloseThroughInteraction == other.canCloseThroughInteraction;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.page, this.canCloseThroughInteraction});
   }
}
