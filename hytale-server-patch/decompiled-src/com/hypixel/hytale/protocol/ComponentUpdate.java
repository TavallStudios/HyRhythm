package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public abstract class ComponentUpdate {
   public static final int MAX_SIZE = 1677721605;

   @Nonnull
   public static ComponentUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      Object var10000;
      switch (typeId) {
         case 0 -> var10000 = NameplateUpdate.deserialize(buf, offset + typeIdLen);
         case 1 -> var10000 = UIComponentsUpdate.deserialize(buf, offset + typeIdLen);
         case 2 -> var10000 = CombatTextUpdate.deserialize(buf, offset + typeIdLen);
         case 3 -> var10000 = ModelUpdate.deserialize(buf, offset + typeIdLen);
         case 4 -> var10000 = PlayerSkinUpdate.deserialize(buf, offset + typeIdLen);
         case 5 -> var10000 = ItemUpdate.deserialize(buf, offset + typeIdLen);
         case 6 -> var10000 = BlockUpdate.deserialize(buf, offset + typeIdLen);
         case 7 -> var10000 = EquipmentUpdate.deserialize(buf, offset + typeIdLen);
         case 8 -> var10000 = EntityStatsUpdate.deserialize(buf, offset + typeIdLen);
         case 9 -> var10000 = TransformUpdate.deserialize(buf, offset + typeIdLen);
         case 10 -> var10000 = MovementStatesUpdate.deserialize(buf, offset + typeIdLen);
         case 11 -> var10000 = EntityEffectsUpdate.deserialize(buf, offset + typeIdLen);
         case 12 -> var10000 = InteractionsUpdate.deserialize(buf, offset + typeIdLen);
         case 13 -> var10000 = DynamicLightUpdate.deserialize(buf, offset + typeIdLen);
         case 14 -> var10000 = InteractableUpdate.deserialize(buf, offset + typeIdLen);
         case 15 -> var10000 = IntangibleUpdate.deserialize(buf, offset + typeIdLen);
         case 16 -> var10000 = InvulnerableUpdate.deserialize(buf, offset + typeIdLen);
         case 17 -> var10000 = RespondToHitUpdate.deserialize(buf, offset + typeIdLen);
         case 18 -> var10000 = HitboxCollisionUpdate.deserialize(buf, offset + typeIdLen);
         case 19 -> var10000 = RepulsionUpdate.deserialize(buf, offset + typeIdLen);
         case 20 -> var10000 = PredictionUpdate.deserialize(buf, offset + typeIdLen);
         case 21 -> var10000 = AudioUpdate.deserialize(buf, offset + typeIdLen);
         case 22 -> var10000 = MountedUpdate.deserialize(buf, offset + typeIdLen);
         case 23 -> var10000 = NewSpawnUpdate.deserialize(buf, offset + typeIdLen);
         case 24 -> var10000 = ActiveAnimationsUpdate.deserialize(buf, offset + typeIdLen);
         case 25 -> var10000 = PropUpdate.deserialize(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ComponentUpdate", typeId);
      }

      return (ComponentUpdate)var10000;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      int var10001;
      switch (typeId) {
         case 0 -> var10001 = NameplateUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 1 -> var10001 = UIComponentsUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 2 -> var10001 = CombatTextUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 3 -> var10001 = ModelUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 4 -> var10001 = PlayerSkinUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 5 -> var10001 = ItemUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 6 -> var10001 = BlockUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 7 -> var10001 = EquipmentUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 8 -> var10001 = EntityStatsUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 9 -> var10001 = TransformUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 10 -> var10001 = MovementStatesUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 11 -> var10001 = EntityEffectsUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 12 -> var10001 = InteractionsUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 13 -> var10001 = DynamicLightUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 14 -> var10001 = InteractableUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 15 -> var10001 = IntangibleUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 16 -> var10001 = InvulnerableUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 17 -> var10001 = RespondToHitUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 18 -> var10001 = HitboxCollisionUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 19 -> var10001 = RepulsionUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 20 -> var10001 = PredictionUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 21 -> var10001 = AudioUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 22 -> var10001 = MountedUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 23 -> var10001 = NewSpawnUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 24 -> var10001 = ActiveAnimationsUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         case 25 -> var10001 = PropUpdate.computeBytesConsumed(buf, offset + typeIdLen);
         default -> throw ProtocolException.unknownPolymorphicType("ComponentUpdate", typeId);
      }

      return typeIdLen + var10001;
   }

   public int getTypeId() {
      if (this instanceof NameplateUpdate sub) {
         return 0;
      } else if (this instanceof UIComponentsUpdate sub) {
         return 1;
      } else if (this instanceof CombatTextUpdate sub) {
         return 2;
      } else if (this instanceof ModelUpdate sub) {
         return 3;
      } else if (this instanceof PlayerSkinUpdate sub) {
         return 4;
      } else if (this instanceof ItemUpdate sub) {
         return 5;
      } else if (this instanceof BlockUpdate sub) {
         return 6;
      } else if (this instanceof EquipmentUpdate sub) {
         return 7;
      } else if (this instanceof EntityStatsUpdate sub) {
         return 8;
      } else if (this instanceof TransformUpdate sub) {
         return 9;
      } else if (this instanceof MovementStatesUpdate sub) {
         return 10;
      } else if (this instanceof EntityEffectsUpdate sub) {
         return 11;
      } else if (this instanceof InteractionsUpdate sub) {
         return 12;
      } else if (this instanceof DynamicLightUpdate sub) {
         return 13;
      } else if (this instanceof InteractableUpdate sub) {
         return 14;
      } else if (this instanceof IntangibleUpdate sub) {
         return 15;
      } else if (this instanceof InvulnerableUpdate sub) {
         return 16;
      } else if (this instanceof RespondToHitUpdate sub) {
         return 17;
      } else if (this instanceof HitboxCollisionUpdate sub) {
         return 18;
      } else if (this instanceof RepulsionUpdate sub) {
         return 19;
      } else if (this instanceof PredictionUpdate sub) {
         return 20;
      } else if (this instanceof AudioUpdate sub) {
         return 21;
      } else if (this instanceof MountedUpdate sub) {
         return 22;
      } else if (this instanceof NewSpawnUpdate sub) {
         return 23;
      } else if (this instanceof ActiveAnimationsUpdate sub) {
         return 24;
      } else if (this instanceof PropUpdate sub) {
         return 25;
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
         case 0 -> var10000 = NameplateUpdate.validateStructure(buffer, offset + typeIdLen);
         case 1 -> var10000 = UIComponentsUpdate.validateStructure(buffer, offset + typeIdLen);
         case 2 -> var10000 = CombatTextUpdate.validateStructure(buffer, offset + typeIdLen);
         case 3 -> var10000 = ModelUpdate.validateStructure(buffer, offset + typeIdLen);
         case 4 -> var10000 = PlayerSkinUpdate.validateStructure(buffer, offset + typeIdLen);
         case 5 -> var10000 = ItemUpdate.validateStructure(buffer, offset + typeIdLen);
         case 6 -> var10000 = BlockUpdate.validateStructure(buffer, offset + typeIdLen);
         case 7 -> var10000 = EquipmentUpdate.validateStructure(buffer, offset + typeIdLen);
         case 8 -> var10000 = EntityStatsUpdate.validateStructure(buffer, offset + typeIdLen);
         case 9 -> var10000 = TransformUpdate.validateStructure(buffer, offset + typeIdLen);
         case 10 -> var10000 = MovementStatesUpdate.validateStructure(buffer, offset + typeIdLen);
         case 11 -> var10000 = EntityEffectsUpdate.validateStructure(buffer, offset + typeIdLen);
         case 12 -> var10000 = InteractionsUpdate.validateStructure(buffer, offset + typeIdLen);
         case 13 -> var10000 = DynamicLightUpdate.validateStructure(buffer, offset + typeIdLen);
         case 14 -> var10000 = InteractableUpdate.validateStructure(buffer, offset + typeIdLen);
         case 15 -> var10000 = IntangibleUpdate.validateStructure(buffer, offset + typeIdLen);
         case 16 -> var10000 = InvulnerableUpdate.validateStructure(buffer, offset + typeIdLen);
         case 17 -> var10000 = RespondToHitUpdate.validateStructure(buffer, offset + typeIdLen);
         case 18 -> var10000 = HitboxCollisionUpdate.validateStructure(buffer, offset + typeIdLen);
         case 19 -> var10000 = RepulsionUpdate.validateStructure(buffer, offset + typeIdLen);
         case 20 -> var10000 = PredictionUpdate.validateStructure(buffer, offset + typeIdLen);
         case 21 -> var10000 = AudioUpdate.validateStructure(buffer, offset + typeIdLen);
         case 22 -> var10000 = MountedUpdate.validateStructure(buffer, offset + typeIdLen);
         case 23 -> var10000 = NewSpawnUpdate.validateStructure(buffer, offset + typeIdLen);
         case 24 -> var10000 = ActiveAnimationsUpdate.validateStructure(buffer, offset + typeIdLen);
         case 25 -> var10000 = PropUpdate.validateStructure(buffer, offset + typeIdLen);
         default -> var10000 = ValidationResult.error("Unknown polymorphic type ID " + typeId + " for ComponentUpdate");
      }

      return var10000;
   }
}
