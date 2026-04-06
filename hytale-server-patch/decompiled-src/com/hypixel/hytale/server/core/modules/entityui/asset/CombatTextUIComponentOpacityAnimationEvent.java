package com.hypixel.hytale.server.core.modules.entityui.asset;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.CombatTextEntityUIAnimationEventType;
import com.hypixel.hytale.protocol.CombatTextEntityUIComponentAnimationEvent;
import javax.annotation.Nonnull;

public class CombatTextUIComponentOpacityAnimationEvent extends CombatTextUIComponentAnimationEvent {
   public static final BuilderCodec<CombatTextUIComponentOpacityAnimationEvent> CODEC;
   private float startOpacity;
   private float endOpacity;

   @Nonnull
   public CombatTextEntityUIComponentAnimationEvent generatePacket() {
      CombatTextEntityUIComponentAnimationEvent packet = super.generatePacket();
      packet.type = CombatTextEntityUIAnimationEventType.Opacity;
      packet.startOpacity = this.startOpacity;
      packet.endOpacity = this.endOpacity;
      return packet;
   }

   @Nonnull
   public String toString() {
      float var10000 = this.startOpacity;
      return "CombatTextUIComponentOpacityAnimationEvent{startOpacity=" + var10000 + ", endOpacity=" + this.endOpacity + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(CombatTextUIComponentOpacityAnimationEvent.class, CombatTextUIComponentOpacityAnimationEvent::new, CombatTextUIComponentAnimationEvent.ABSTRACT_CODEC).appendInherited(new KeyedCodec("StartOpacity", Codec.FLOAT), (event, f) -> event.startOpacity = f, (event) -> event.startOpacity, (parent, event) -> event.startOpacity = parent.startOpacity).documentation("The opacity that should be applied to text instances before the animation event begins.").addValidator(Validators.nonNull()).addValidator(Validators.range(0.0F, 1.0F)).add()).appendInherited(new KeyedCodec("EndOpacity", Codec.FLOAT), (event, f) -> event.endOpacity = f, (event) -> event.endOpacity, (parent, event) -> event.endOpacity = parent.endOpacity).documentation("The opacity that should be applied to text instances by the end of the animation.").addValidator(Validators.nonNull()).addValidator(Validators.range(0.0F, 1.0F)).add()).build();
   }
}
