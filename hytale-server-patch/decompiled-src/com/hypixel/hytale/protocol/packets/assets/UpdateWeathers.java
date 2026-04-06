package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.Weather;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateWeathers implements Packet, ToClientPacket {
   public static final int PACKET_ID = 47;
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
   public Map<Integer, Weather> weathers;

   public int getId() {
      return 47;
   }

   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateWeathers() {
      this.type = UpdateType.Init;
   }

   public UpdateWeathers(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, Weather> weathers) {
      this.type = UpdateType.Init;
      this.type = type;
      this.maxId = maxId;
      this.weathers = weathers;
   }

   public UpdateWeathers(@Nonnull UpdateWeathers other) {
      this.type = UpdateType.Init;
      this.type = other.type;
      this.maxId = other.maxId;
      this.weathers = other.weathers;
   }

   @Nonnull
   public static UpdateWeathers deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateWeathers obj = new UpdateWeathers();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int weathersCount = VarInt.peek(buf, pos);
         if (weathersCount < 0) {
            throw ProtocolException.negativeLength("Weathers", weathersCount);
         }

         if (weathersCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Weathers", weathersCount, 4096000);
         }

         pos += VarInt.size(weathersCount);
         obj.weathers = new HashMap(weathersCount);

         for(int i = 0; i < weathersCount; ++i) {
            int key = buf.getIntLE(pos);
            pos += 4;
            Weather val = Weather.deserialize(buf, pos);
            pos += Weather.computeBytesConsumed(buf, pos);
            if (obj.weathers.put(key, val) != null) {
               throw ProtocolException.duplicateKey("weathers", key);
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
            pos += Weather.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.weathers != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.weathers != null) {
         if (this.weathers.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Weathers", this.weathers.size(), 4096000);
         }

         VarInt.write(buf, this.weathers.size());

         for(Map.Entry<Integer, Weather> e : this.weathers.entrySet()) {
            buf.writeIntLE((Integer)e.getKey());
            ((Weather)e.getValue()).serialize(buf);
         }
      }

   }

   public int computeSize() {
      int size = 6;
      if (this.weathers != null) {
         int weathersSize = 0;

         for(Map.Entry<Integer, Weather> kvp : this.weathers.entrySet()) {
            weathersSize += 4 + ((Weather)kvp.getValue()).computeSize();
         }

         size += VarInt.size(this.weathers.size()) + weathersSize;
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
            int weathersCount = VarInt.peek(buffer, pos);
            if (weathersCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Weathers");
            }

            if (weathersCount > 4096000) {
               return ValidationResult.error("Weathers exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for(int i = 0; i < weathersCount; ++i) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += Weather.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateWeathers clone() {
      UpdateWeathers copy = new UpdateWeathers();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.weathers != null) {
         Map<Integer, Weather> m = new HashMap();

         for(Map.Entry<Integer, Weather> e : this.weathers.entrySet()) {
            m.put((Integer)e.getKey(), ((Weather)e.getValue()).clone());
         }

         copy.weathers = m;
      }

      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof UpdateWeathers)) {
         return false;
      } else {
         UpdateWeathers other = (UpdateWeathers)obj;
         return Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.weathers, other.weathers);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.maxId, this.weathers});
   }
}
