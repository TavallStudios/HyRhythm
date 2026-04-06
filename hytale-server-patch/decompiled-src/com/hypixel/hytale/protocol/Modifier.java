package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Modifier {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 6;
   @Nonnull
   public ModifierTarget target;
   @Nonnull
   public CalculationType calculationType;
   public float amount;

   public Modifier() {
      this.target = ModifierTarget.Min;
      this.calculationType = CalculationType.Additive;
   }

   public Modifier(@Nonnull ModifierTarget target, @Nonnull CalculationType calculationType, float amount) {
      this.target = ModifierTarget.Min;
      this.calculationType = CalculationType.Additive;
      this.target = target;
      this.calculationType = calculationType;
      this.amount = amount;
   }

   public Modifier(@Nonnull Modifier other) {
      this.target = ModifierTarget.Min;
      this.calculationType = CalculationType.Additive;
      this.target = other.target;
      this.calculationType = other.calculationType;
      this.amount = other.amount;
   }

   @Nonnull
   public static Modifier deserialize(@Nonnull ByteBuf buf, int offset) {
      Modifier obj = new Modifier();
      obj.target = ModifierTarget.fromValue(buf.getByte(offset + 0));
      obj.calculationType = CalculationType.fromValue(buf.getByte(offset + 1));
      obj.amount = buf.getFloatLE(offset + 2);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 6;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.target.getValue());
      buf.writeByte(this.calculationType.getValue());
      buf.writeFloatLE(this.amount);
   }

   public int computeSize() {
      return 6;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 6 ? ValidationResult.error("Buffer too small: expected at least 6 bytes") : ValidationResult.OK;
   }

   public Modifier clone() {
      Modifier copy = new Modifier();
      copy.target = this.target;
      copy.calculationType = this.calculationType;
      copy.amount = this.amount;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof Modifier)) {
         return false;
      } else {
         Modifier other = (Modifier)obj;
         return Objects.equals(this.target, other.target) && Objects.equals(this.calculationType, other.calculationType) && this.amount == other.amount;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.target, this.calculationType, this.amount});
   }
}
