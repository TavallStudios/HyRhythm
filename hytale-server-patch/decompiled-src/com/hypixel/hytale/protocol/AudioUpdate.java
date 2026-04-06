package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class AudioUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public int[] soundEventIds = new int[0];

   public AudioUpdate() {
   }

   public AudioUpdate(@Nonnull int[] soundEventIds) {
      this.soundEventIds = soundEventIds;
   }

   public AudioUpdate(@Nonnull AudioUpdate other) {
      this.soundEventIds = other.soundEventIds;
   }

   @Nonnull
   public static AudioUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      AudioUpdate obj = new AudioUpdate();
      int pos = offset + 0;
      int soundEventIdsCount = VarInt.peek(buf, pos);
      if (soundEventIdsCount < 0) {
         throw ProtocolException.negativeLength("SoundEventIds", soundEventIdsCount);
      } else if (soundEventIdsCount > 4096000) {
         throw ProtocolException.arrayTooLong("SoundEventIds", soundEventIdsCount, 4096000);
      } else {
         int soundEventIdsVarLen = VarInt.size(soundEventIdsCount);
         if ((long)(pos + soundEventIdsVarLen) + (long)soundEventIdsCount * 4L > (long)buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("SoundEventIds", pos + soundEventIdsVarLen + soundEventIdsCount * 4, buf.readableBytes());
         } else {
            pos += soundEventIdsVarLen;
            obj.soundEventIds = new int[soundEventIdsCount];

            for(int i = 0; i < soundEventIdsCount; ++i) {
               obj.soundEventIds[i] = buf.getIntLE(pos + i * 4);
            }

            int var10000 = pos + soundEventIdsCount * 4;
            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos) + arrLen * 4;
      return pos - offset;
   }

   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      if (this.soundEventIds.length > 4096000) {
         throw ProtocolException.arrayTooLong("SoundEventIds", this.soundEventIds.length, 4096000);
      } else {
         VarInt.write(buf, this.soundEventIds.length);

         for(int item : this.soundEventIds) {
            buf.writeIntLE(item);
         }

         return buf.writerIndex() - startPos;
      }
   }

   public int computeSize() {
      int size = 0;
      size += VarInt.size(this.soundEventIds.length) + this.soundEventIds.length * 4;
      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int soundEventIdsCount = VarInt.peek(buffer, pos);
         if (soundEventIdsCount < 0) {
            return ValidationResult.error("Invalid array count for SoundEventIds");
         } else if (soundEventIdsCount > 4096000) {
            return ValidationResult.error("SoundEventIds exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += soundEventIdsCount * 4;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading SoundEventIds") : ValidationResult.OK;
         }
      }
   }

   public AudioUpdate clone() {
      AudioUpdate copy = new AudioUpdate();
      copy.soundEventIds = Arrays.copyOf(this.soundEventIds, this.soundEventIds.length);
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof AudioUpdate) {
         AudioUpdate other = (AudioUpdate)obj;
         return Arrays.equals(this.soundEventIds, other.soundEventIds);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode(this.soundEventIds);
      return result;
   }
}
