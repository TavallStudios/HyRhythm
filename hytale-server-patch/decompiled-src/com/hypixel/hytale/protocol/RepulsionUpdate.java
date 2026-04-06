package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class RepulsionUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int repulsionConfigIndex;

   public RepulsionUpdate() {
   }

   public RepulsionUpdate(int repulsionConfigIndex) {
      this.repulsionConfigIndex = repulsionConfigIndex;
   }

   public RepulsionUpdate(@Nonnull RepulsionUpdate other) {
      this.repulsionConfigIndex = other.repulsionConfigIndex;
   }

   @Nonnull
   public static RepulsionUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      RepulsionUpdate obj = new RepulsionUpdate();
      obj.repulsionConfigIndex = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.repulsionConfigIndex);
      return buf.writerIndex() - startPos;
   }

   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public RepulsionUpdate clone() {
      RepulsionUpdate copy = new RepulsionUpdate();
      copy.repulsionConfigIndex = this.repulsionConfigIndex;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj instanceof RepulsionUpdate) {
         RepulsionUpdate other = (RepulsionUpdate)obj;
         return this.repulsionConfigIndex == other.repulsionConfigIndex;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.repulsionConfigIndex});
   }
}
