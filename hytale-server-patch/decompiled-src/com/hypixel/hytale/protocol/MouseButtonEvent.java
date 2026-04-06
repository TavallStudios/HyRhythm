package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MouseButtonEvent {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public MouseButtonType mouseButtonType;
   @Nonnull
   public MouseButtonState state;
   public byte clicks;

   public MouseButtonEvent() {
      this.mouseButtonType = MouseButtonType.Left;
      this.state = MouseButtonState.Pressed;
   }

   public MouseButtonEvent(@Nonnull MouseButtonType mouseButtonType, @Nonnull MouseButtonState state, byte clicks) {
      this.mouseButtonType = MouseButtonType.Left;
      this.state = MouseButtonState.Pressed;
      this.mouseButtonType = mouseButtonType;
      this.state = state;
      this.clicks = clicks;
   }

   public MouseButtonEvent(@Nonnull MouseButtonEvent other) {
      this.mouseButtonType = MouseButtonType.Left;
      this.state = MouseButtonState.Pressed;
      this.mouseButtonType = other.mouseButtonType;
      this.state = other.state;
      this.clicks = other.clicks;
   }

   @Nonnull
   public static MouseButtonEvent deserialize(@Nonnull ByteBuf buf, int offset) {
      MouseButtonEvent obj = new MouseButtonEvent();
      obj.mouseButtonType = MouseButtonType.fromValue(buf.getByte(offset + 0));
      obj.state = MouseButtonState.fromValue(buf.getByte(offset + 1));
      obj.clicks = buf.getByte(offset + 2);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.mouseButtonType.getValue());
      buf.writeByte(this.state.getValue());
      buf.writeByte(this.clicks);
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
   }

   public MouseButtonEvent clone() {
      MouseButtonEvent copy = new MouseButtonEvent();
      copy.mouseButtonType = this.mouseButtonType;
      copy.state = this.state;
      copy.clicks = this.clicks;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof MouseButtonEvent)) {
         return false;
      } else {
         MouseButtonEvent other = (MouseButtonEvent)obj;
         return Objects.equals(this.mouseButtonType, other.mouseButtonType) && Objects.equals(this.state, other.state) && this.clicks == other.clicks;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.mouseButtonType, this.state, this.clicks});
   }
}
