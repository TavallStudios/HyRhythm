package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.BlockSet;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateBlockSets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 46;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type;
   @Nullable
   public Map<String, BlockSet> blockSets;

   public int getId() {
      return 46;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateBlockSets() {
      this.type = UpdateType.Init;
   }

   public UpdateBlockSets(@Nonnull UpdateType type, @Nullable Map<String, BlockSet> blockSets) {
      this.type = UpdateType.Init;
      this.type = type;
      this.blockSets = blockSets;
   }

   public UpdateBlockSets(@Nonnull UpdateBlockSets other) {
      this.type = UpdateType.Init;
      this.type = other.type;
      this.blockSets = other.blockSets;
   }

   @Nonnull
   public static UpdateBlockSets deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateBlockSets obj = new UpdateBlockSets();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int blockSetsCount = VarInt.peek(buf, pos);
         if (blockSetsCount < 0) {
            throw ProtocolException.negativeLength("BlockSets", blockSetsCount);
         }

         if (blockSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockSets", blockSetsCount, 4096000);
         }

         pos += VarInt.size(blockSetsCount);
         obj.blockSets = new HashMap(blockSetsCount);

         for(int i = 0; i < blockSetsCount; ++i) {
            int keyLen = VarInt.peek(buf, pos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, pos);
            String key = PacketIO.readVarString(buf, pos);
            pos += keyVarLen + keyLen;
            BlockSet val = BlockSet.deserialize(buf, pos);
            pos += BlockSet.computeBytesConsumed(buf, pos);
            if (obj.blockSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("blockSets", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for(int i = 0; i < dictLen; ++i) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
            pos += BlockSet.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.blockSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.blockSets != null) {
         if (this.blockSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("BlockSets", this.blockSets.size(), 4096000);
         }

         VarInt.write(buf, this.blockSets.size());

         for(Map.Entry<String, BlockSet> e : this.blockSets.entrySet()) {
            PacketIO.writeVarString(buf, (String)e.getKey(), 4096000);
            ((BlockSet)e.getValue()).serialize(buf);
         }
      }

   }

   public int computeSize() {
      int size = 2;
      if (this.blockSets != null) {
         int blockSetsSize = 0;

         for(Map.Entry<String, BlockSet> kvp : this.blockSets.entrySet()) {
            blockSetsSize += PacketIO.stringSize((String)kvp.getKey()) + ((BlockSet)kvp.getValue()).computeSize();
         }

         size += VarInt.size(this.blockSets.size()) + blockSetsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int blockSetsCount = VarInt.peek(buffer, pos);
            if (blockSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for BlockSets");
            }

            if (blockSetsCount > 4096000) {
               return ValidationResult.error("BlockSets exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for(int i = 0; i < blockSetsCount; ++i) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.length(buffer, pos);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += BlockSet.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateBlockSets clone() {
      UpdateBlockSets copy = new UpdateBlockSets();
      copy.type = this.type;
      if (this.blockSets != null) {
         Map<String, BlockSet> m = new HashMap();

         for(Map.Entry<String, BlockSet> e : this.blockSets.entrySet()) {
            m.put((String)e.getKey(), ((BlockSet)e.getValue()).clone());
         }

         copy.blockSets = m;
      }

      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UpdateBlockSets)) {
         return false;
      } else {
         UpdateBlockSets other = (UpdateBlockSets)obj;
         return Objects.equals(this.type, other.type) && Objects.equals(this.blockSets, other.blockSets);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.blockSets});
   }
}
