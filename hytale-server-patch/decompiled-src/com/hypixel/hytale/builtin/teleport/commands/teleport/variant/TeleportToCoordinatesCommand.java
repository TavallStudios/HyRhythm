package com.hypixel.hytale.builtin.teleport.commands.teleport.variant;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.Coord;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeFloat;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportToCoordinatesCommand extends AbstractPlayerCommand {
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

   public TeleportToCoordinatesCommand() {
      super("server.commands.teleport.toCoordinates.desc");
      this.xArg = this.withRequiredArg("x", "server.commands.teleport.x.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.yArg = this.withRequiredArg("y", "server.commands.teleport.y.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.zArg = this.withRequiredArg("z", "server.commands.teleport.z.desc", ArgTypes.RELATIVE_DOUBLE_COORD);
      this.yawArg = this.withOptionalArg("yaw", "server.commands.teleport.yaw.desc", ArgTypes.RELATIVE_FLOAT);
      this.pitchArg = this.withOptionalArg("pitch", "server.commands.teleport.pitch.desc", ArgTypes.RELATIVE_FLOAT);
      this.rollArg = this.withOptionalArg("roll", "server.commands.teleport.roll.desc", ArgTypes.RELATIVE_FLOAT);
      this.requirePermission(HytalePermissions.fromCommand("teleport.self"));
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      HeadRotation headRotationComponent = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3d previousPos = transformComponent.getPosition().clone();
      Vector3f previousHeadRotation = headRotationComponent.getRotation().clone();
      Vector3f previousBodyRotation = transformComponent.getRotation().clone();
      Coord relX = (Coord)this.xArg.get(context);
      Coord relY = (Coord)this.yArg.get(context);
      Coord relZ = (Coord)this.zArg.get(context);
      double x = relX.resolveXZ(previousPos.getX());
      double z = relZ.resolveXZ(previousPos.getZ());
      double y = relY.resolveYAtWorldCoords(previousPos.getY(), world, x, z);
      float yaw = this.yawArg.provided(context) ? ((RelativeFloat)this.yawArg.get(context)).resolve(previousHeadRotation.getYaw() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
      float pitch = this.pitchArg.provided(context) ? ((RelativeFloat)this.pitchArg.get(context)).resolve(previousHeadRotation.getPitch() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
      float roll = this.rollArg.provided(context) ? ((RelativeFloat)this.rollArg.get(context)).resolve(previousHeadRotation.getRoll() * 57.295776F) * 0.017453292F : 0.0F / 0.0F;
      Teleport teleport = Teleport.createForPlayer(new Vector3d(x, y, z), new Vector3f(previousBodyRotation.getPitch(), yaw, previousBodyRotation.getRoll())).setHeadRotation(new Vector3f(pitch, yaw, roll));
      store.addComponent(ref, Teleport.getComponentType(), teleport);
      boolean hasRotation = this.yawArg.provided(context) || this.pitchArg.provided(context) || this.rollArg.provided(context);
      if (hasRotation) {
         float displayYaw = Float.isNaN(yaw) ? previousHeadRotation.getYaw() * 57.295776F : yaw * 57.295776F;
         float displayPitch = Float.isNaN(pitch) ? previousHeadRotation.getPitch() * 57.295776F : pitch * 57.295776F;
         float displayRoll = Float.isNaN(roll) ? previousHeadRotation.getRoll() * 57.295776F : roll * 57.295776F;
         context.sendMessage(Message.translation("server.commands.teleport.teleportedToCoordinatesWithLook").param("x", x).param("y", y).param("z", z).param("yaw", displayYaw).param("pitch", displayPitch).param("roll", displayRoll));
      } else {
         context.sendMessage(Message.translation("server.commands.teleport.teleportedToCoordinates").param("x", x).param("y", y).param("z", z));
      }

      ((TeleportHistory)store.ensureAndGetComponent(ref, TeleportHistory.getComponentType())).append(world, previousPos, previousHeadRotation, String.format("Teleport to (%s, %s, %s)", x, y, z));
   }
}
