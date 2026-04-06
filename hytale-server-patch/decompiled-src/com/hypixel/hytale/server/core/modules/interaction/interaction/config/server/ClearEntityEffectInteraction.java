package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ClearEntityEffectInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<ClearEntityEffectInteraction> CODEC;
   protected String entityEffectId;
   @Nonnull
   private InteractionTarget entityTarget;

   public ClearEntityEffectInteraction() {
      this.entityTarget = InteractionTarget.USER;
   }

   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> ref = context.getEntity();
      Ref<EntityStore> targetRef = this.entityTarget.getEntity(context, ref);
      if (targetRef != null && targetRef.isValid()) {
         EntityEffect entityEffect = (EntityEffect)EntityEffect.getAssetMap().getAsset(this.entityEffectId);
         if (entityEffect != null) {
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
            EffectControllerComponent effectControllerComponent = (EffectControllerComponent)commandBuffer.getComponent(targetRef, EffectControllerComponent.getComponentType());
            if (effectControllerComponent != null) {
               effectControllerComponent.removeEffect(targetRef, EntityEffect.getAssetMap().getIndex(this.entityEffectId), commandBuffer);
            }

         }
      }
   }

   @Nonnull
   protected Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ClearEntityEffectInteraction();
   }

   protected void configurePacket(Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ClearEntityEffectInteraction p = (com.hypixel.hytale.protocol.ClearEntityEffectInteraction)packet;
      p.effectId = EntityEffect.getAssetMap().getIndex(this.entityEffectId);
      p.entityTarget = this.entityTarget.toProtocol();
   }

   @Nonnull
   public String toString() {
      String var10000 = this.entityEffectId;
      return "ClearEntityEffectInteraction{entityEffectId='" + var10000 + "', entityTarget=" + String.valueOf(this.entityTarget) + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ClearEntityEffectInteraction.class, ClearEntityEffectInteraction::new, SimpleInstantInteraction.CODEC).documentation("Removes the given entity effect from the given entity.")).append(new KeyedCodec("EntityEffectId", Codec.STRING), (clearEntityEffectInteraction, string) -> clearEntityEffectInteraction.entityEffectId = string, (clearEntityEffectInteraction) -> clearEntityEffectInteraction.entityEffectId).addValidator(Validators.nonNull()).addValidatorLate(() -> EntityEffect.VALIDATOR_CACHE.getValidator().late()).add()).appendInherited(new KeyedCodec("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, (o) -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget).documentation("The entity to target for this interaction.").addValidator(Validators.nonNull()).add()).build();
   }
}
