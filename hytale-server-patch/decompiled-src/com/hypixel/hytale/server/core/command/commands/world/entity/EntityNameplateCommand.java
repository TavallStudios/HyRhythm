package com.hypixel.hytale.server.core.command.commands.world.entity;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EntityWrappedArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityNameplateCommand extends AbstractWorldCommand {
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
   private static final Message MESSAGE_COMMANDS_ENTITY_NAMEPLATE_UPDATED = Message.translation("server.commands.entity.nameplate.updated");
   @Nonnull
   private final EntityWrappedArg entityArg;
   @Nonnull
   private final RequiredArg<String> textArg;

   public EntityNameplateCommand() {
      super("nameplate", "server.commands.entity.nameplate.desc");
      this.entityArg = (EntityWrappedArg)this.withOptionalArg("entity", "server.commands.entity.nameplate.entity.desc", ArgTypes.ENTITY_ID);
      this.textArg = this.withRequiredArg("text", "server.commands.entity.nameplate.text.desc", ArgTypes.STRING);
      this.addUsageVariant(new Remove());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Ref<EntityStore> entityRef = this.entityArg.get(store, context);
      if (entityRef != null && entityRef.isValid()) {
         String text = (String)this.textArg.get(context);
         ((Nameplate)store.ensureAndGetComponent(entityRef, Nameplate.getComponentType())).setText(text);
         context.sendMessage(MESSAGE_COMMANDS_ENTITY_NAMEPLATE_UPDATED);
      } else {
         context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
      }
   }

   public static class Remove extends AbstractWorldCommand {
      @Nonnull
      private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");
      @Nonnull
      private static final Message MESSAGE_COMMANDS_ENTITY_NAMEPLATE_REMOVED = Message.translation("server.commands.entity.nameplate.removed");
      @Nonnull
      private final EntityWrappedArg entityArg;

      public Remove() {
         super("server.commands.entity.nameplate.remove.desc");
         this.entityArg = (EntityWrappedArg)this.withOptionalArg("entity", "server.commands.entity.nameplate.entity.desc", ArgTypes.ENTITY_ID);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Ref<EntityStore> entityRef = this.entityArg.get(store, context);
         if (entityRef != null && entityRef.isValid()) {
            store.tryRemoveComponent(entityRef, Nameplate.getComponentType());
            context.sendMessage(MESSAGE_COMMANDS_ENTITY_NAMEPLATE_REMOVED);
         } else {
            context.sendMessage(MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
         }
      }
   }
}
