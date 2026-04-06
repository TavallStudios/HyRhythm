package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.path.entities.PatrolPathMarkerEntity;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;

public class PrefabPathUpdateObservationAngleCommand extends AbstractWorldCommand {
   @Nonnull
   private final EntityWrappedArg entityIdArg;
   @Nonnull
   private final RequiredArg<Float> angleArg;

   public PrefabPathUpdateObservationAngleCommand() {
      super("observationAngle", "server.commands.npcpath.update.observationAngle.desc");
      this.entityIdArg = (EntityWrappedArg)this.withOptionalArg("entityId", "server.commands.npcpath.update.observationAngle.entityId.desc", ArgTypes.ENTITY_ID);
      this.angleArg = this.withRequiredArg("angle", "server.commands.npcpath.update.observationAngle.angle.desc", ArgTypes.FLOAT);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Float angleDegrees = (Float)this.angleArg.get(context);
      Ref<EntityStore> ref;
      if (this.entityIdArg.provided(context)) {
         ref = this.entityIdArg.get(store, context);
      } else {
         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef == null) {
            throw new GeneralCommandException(Message.translation("server.commands.errors.playerOrArg").param("option", "entity"));
         }

         if (!playerRef.isValid()) {
            throw new GeneralCommandException(Message.translation("server.commands.errors.playerNotInWorld").param("option", "entity"));
         }

         Ref<EntityStore> entityRef = TargetUtil.getTargetEntity(playerRef, store);
         if (entityRef == null) {
            throw new GeneralCommandException(Message.translation("server.commands.errors.no_entity_in_view").param("option", "entity"));
         }

         ref = entityRef;
      }

      PatrolPathMarkerEntity marker = (PatrolPathMarkerEntity)store.getComponent(ref, PatrolPathMarkerEntity.getComponentType());
      if (marker == null) {
         context.sendMessage(Message.translation("server.general.entityNotFound").param("id", ref.getIndex()));
      } else {
         marker.setObservationAngle(angleDegrees * 0.017453292F);
         marker.markNeedsSave();
      }
   }
}
