package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class WindowAction {
   public static final int MAX_SIZE = 32768023;

   @Nonnull
   public static WindowAction deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      Object var10000;
      switch (typeId) {
         case 0 -> var10000 = CraftRecipeAction.deserialize(buf, offset + typeIdLen);
         case 1 -> var10000 = TierUpgradeAction.deserialize(buf, offset + typeIdLen);
         case 2 -> var10000 = SelectSlotAction.deserialize(buf, offset + typeIdLen);
         case 3 -> var10000 = ChangeBlockAction.deserialize(buf, offset + typeIdLen);
         case 4 -> var10000 = SetActiveAction.deserialize(buf, offset + typeIdLen);
         case 5 -> var10000 = CraftItemAction.deserialize(buf, offset + typeIdLen);
         case 6 -> var10000 = UpdateCategoryAction.deserialize(buf, offset + typeIdLen);
         case 7 -> var10000 = CancelCraftingAction.deserialize(buf, offset + typeIdLen);
         case 8 -> var10000 = SortItemsAction.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("WindowAction", typeId);
      }

      return (WindowAction)var10000;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      int var10001;
      switch (typeId) {
         case 0 -> var10001 = CraftRecipeAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> var10001 = TierUpgradeAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> var10001 = SelectSlotAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> var10001 = ChangeBlockAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> var10001 = SetActiveAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 5 -> var10001 = CraftItemAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 6 -> var10001 = UpdateCategoryAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 7 -> var10001 = CancelCraftingAction.computeBytesConsumed(buf, offset + typeIdLen);
         case 8 -> var10001 = SortItemsAction.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("WindowAction", typeId);
      }

      return typeIdLen + var10001;
   }

   public int getTypeId() {
      if (this instanceof CraftRecipeAction sub) {
         return 0;
      } else if (this instanceof TierUpgradeAction sub) {
         return 1;
      } else if (this instanceof SelectSlotAction sub) {
         return 2;
      } else if (this instanceof ChangeBlockAction sub) {
         return 3;
      } else if (this instanceof SetActiveAction sub) {
         return 4;
      } else if (this instanceof CraftItemAction sub) {
         return 5;
      } else if (this instanceof UpdateCategoryAction sub) {
         return 6;
      } else if (this instanceof CancelCraftingAction sub) {
         return 7;
      } else if (this instanceof SortItemsAction sub) {
         return 8;
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
         case 0 -> var10000 = CraftRecipeAction.validateStructure(buffer, offset + typeIdLen);
         case 1 -> var10000 = TierUpgradeAction.validateStructure(buffer, offset + typeIdLen);
         case 2 -> var10000 = SelectSlotAction.validateStructure(buffer, offset + typeIdLen);
         case 3 -> var10000 = ChangeBlockAction.validateStructure(buffer, offset + typeIdLen);
         case 4 -> var10000 = SetActiveAction.validateStructure(buffer, offset + typeIdLen);
         case 5 -> var10000 = CraftItemAction.validateStructure(buffer, offset + typeIdLen);
         case 6 -> var10000 = UpdateCategoryAction.validateStructure(buffer, offset + typeIdLen);
         case 7 -> var10000 = CancelCraftingAction.validateStructure(buffer, offset + typeIdLen);
         case 8 -> var10000 = SortItemsAction.validateStructure(buffer, offset + typeIdLen);
         default -> var10000 = ValidationResult.error("Unknown polymorphic type ID " + typeId + " for WindowAction");
      }

      return var10000;
   }
}
