package com.hypixel.hytale.builtin.teleport.commands.teleport.variant;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportToPlayerCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD = Message.translation("server.commands.errors.targetNotInWorld");
   @Nonnull
   private final RequiredArg<PlayerRef> targetPlayerArg;

   public TeleportToPlayerCommand() {
      super("server.commands.teleport.toPlayer.desc");
      this.targetPlayerArg = this.withRequiredArg("targetPlayer", "server.commands.teleport.targetPlayer.desc", ArgTypes.PLAYER_REF);
      this.requirePermission(HytalePermissions.fromCommand("teleport.self"));
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      PlayerRef targetPlayerRef = (PlayerRef)this.targetPlayerArg.get(context);
      Ref<EntityStore> targetRef = targetPlayerRef.getReference();
      if (targetRef != null && targetRef.isValid()) {
         Store<EntityStore> targetStore = targetRef.getStore();
         World targetWorld = ((EntityStore)targetStore.getExternalData()).getWorld();
         TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         HeadRotation headRotationComponent = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3d pos = transformComponent.getPosition().clone();
         Vector3f rotation = headRotationComponent.getRotation().clone();
         targetWorld.execute(() -> {
            TransformComponent targetTransformComponent = (TransformComponent)targetStore.getComponent(targetRef, TransformComponent.getComponentType());

            assert targetTransformComponent != null;

            HeadRotation targetHeadRotationComponent = (HeadRotation)targetStore.getComponent(targetRef, HeadRotation.getComponentType());

            assert targetHeadRotationComponent != null;

            Vector3d targetPosition = targetTransformComponent.getPosition().clone();
            Vector3f targetHeadRotation = targetHeadRotationComponent.getRotation().clone();
            Transform targetTransform = new Transform(targetPosition, targetHeadRotation);
            world.execute(() -> {
               Teleport teleportComponent = Teleport.createForPlayer(targetWorld, targetTransform);
               store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
               PlayerRef targetPlayerRefComponent = (PlayerRef)targetStore.getComponent(targetRef, PlayerRef.getComponentType());

               assert targetPlayerRefComponent != null;

               context.sendMessage(Message.translation("server.commands.teleport.teleportedToPlayer").param("toName", targetPlayerRefComponent.getUsername()));
               ((TeleportHistory)store.ensureAndGetComponent(ref, TeleportHistory.getComponentType())).append(world, pos, rotation, "Teleport to " + targetPlayerRefComponent.getUsername());
            });
         });
      } else {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_TARGET_NOT_IN_WORLD);
      }
   }
}
