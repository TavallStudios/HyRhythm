package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class ParamValue {
   public static final int MAX_SIZE = 16384011;

   @Nonnull
   public static ParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      Object var10000;
      switch (typeId) {
         case 0 -> var10000 = StringParamValue.deserialize(buf, offset + typeIdLen);
         case 1 -> var10000 = BoolParamValue.deserialize(buf, offset + typeIdLen);
         case 2 -> var10000 = DoubleParamValue.deserialize(buf, offset + typeIdLen);
         case 3 -> var10000 = IntParamValue.deserialize(buf, offset + typeIdLen);
         case 4 -> var10000 = LongParamValue.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ParamValue", typeId);
      }

      return (ParamValue)var10000;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      int var10001;
      switch (typeId) {
         case 0 -> var10001 = StringParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> var10001 = BoolParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> var10001 = DoubleParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> var10001 = IntParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> var10001 = LongParamValue.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ParamValue", typeId);
      }

      return typeIdLen + var10001;
   }

   public int getTypeId() {
      if (this instanceof StringParamValue sub) {
         return 0;
      } else if (this instanceof BoolParamValue sub) {
         return 1;
      } else if (this instanceof DoubleParamValue sub) {
         return 2;
      } else if (this instanceof IntParamValue sub) {
         return 3;
      } else if (this instanceof LongParamValue sub) {
         return 4;
      } else {
         throw new IllegalStateException("Unknown subtype: " + this.getClass().getName());
      }
   }

   public abstract int serialize(@Nonnull ByteBuf var1);

   public abstract int computeSize();

   public int serializeWithTypeId(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      VarInt.write(buf, this.getTypeId());
      this.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   public int computeSizeWithTypeId() {
      return VarInt.size(this.getTypeId()) + this.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      int typeId = VarInt.peek(buffer, offset);
      int typeIdLen = VarInt.length(buffer, offset);
      ValidationResult var10000;
      switch (typeId) {
         case 0 -> var10000 = StringParamValue.validateStructure(buffer, offset + typeIdLen);
         case 1 -> var10000 = BoolParamValue.validateStructure(buffer, offset + typeIdLen);
         case 2 -> var10000 = DoubleParamValue.validateStructure(buffer, offset + typeIdLen);
         case 3 -> var10000 = IntParamValue.validateStructure(buffer, offset + typeIdLen);
         case 4 -> var10000 = LongParamValue.validateStructure(buffer, offset + typeIdLen);
         default -> var10000 = ValidationResult.error("Unknown polymorphic type ID " + typeId + " for ParamValue");
      }

      return var10000;
   }
}
