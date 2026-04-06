package com.hypixel.hytale.server.npc.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import javax.annotation.Nonnull;

public class NPCMessageCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> messageArg;
   @Nonnull
   private final OptionalArg<Double> expirationTimeArg;
   @Nonnull
   private final FlagArg allArg;
   @Nonnull
   private final EntityWrappedArg entityArg;

   public NPCMessageCommand() {
      super("message", "server.commands.npc.message.desc");
      this.messageArg = this.withRequiredArg("message", "server.commands.npc.message.message.desc", ArgTypes.STRING);
      this.expirationTimeArg = this.withOptionalArg("expiration", "server.commands.npc.message.expiration", ArgTypes.DOUBLE);
      this.allArg = this.withFlagArg("all", "server.commands.npc.message.all");
      this.entityArg = (EntityWrappedArg)this.withOptionalArg("entity", "server.commands.entity.entity.desc", ArgTypes.ENTITY_ID);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      String msg = (String)this.messageArg.get(context);
      double expiration = this.expirationTimeArg.provided(context) ? (Double)this.expirationTimeArg.get(context) : 1.0;
      if ((Boolean)this.allArg.get(context)) {
         store.forEachEntityParallel(NPCEntity.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            BeaconSupport beaconSupport = (BeaconSupport)archetypeChunk.getComponent(index, BeaconSupport.getComponentType());
            if (beaconSupport != null) {
               beaconSupport.postMessage(msg, ref, expiration);
            }

         });
      } else {
         Pair<Ref<EntityStore>, NPCEntity> targetNpcPair = NPCCommandUtils.getTargetNpc(context, this.entityArg, store);
         if (targetNpcPair != null) {
            Ref<EntityStore> targetNpcRef = (Ref)targetNpcPair.first();
            BeaconSupport beaconSupportComponent = (BeaconSupport)store.getComponent(targetNpcRef, BeaconSupport.getComponentType());
            if (beaconSupportComponent != null) {
               beaconSupportComponent.postMessage(msg, ref, expiration);
            }

         }
      }
   }
}
