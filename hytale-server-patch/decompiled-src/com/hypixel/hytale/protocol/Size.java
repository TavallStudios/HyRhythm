package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Size {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int width;
   public int height;

   public Size() {
   }

   public Size(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public Size(@Nonnull Size other) {
      this.width = other.width;
      this.height = other.height;
   }

   @Nonnull
   public static Size deserialize(@Nonnull ByteBuf buf, int offset) {
      Size obj = new Size();
      obj.width = buf.getIntLE(offset + 0);
      obj.height = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public Size clone() {
      Size copy = new Size();
      copy.width = this.width;
      copy.height = this.height;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof Size)) {
         return false;
      } else {
         Size other = (Size)obj;
         return this.width == other.width && this.height == other.height;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.width, this.height});
   }
}
