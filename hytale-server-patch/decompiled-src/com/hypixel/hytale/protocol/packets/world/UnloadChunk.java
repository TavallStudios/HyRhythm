package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UnloadChunk implements Packet, ToClientPacket {
   public static final int PACKET_ID = 135;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int chunkX;
   public int chunkZ;

   public int getId() {
      return 135;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public UnloadChunk() {
   }

   public UnloadChunk(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   public UnloadChunk(@Nonnull UnloadChunk other) {
      this.chunkX = other.chunkX;
      this.chunkZ = other.chunkZ;
   }

   @Nonnull
   public static UnloadChunk deserialize(@Nonnull ByteBuf buf, int offset) {
      UnloadChunk obj = new UnloadChunk();
      obj.chunkX = buf.getIntLE(offset + 0);
      obj.chunkZ = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.chunkX);
      buf.writeIntLE(this.chunkZ);
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public UnloadChunk clone() {
      UnloadChunk copy = new UnloadChunk();
      copy.chunkX = this.chunkX;
      copy.chunkZ = this.chunkZ;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UnloadChunk)) {
         return false;
      } else {
         UnloadChunk other = (UnloadChunk)obj;
         return this.chunkX == other.chunkX && this.chunkZ == other.chunkZ;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.chunkX, this.chunkZ});
   }
}
