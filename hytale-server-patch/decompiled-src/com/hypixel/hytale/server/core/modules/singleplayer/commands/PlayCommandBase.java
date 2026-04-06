package com.hypixel.hytale.server.core.modules.singleplayer.commands;

import com.hypixel.hytale.protocol.packets.serveraccess.Access;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import javax.annotation.Nonnull;

public abstract class PlayCommandBase extends CommandBase {
   @Nonnull
   private final SingleplayerModule singleplayerModule;
   @Nonnull
   private final Access commandAccess;
   @Nonnull
   private final OptionalArg<Boolean> enabledArg;

   protected PlayCommandBase(@Nonnull String name, @Nonnull String description, @Nonnull SingleplayerModule singleplayerModule, @Nonnull Access commandAccess) {
      super(name, description);
      this.enabledArg = this.withOptionalArg("enabled", "server.commands.play.enabled.desc", ArgTypes.BOOLEAN);
      this.singleplayerModule = singleplayerModule;
      this.commandAccess = commandAccess;
   }

   protected void executeSync(@Nonnull CommandContext context) {
      if (Options.getOptionSet().valueOf(Options.AUTH_MODE) == Options.AuthMode.OFFLINE) {
         context.sendMessage(Message.translation("server.commands.play.offlineMode"));
      } else if (!Constants.SINGLEPLAYER) {
         switch (this.commandAccess) {
            case Private -> context.sendMessage(Message.translation("server.commands.play.singleplayerOnlyPrivate"));
            case LAN -> context.sendMessage(Message.translation("server.commands.play.singleplayerOnlyLan"));
            case Friend -> context.sendMessage(Message.translation("server.commands.play.singleplayerOnlyFriend"));
            case Open -> context.sendMessage(Message.translation("server.commands.play.singleplayerOnlyOpen"));
         }

      } else {
         Access access = SingleplayerModule.get().getAccess();
         if (!this.enabledArg.provided(context)) {
            if (access == this.commandAccess) {
               this.singleplayerModule.requestServerAccess(Access.Private);
               switch (this.commandAccess) {
                  case Private -> context.sendMessage(Message.translation("server.commands.play.accessDisabledPrivate"));
                  case LAN -> context.sendMessage(Message.translation("server.commands.play.accessDisabledLan"));
                  case Friend -> context.sendMessage(Message.translation("server.commands.play.accessDisabledFriend"));
                  case Open -> context.sendMessage(Message.translation("server.commands.play.accessDisabledOpen"));
               }
            } else {
               this.singleplayerModule.requestServerAccess(this.commandAccess);
               switch (this.commandAccess) {
                  case Private -> context.sendMessage(Message.translation("server.commands.play.accessEnabledPrivate"));
                  case LAN -> context.sendMessage(Message.translation("server.commands.play.accessEnabledLan"));
                  case Friend -> context.sendMessage(Message.translation("server.commands.play.accessEnabledFriend"));
                  case Open -> context.sendMessage(Message.translation("server.commands.play.accessEnabledOpen"));
               }
            }

         } else {
            Boolean enabled = (Boolean)this.enabledArg.get(context);
            if (!enabled && access == this.commandAccess) {
               this.singleplayerModule.requestServerAccess(Access.Private);
               switch (this.commandAccess) {
                  case Private -> context.sendMessage(Message.translation("server.commands.play.accessDisabledPrivate"));
                  case LAN -> context.sendMessage(Message.translation("server.commands.play.accessDisabledLan"));
                  case Friend -> context.sendMessage(Message.translation("server.commands.play.accessDisabledFriend"));
                  case Open -> context.sendMessage(Message.translation("server.commands.play.accessDisabledOpen"));
               }
            } else if (enabled && access != this.commandAccess) {
               this.singleplayerModule.requestServerAccess(this.commandAccess);
               switch (this.commandAccess) {
                  case Private -> context.sendMessage(Message.translation("server.commands.play.accessEnabledPrivate"));
                  case LAN -> context.sendMessage(Message.translation("server.commands.play.accessEnabledLan"));
                  case Friend -> context.sendMessage(Message.translation("server.commands.play.accessEnabledFriend"));
                  case Open -> context.sendMessage(Message.translation("server.commands.play.accessEnabledOpen"));
               }
            } else {
               switch (this.commandAccess) {
                  case Private -> context.sendMessage(Message.translation("server.commands.play.accessAlreadyToggledPrivate").param("enabled", enabled.toString()));
                  case LAN -> context.sendMessage(Message.translation("server.commands.play.accessAlreadyToggledLan").param("enabled", enabled.toString()));
                  case Friend -> context.sendMessage(Message.translation("server.commands.play.accessAlreadyToggledFriend").param("enabled", enabled.toString()));
                  case Open -> context.sendMessage(Message.translation("server.commands.play.accessAlreadyToggledOpen").param("enabled", enabled.toString()));
               }
            }

         }
      }
   }
}
