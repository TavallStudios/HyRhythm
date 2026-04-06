package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import javax.annotation.Nonnull;

public class CombatTextUIComponentPositionAnimationEvent extends CombatTextUIComponentAnimationEvent {
   public static final BuilderCodec<CombatTextUIComponentPositionAnimationEvent> CODEC;
   private Vector2f positionOffset;

   @Nonnull
   public CombatTextEntityUIComponentAnimationEvent generatePacket() {
      CombatTextEntityUIComponentAnimationEvent packet = super.generatePacket();
      packet.type = CombatTextEntityUIAnimationEventType.Position;
      packet.positionOffset = this.positionOffset;
      return packet;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.positionOffset);
      return "CombatTextUIComponentPositionAnimationEvent{positionOffset=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(CombatTextUIComponentPositionAnimationEvent.class, CombatTextUIComponentPositionAnimationEvent::new, CombatTextUIComponentAnimationEvent.ABSTRACT_CODEC).appendInherited(new KeyedCodec("PositionOffset", ProtocolCodecs.VECTOR2F), (event, f) -> event.positionOffset = f, (event) -> event.positionOffset, (parent, event) -> event.positionOffset = parent.positionOffset).documentation("The offset from the starting position that the text instance should animate to.").addValidator(Validators.nonNull()).add()).build();
   }
}
