package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Interaction {
   public static final int MAX_SIZE = 1677721605;
   @Nonnull
   public WaitForDataFrom waitForDataFrom;
   @Nullable
   public InteractionEffects effects;
   public float horizontalSpeedMultiplier;
   public float runTime;
   public boolean cancelOnItemChange;
   @Nullable
   public Map<GameMode, InteractionSettings> settings;
   @Nullable
   public InteractionRules rules;
   @Nullable
   public int[] tags;
   @Nullable
   public InteractionCameraSettings camera;

   public Interaction() {
      this.waitForDataFrom = WaitForDataFrom.Client;
   }

   @Nonnull
   public static Interaction deserialize(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      Object var10000;
      switch (typeId) {
         case 0:
            var10000 = SimpleBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 1:
            var10000 = SimpleInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 2:
            var10000 = PlaceBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 3:
            var10000 = BreakBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 4:
            var10000 = PickBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 5:
            var10000 = UseBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 6:
            var10000 = UseEntityInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 7:
            var10000 = BuilderToolInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 8:
            var10000 = ModifyInventoryInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 9:
            var10000 = ChargingInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 10:
            var10000 = WieldingInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 11:
            var10000 = ChainingInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 12:
            var10000 = ConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 13:
            var10000 = StatsConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 14:
            var10000 = BlockConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 15:
            var10000 = ReplaceInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 16:
            var10000 = ChangeBlockInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 17:
            var10000 = ChangeStateInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 18:
            var10000 = FirstClickInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 19:
         default:
            throw ProtocolException.unknownPolymorphicType("Interaction", typeId);
         case 20:
            var10000 = SelectInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 21:
            var10000 = DamageEntityInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 22:
            var10000 = RepeatInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 23:
            var10000 = ParallelInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 24:
            var10000 = ChangeActiveSlotInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 25:
            var10000 = EffectConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 26:
            var10000 = ApplyForceInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 27:
            var10000 = ApplyEffectInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 28:
            var10000 = ClearEntityEffectInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 29:
            var10000 = SerialInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 30:
            var10000 = ChangeStatInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 31:
            var10000 = MovementConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 32:
            var10000 = ProjectileInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 33:
            var10000 = RemoveEntityInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 34:
            var10000 = ResetCooldownInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 35:
            var10000 = TriggerCooldownInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 36:
            var10000 = CooldownConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 37:
            var10000 = ChainFlagInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 38:
            var10000 = IncrementCooldownInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 39:
            var10000 = CancelChainInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 40:
            var10000 = RunRootInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 41:
            var10000 = CameraInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 42:
            var10000 = SpawnDeployableFromRaycastInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 43:
            var10000 = MemoriesConditionInteraction.deserialize(buf, offset + typeIdLen);
            break;
         case 44:
            var10000 = ToggleGliderInteraction.deserialize(buf, offset + typeIdLen);
      }

      return (Interaction)var10000;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int typeId = VarInt.peek(buf, offset);
      int typeIdLen = VarInt.length(buf, offset);
      int var10001;
      switch (typeId) {
         case 0:
            var10001 = SimpleBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 1:
            var10001 = SimpleInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 2:
            var10001 = PlaceBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 3:
            var10001 = BreakBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 4:
            var10001 = PickBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 5:
            var10001 = UseBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 6:
            var10001 = UseEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 7:
            var10001 = BuilderToolInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 8:
            var10001 = ModifyInventoryInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 9:
            var10001 = ChargingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 10:
            var10001 = WieldingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 11:
            var10001 = ChainingInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 12:
            var10001 = ConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 13:
            var10001 = StatsConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 14:
            var10001 = BlockConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 15:
            var10001 = ReplaceInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 16:
            var10001 = ChangeBlockInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 17:
            var10001 = ChangeStateInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 18:
            var10001 = FirstClickInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 19:
         default:
            throw ProtocolException.unknownPolymorphicType("Interaction", typeId);
         case 20:
            var10001 = SelectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 21:
            var10001 = DamageEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 22:
            var10001 = RepeatInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 23:
            var10001 = ParallelInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 24:
            var10001 = ChangeActiveSlotInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 25:
            var10001 = EffectConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 26:
            var10001 = ApplyForceInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 27:
            var10001 = ApplyEffectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 28:
            var10001 = ClearEntityEffectInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 29:
            var10001 = SerialInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 30:
            var10001 = ChangeStatInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 31:
            var10001 = MovementConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 32:
            var10001 = ProjectileInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 33:
            var10001 = RemoveEntityInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 34:
            var10001 = ResetCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 35:
            var10001 = TriggerCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 36:
            var10001 = CooldownConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 37:
            var10001 = ChainFlagInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 38:
            var10001 = IncrementCooldownInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 39:
            var10001 = CancelChainInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 40:
            var10001 = RunRootInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 41:
            var10001 = CameraInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 42:
            var10001 = SpawnDeployableFromRaycastInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 43:
            var10001 = MemoriesConditionInteraction.computeBytesConsumed(buf, offset + typeIdLen);
            break;
         case 44:
            var10001 = ToggleGliderInteraction.computeBytesConsumed(buf, offset + typeIdLen);
      }

      return typeIdLen + var10001;
   }

   public int getTypeId() {
      if (this instanceof BreakBlockInteraction sub) {
         return 3;
      } else if (this instanceof PickBlockInteraction sub) {
         return 4;
      } else if (this instanceof UseBlockInteraction sub) {
         return 5;
      } else if (this instanceof BlockConditionInteraction sub) {
         return 14;
      } else if (this instanceof ChangeBlockInteraction sub) {
         return 16;
      } else if (this instanceof ChangeStateInteraction sub) {
         return 17;
      } else if (this instanceof SimpleBlockInteraction sub) {
         return 0;
      } else if (this instanceof PlaceBlockInteraction sub) {
         return 2;
      } else if (this instanceof UseEntityInteraction sub) {
         return 6;
      } else if (this instanceof BuilderToolInteraction sub) {
         return 7;
      } else if (this instanceof ModifyInventoryInteraction sub) {
         return 8;
      } else if (this instanceof WieldingInteraction sub) {
         return 10;
      } else if (this instanceof ConditionInteraction sub) {
         return 12;
      } else if (this instanceof StatsConditionInteraction sub) {
         return 13;
      } else if (this instanceof SelectInteraction sub) {
         return 20;
      } else if (this instanceof RepeatInteraction sub) {
         return 22;
      } else if (this instanceof EffectConditionInteraction sub) {
         return 25;
      } else if (this instanceof ApplyForceInteraction sub) {
         return 26;
      } else if (this instanceof ApplyEffectInteraction sub) {
         return 27;
      } else if (this instanceof ClearEntityEffectInteraction sub) {
         return 28;
      } else if (this instanceof ChangeStatInteraction sub) {
         return 30;
      } else if (this instanceof MovementConditionInteraction sub) {
         return 31;
      } else if (this instanceof ProjectileInteraction sub) {
         return 32;
      } else if (this instanceof RemoveEntityInteraction sub) {
         return 33;
      } else if (this instanceof ResetCooldownInteraction sub) {
         return 34;
      } else if (this instanceof TriggerCooldownInteraction sub) {
         return 35;
      } else if (this instanceof CooldownConditionInteraction sub) {
         return 36;
      } else if (this instanceof ChainFlagInteraction sub) {
         return 37;
      } else if (this instanceof IncrementCooldownInteraction sub) {
         return 38;
      } else if (this instanceof CancelChainInteraction sub) {
         return 39;
      } else if (this instanceof RunRootInteraction sub) {
         return 40;
      } else if (this instanceof CameraInteraction sub) {
         return 41;
      } else if (this instanceof SpawnDeployableFromRaycastInteraction sub) {
         return 42;
      } else if (this instanceof ToggleGliderInteraction sub) {
         return 44;
      } else if (this instanceof SimpleInteraction sub) {
         return 1;
      } else if (this instanceof ChargingInteraction sub) {
         return 9;
      } else if (this instanceof ChainingInteraction sub) {
         return 11;
      } else if (this instanceof ReplaceInteraction sub) {
         return 15;
      } else if (this instanceof FirstClickInteraction sub) {
         return 18;
      } else if (this instanceof DamageEntityInteraction sub) {
         return 21;
      } else if (this instanceof ParallelInteraction sub) {
         return 23;
      } else if (this instanceof ChangeActiveSlotInteraction sub) {
         return 24;
      } else if (this instanceof SerialInteraction sub) {
         return 29;
      } else if (this instanceof MemoriesConditionInteraction sub) {
         return 43;
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
         case 0:
            var10000 = SimpleBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 1:
            var10000 = SimpleInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 2:
            var10000 = PlaceBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 3:
            var10000 = BreakBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 4:
            var10000 = PickBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 5:
            var10000 = UseBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 6:
            var10000 = UseEntityInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 7:
            var10000 = BuilderToolInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 8:
            var10000 = ModifyInventoryInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 9:
            var10000 = ChargingInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 10:
            var10000 = WieldingInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 11:
            var10000 = ChainingInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 12:
            var10000 = ConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 13:
            var10000 = StatsConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 14:
            var10000 = BlockConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 15:
            var10000 = ReplaceInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 16:
            var10000 = ChangeBlockInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 17:
            var10000 = ChangeStateInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 18:
            var10000 = FirstClickInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 19:
         default:
            var10000 = ValidationResult.error("Unknown polymorphic type ID " + typeId + " for Interaction");
            break;
         case 20:
            var10000 = SelectInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 21:
            var10000 = DamageEntityInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 22:
            var10000 = RepeatInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 23:
            var10000 = ParallelInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 24:
            var10000 = ChangeActiveSlotInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 25:
            var10000 = EffectConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 26:
            var10000 = ApplyForceInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 27:
            var10000 = ApplyEffectInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 28:
            var10000 = ClearEntityEffectInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 29:
            var10000 = SerialInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 30:
            var10000 = ChangeStatInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 31:
            var10000 = MovementConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 32:
            var10000 = ProjectileInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 33:
            var10000 = RemoveEntityInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 34:
            var10000 = ResetCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 35:
            var10000 = TriggerCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 36:
            var10000 = CooldownConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 37:
            var10000 = ChainFlagInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 38:
            var10000 = IncrementCooldownInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 39:
            var10000 = CancelChainInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 40:
            var10000 = RunRootInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 41:
            var10000 = CameraInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 42:
            var10000 = SpawnDeployableFromRaycastInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 43:
            var10000 = MemoriesConditionInteraction.validateStructure(buffer, offset + typeIdLen);
            break;
         case 44:
            var10000 = ToggleGliderInteraction.validateStructure(buffer, offset + typeIdLen);
      }

      return var10000;
   }
}
