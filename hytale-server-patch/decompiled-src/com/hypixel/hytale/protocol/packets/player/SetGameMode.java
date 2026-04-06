package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetGameMode implements Packet, ToClientPacket {
   public static final int PACKET_ID = 101;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public GameMode gameMode;

   public int getId() {
      return 101;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public SetGameMode() {
      this.gameMode = GameMode.Adventure;
   }

   public SetGameMode(@Nonnull GameMode gameMode) {
      this.gameMode = GameMode.Adventure;
      this.gameMode = gameMode;
   }

   public SetGameMode(@Nonnull SetGameMode other) {
      this.gameMode = GameMode.Adventure;
      this.gameMode = other.gameMode;
   }

   @Nonnull
   public static SetGameMode deserialize(@Nonnull ByteBuf buf, int offset) {
      SetGameMode obj = new SetGameMode();
      obj.gameMode = GameMode.fromValue(buf.getByte(offset + 0));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.gameMode.getValue());
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public SetGameMode clone() {
      SetGameMode copy = new SetGameMode();
      copy.gameMode = this.gameMode;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof SetGameMode) {
         SetGameMode other = (SetGameMode)obj;
         return Objects.equals(this.gameMode, other.gameMode);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.gameMode});
   }
}
