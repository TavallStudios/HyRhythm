package com.hypixel.hytale.builtin.mounts.interactions;

import com.hypixel.hytale.builtin.mounts.MountedByComponent;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MountInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<MountInteraction> CODEC;
   private Vector3f attachmentOffset = new Vector3f(0.0F, 0.0F, 0.0F);
   private MountController controller;

   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      Ref<EntityStore> target = context.getTargetEntity();
      if (target == null) {
         context.getState().state = InteractionState.Failed;
      } else {
         Ref<EntityStore> self = context.getEntity();
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
         MountedComponent mounted = (MountedComponent)commandBuffer.getComponent(self, MountedComponent.getComponentType());
         if (mounted != null) {
            commandBuffer.removeComponent(self, MountedComponent.getComponentType());
            context.getState().state = InteractionState.Failed;
         } else {
            MountedByComponent mountedBy = (MountedByComponent)commandBuffer.getComponent(target, MountedByComponent.getComponentType());
            if (mountedBy != null && !mountedBy.getPassengers().isEmpty()) {
               context.getState().state = InteractionState.Failed;
            } else {
               commandBuffer.addComponent(self, MountedComponent.getComponentType(), new MountedComponent(target, this.attachmentOffset, this.controller));
            }
         }
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(MountInteraction.class, MountInteraction::new, SimpleInstantInteraction.CODEC).appendInherited(new KeyedCodec("AttachmentOffset", ProtocolCodecs.VECTOR3F), (o, v) -> o.attachmentOffset.assign(v.x, v.y, v.z), (o) -> new com.hypixel.hytale.protocol.Vector3f(o.attachmentOffset.x, o.attachmentOffset.y, o.attachmentOffset.z), (o, p) -> o.attachmentOffset = p.attachmentOffset).add()).appendInherited(new KeyedCodec("Controller", new EnumCodec(MountController.class)), (o, v) -> o.controller = v, (o) -> o.controller, (o, p) -> o.controller = p.controller).addValidator(Validators.nonNull()).add()).build();
   }
}
