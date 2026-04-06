package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class UIComponentsUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public int[] components = new int[0];

   public UIComponentsUpdate() {
   }

   public UIComponentsUpdate(@Nonnull int[] components) {
      this.components = components;
   }

   public UIComponentsUpdate(@Nonnull UIComponentsUpdate other) {
      this.components = other.components;
   }

   @Nonnull
   public static UIComponentsUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      UIComponentsUpdate obj = new UIComponentsUpdate();
      int pos = offset + 0;
      int componentsCount = VarInt.peek(buf, pos);
      if (componentsCount < 0) {
         throw ProtocolException.negativeLength("Components", componentsCount);
      } else if (componentsCount > 4096000) {
         throw ProtocolException.arrayTooLong("Components", componentsCount, 4096000);
      } else {
         int componentsVarLen = VarInt.size(componentsCount);
         if ((long)(pos + componentsVarLen) + (long)componentsCount * 4L > (long)buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Components", pos + componentsVarLen + componentsCount * 4, buf.readableBytes());
         } else {
            pos += componentsVarLen;
            obj.components = new int[componentsCount];

            for(int i = 0; i < componentsCount; ++i) {
               obj.components[i] = buf.getIntLE(pos + i * 4);
            }

            int var10000 = pos + componentsCount * 4;
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
      if (this.components.length > 4096000) {
         throw ProtocolException.arrayTooLong("Components", this.components.length, 4096000);
      } else {
         VarInt.write(buf, this.components.length);

         for(int item : this.components) {
            buf.writeIntLE(item);
         }

         return buf.writerIndex() - startPos;
      }
   }

   public int computeSize() {
      int size = 0;
      size += VarInt.size(this.components.length) + this.components.length * 4;
      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int componentsCount = VarInt.peek(buffer, pos);
         if (componentsCount < 0) {
            return ValidationResult.error("Invalid array count for Components");
         } else if (componentsCount > 4096000) {
            return ValidationResult.error("Components exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += componentsCount * 4;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Components") : ValidationResult.OK;
         }
      }
   }

   public UIComponentsUpdate clone() {
      UIComponentsUpdate copy = new UIComponentsUpdate();
      copy.components = Arrays.copyOf(this.components, this.components.length);
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof UIComponentsUpdate) {
         UIComponentsUpdate other = (UIComponentsUpdate)obj;
         return Arrays.equals(this.components, other.components);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode(this.components);
      return result;
   }
}
