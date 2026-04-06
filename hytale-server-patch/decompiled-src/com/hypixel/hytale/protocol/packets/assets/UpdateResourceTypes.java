package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ResourceType;
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

public class UpdateResourceTypes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 59;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type;
   @Nullable
   public Map<String, ResourceType> resourceTypes;

   public int getId() {
      return 59;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateResourceTypes() {
      this.type = UpdateType.Init;
   }

   public UpdateResourceTypes(@Nonnull UpdateType type, @Nullable Map<String, ResourceType> resourceTypes) {
      this.type = UpdateType.Init;
      this.type = type;
      this.resourceTypes = resourceTypes;
   }

   public UpdateResourceTypes(@Nonnull UpdateResourceTypes other) {
      this.type = UpdateType.Init;
      this.type = other.type;
      this.resourceTypes = other.resourceTypes;
   }

   @Nonnull
   public static UpdateResourceTypes deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateResourceTypes obj = new UpdateResourceTypes();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int resourceTypesCount = VarInt.peek(buf, pos);
         if (resourceTypesCount < 0) {
            throw ProtocolException.negativeLength("ResourceTypes", resourceTypesCount);
         }

         if (resourceTypesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ResourceTypes", resourceTypesCount, 4096000);
         }

         pos += VarInt.size(resourceTypesCount);
         obj.resourceTypes = new HashMap(resourceTypesCount);

         for(int i = 0; i < resourceTypesCount; ++i) {
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
            ResourceType val = ResourceType.deserialize(buf, pos);
            pos += ResourceType.computeBytesConsumed(buf, pos);
            if (obj.resourceTypes.put(key, val) != null) {
               throw ProtocolException.duplicateKey("resourceTypes", key);
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
            pos += ResourceType.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.resourceTypes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.resourceTypes != null) {
         if (this.resourceTypes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ResourceTypes", this.resourceTypes.size(), 4096000);
         }

         VarInt.write(buf, this.resourceTypes.size());

         for(Map.Entry<String, ResourceType> e : this.resourceTypes.entrySet()) {
            PacketIO.writeVarString(buf, (String)e.getKey(), 4096000);
            ((ResourceType)e.getValue()).serialize(buf);
         }
      }

   }

   public int computeSize() {
      int size = 2;
      if (this.resourceTypes != null) {
         int resourceTypesSize = 0;

         for(Map.Entry<String, ResourceType> kvp : this.resourceTypes.entrySet()) {
            resourceTypesSize += PacketIO.stringSize((String)kvp.getKey()) + ((ResourceType)kvp.getValue()).computeSize();
         }

         size += VarInt.size(this.resourceTypes.size()) + resourceTypesSize;
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
            int resourceTypesCount = VarInt.peek(buffer, pos);
            if (resourceTypesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ResourceTypes");
            }

            if (resourceTypesCount > 4096000) {
               return ValidationResult.error("ResourceTypes exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for(int i = 0; i < resourceTypesCount; ++i) {
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

               pos += ResourceType.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateResourceTypes clone() {
      UpdateResourceTypes copy = new UpdateResourceTypes();
      copy.type = this.type;
      if (this.resourceTypes != null) {
         Map<String, ResourceType> m = new HashMap();

         for(Map.Entry<String, ResourceType> e : this.resourceTypes.entrySet()) {
            m.put((String)e.getKey(), ((ResourceType)e.getValue()).clone());
         }

         copy.resourceTypes = m;
      }

      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UpdateResourceTypes)) {
         return false;
      } else {
         UpdateResourceTypes other = (UpdateResourceTypes)obj;
         return Objects.equals(this.type, other.type) && Objects.equals(this.resourceTypes, other.resourceTypes);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.resourceTypes});
   }
}
