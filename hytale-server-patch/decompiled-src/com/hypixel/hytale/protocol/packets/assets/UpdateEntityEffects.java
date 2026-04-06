package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.EntityEffect;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateEntityEffects implements Packet, ToClientPacket {
   public static final int PACKET_ID = 51;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type;
   public int maxId;
   @Nullable
   public Map<Integer, EntityEffect> entityEffects;

   public int getId() {
      return 51;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateEntityEffects() {
      this.type = UpdateType.Init;
   }

   public UpdateEntityEffects(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, EntityEffect> entityEffects) {
      this.type = UpdateType.Init;
      this.type = type;
      this.maxId = maxId;
      this.entityEffects = entityEffects;
   }

   public UpdateEntityEffects(@Nonnull UpdateEntityEffects other) {
      this.type = UpdateType.Init;
      this.type = other.type;
      this.maxId = other.maxId;
      this.entityEffects = other.entityEffects;
   }

   @Nonnull
   public static UpdateEntityEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateEntityEffects obj = new UpdateEntityEffects();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int entityEffectsCount = VarInt.peek(buf, pos);
         if (entityEffectsCount < 0) {
            throw ProtocolException.negativeLength("EntityEffects", entityEffectsCount);
         }

         if (entityEffectsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("EntityEffects", entityEffectsCount, 4096000);
         }

         pos += VarInt.size(entityEffectsCount);
         obj.entityEffects = new HashMap(entityEffectsCount);

         for(int i = 0; i < entityEffectsCount; ++i) {
            int key = buf.getIntLE(pos);
            pos += 4;
            EntityEffect val = EntityEffect.deserialize(buf, pos);
            pos += EntityEffect.computeBytesConsumed(buf, pos);
            if (obj.entityEffects.put(key, val) != null) {
               throw ProtocolException.duplicateKey("entityEffects", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for(int i = 0; i < dictLen; ++i) {
            pos += 4;
            pos += EntityEffect.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.entityEffects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.entityEffects != null) {
         if (this.entityEffects.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("EntityEffects", this.entityEffects.size(), 4096000);
         }

         VarInt.write(buf, this.entityEffects.size());

         for(Map.Entry<Integer, EntityEffect> e : this.entityEffects.entrySet()) {
            buf.writeIntLE((Integer)e.getKey());
            ((EntityEffect)e.getValue()).serialize(buf);
         }
      }

   }

   public int computeSize() {
      int size = 6;
      if (this.entityEffects != null) {
         int entityEffectsSize = 0;

         for(Map.Entry<Integer, EntityEffect> kvp : this.entityEffects.entrySet()) {
            entityEffectsSize += 4 + ((EntityEffect)kvp.getValue()).computeSize();
         }

         size += VarInt.size(this.entityEffects.size()) + entityEffectsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int entityEffectsCount = VarInt.peek(buffer, pos);
            if (entityEffectsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for EntityEffects");
            }

            if (entityEffectsCount > 4096000) {
               return ValidationResult.error("EntityEffects exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for(int i = 0; i < entityEffectsCount; ++i) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += EntityEffect.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateEntityEffects clone() {
      UpdateEntityEffects copy = new UpdateEntityEffects();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.entityEffects != null) {
         Map<Integer, EntityEffect> m = new HashMap();

         for(Map.Entry<Integer, EntityEffect> e : this.entityEffects.entrySet()) {
            m.put((Integer)e.getKey(), ((EntityEffect)e.getValue()).clone());
         }

         copy.entityEffects = m;
      }

      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UpdateEntityEffects)) {
         return false;
      } else {
         UpdateEntityEffects other = (UpdateEntityEffects)obj;
         return Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.entityEffects, other.entityEffects);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.maxId, this.entityEffects});
   }
}
