package com.hypixel.hytale.protocol.packets.inventory;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.SmartMoveType;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SmartMoveItemStack implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 176;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 13;
   public int fromSectionId;
   public int fromSlotId;
   public int quantity;
   @Nonnull
   public SmartMoveType moveType;

   public int getId() {
      return 176;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SmartMoveItemStack() {
      this.moveType = SmartMoveType.EquipOrMergeStack;
   }

   public SmartMoveItemStack(int fromSectionId, int fromSlotId, int quantity, @Nonnull SmartMoveType moveType) {
      this.moveType = SmartMoveType.EquipOrMergeStack;
      this.fromSectionId = fromSectionId;
      this.fromSlotId = fromSlotId;
      this.quantity = quantity;
      this.moveType = moveType;
   }

   public SmartMoveItemStack(@Nonnull SmartMoveItemStack other) {
      this.moveType = SmartMoveType.EquipOrMergeStack;
      this.fromSectionId = other.fromSectionId;
      this.fromSlotId = other.fromSlotId;
      this.quantity = other.quantity;
      this.moveType = other.moveType;
   }

   @Nonnull
   public static SmartMoveItemStack deserialize(@Nonnull ByteBuf buf, int offset) {
      SmartMoveItemStack obj = new SmartMoveItemStack();
      obj.fromSectionId = buf.getIntLE(offset + 0);
      obj.fromSlotId = buf.getIntLE(offset + 4);
      obj.quantity = buf.getIntLE(offset + 8);
      obj.moveType = SmartMoveType.fromValue(buf.getByte(offset + 12));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.fromSectionId);
      buf.writeIntLE(this.fromSlotId);
      buf.writeIntLE(this.quantity);
      buf.writeByte(this.moveType.getValue());
   }

   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 13 ? ValidationResult.error("Buffer too small: expected at least 13 bytes") : ValidationResult.OK;
   }

   public SmartMoveItemStack clone() {
      SmartMoveItemStack copy = new SmartMoveItemStack();
      copy.fromSectionId = this.fromSectionId;
      copy.fromSlotId = this.fromSlotId;
      copy.quantity = this.quantity;
      copy.moveType = this.moveType;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof SmartMoveItemStack)) {
         return false;
      } else {
         SmartMoveItemStack other = (SmartMoveItemStack)obj;
         return this.fromSectionId == other.fromSectionId && this.fromSlotId == other.fromSlotId && this.quantity == other.quantity && Objects.equals(this.moveType, other.moveType);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.fromSectionId, this.fromSlotId, this.quantity, this.moveType});
   }
}
