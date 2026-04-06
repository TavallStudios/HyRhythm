package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.Coord;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeFloat;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import javax.annotation.Nonnull;

public class TeleportAllCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private final RequiredArg<Coord> xArg;
   @Nonnull
   private final RequiredArg<Coord> yArg;
   @Nonnull
   private final RequiredArg<Coord> zArg;
   @Nonnull
   private final OptionalArg<RelativeFloat> yawArg;
   @Nonnull
   private final OptionalArg<RelativeFloat> pitchArg;
   @Nonnull
   private final OptionalArg<RelativeFloat> rollArg;
   @Nonnull
   private final OptionalArg<World> worldArg;

   public TeleportAllCommand() {
      super("all", "server.commands.tpall.desc");
      this.xArg = this.withRequiredArg("x", "server.commands.teleport.x.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.yArg = this.withRequiredArg("y", "server.commands.teleport.y.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.zArg = this.withRequiredArg("z", "server.commands.teleport.z.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.yawArg = this.withOptionalArg("yaw", "server.commands.teleport.yaw.desc", ArgTypes.RELATIVE_FLOAT);
      this.pitchArg = this.withOptionalArg("pitch", "server.commands.teleport.pitch.desc", ArgTypes.RELATIVE_FLOAT);
      this.rollArg = this.withOptionalArg("roll", "server.commands.teleport.roll.desc", ArgTypes.RELATIVE_FLOAT);
      this.worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);
      this.setPermissionGroup((GameMode)null);
      this.requirePermission(HytalePermissions.fromCommand("teleport.all"));
   }

   protected void executeSync(@Nonnull CommandContext context) {
      Coord relX = (Coord)this.xArg.get(context);
      Coord relY = (Coord)this.yArg.get(context);
      Coord relZ = (Coord)this.zArg.get(context);
      World targetWorld;
      if (this.worldArg.provided(context)) {
         targetWorld = (World)this.worldArg.get(context);
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "world"));
            return;
         }

         Ref<EntityStore> senderRef = context.senderAsPlayerRef();
         if (senderRef == null || !senderRef.isValid()) {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return;
         }

         targetWorld = ((EntityStore)senderRef.getStore().getExternalData()).getWorld();
      }

      targetWorld.execute(() -> {
         Store<EntityStore> store = targetWorld.getEntityStore().getStore();
         double baseX = 0.0;
         double baseY = 0.0;
         double baseZ = 0.0;
         if (context.isPlayer()) {
            Ref<EntityStore> senderRef = context.senderAsPlayerRef();
            if (senderRef != null && senderRef.isValid()) {
               Store<EntityStore> senderStore = senderRef.getStore();
               World senderWorld = ((EntityStore)senderStore.getExternalData()).getWorld();
               if (senderWorld == targetWorld) {
                  TransformComponent transformComponent = (TransformComponent)senderStore.getComponent(senderRef, TransformComponent.getComponentType());
                  if (transformComponent != null) {
                     Vector3d pos = transformComponent.getPosition();
                     baseX = pos.getX();
                     baseY = pos.getY();
                     baseZ = pos.getZ();
                  }
               }
            }
         }

         double x = relX.resolveXZ(baseX);
         double z = relZ.resolveXZ(baseZ);
         double y = relY.resolveYAtWorldCoords(baseY, targetWorld, x, z);
         boolean hasRotation = this.yawArg.provided(context) || this.pitchArg.provided(context) || this.rollArg.provided(context);

         for(PlayerRef playerRef : targetWorld.getPlayerRefs()) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid()) {
               TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
               HeadRotation headRotationComponent = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());
               if (transformComponent != null && headRotationComponent != null) {
                  Vector3d previousPos = transformComponent.getPosition().clone();
                  Vector3f previousHeadRotation = headRotationComponent.getRotation().clone();
                  Vector3f previousBodyRotation = transformComponent.getRotation().clone();
                  float yaw = this.yawArg.provided(context) ? ((RelativeFloat)this.yawArg.get(context)).resolve(previousHeadRotation.getYaw() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
                  float pitch = this.pitchArg.provided(context) ? ((RelativeFloat)this.pitchArg.get(context)).resolve(previousHeadRotation.getPitch() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
                  float roll = this.rollArg.provided(context) ? ((RelativeFloat)this.rollArg.get(context)).resolve(previousHeadRotation.getRoll() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
                  Teleport teleport = Teleport.createExact(new Vector3d(x, y, z), new Vector3f(previousBodyRotation.getPitch(), yaw, previousBodyRotation.getRoll()), new Vector3f(pitch, yaw, roll));
                  store.addComponent(ref, Teleport.getComponentType(), teleport);
                  Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
                  if (playerComponent != null) {
                     PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

                     assert playerRefComponent != null;

                     TeleportHistory teleportHistoryComponent = (TeleportHistory)store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
                     teleportHistoryComponent.append(targetWorld, previousPos, previousHeadRotation, String.format("Teleport to (%s, %s, %s) by %s", x, y, z, context.sender().getDisplayName()));
                     if (hasRotation) {
                        float displayYaw = Float.isNaN(yaw) ? previousHeadRotation.getYaw() * 57.295776F : yaw * 57.295776F;
                        float displayPitch = Float.isNaN(pitch) ? previousHeadRotation.getPitch() * 57.295776F : pitch * 57.295776F;
                        float displayRoll = Float.isNaN(roll) ? previousHeadRotation.getRoll() * 57.295776F : roll * 57.295776F;
                        NotificationUtil.sendNotification(playerRefComponent.getPacketHandler(), Message.translation("server.commands.teleport.teleportedWithLookNotification").param("x", x).param("y", y).param("z", z).param("yaw", displayYaw).param("pitch", displayPitch).param("roll", displayRoll).param("sender", context.sender().getDisplayName()), (Message)null, (String)"teleportation");
                     } else {
                        NotificationUtil.sendNotification(playerRefComponent.getPacketHandler(), Message.translation("server.commands.teleport.teleportedToCoordinatesNotification").param("x", x).param("y", y).param("z", z).param("sender", context.sender().getDisplayName()), (Message)null, (String)"teleportation");
                     }
                  }
               }
            }
         }

         if (hasRotation) {
            float displayYaw = this.yawArg.provided(context) ? ((RelativeFloat)this.yawArg.get(context)).getRawValue() : 0.0F;
            float displayPitch = this.pitchArg.provided(context) ? ((RelativeFloat)this.pitchArg.get(context)).getRawValue() : 0.0F;
            float displayRoll = this.rollArg.provided(context) ? ((RelativeFloat)this.rollArg.get(context)).getRawValue() : 0.0F;
            context.sendMessage(Message.translation("server.commands.teleport.teleportEveryoneWithLook").param("world", targetWorld.getName()).param("x", x).param("y", y).param("z", z).param("yaw", displayYaw).param("pitch", displayPitch).param("roll", displayRoll));
         } else {
            context.sendMessage(Message.translation("server.commands.teleport.teleportEveryone").param("world", targetWorld.getName()).param("x", x).param("y", y).param("z", z));
         }

      });
   }
}
