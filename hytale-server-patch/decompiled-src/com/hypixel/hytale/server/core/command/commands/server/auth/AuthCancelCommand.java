package com.hypixel.hytale.server.core.command.commands.server.auth;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import javax.annotation.Nonnull;

public class AuthCancelCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_SINGLEPLAYER;
   @Nonnull
   private static final Message MESSAGE_SUCCESS;
   @Nonnull
   private static final Message MESSAGE_NOTHING;

   public AuthCancelCommand() {
      super("cancel", "server.commands.auth.cancel.desc");
   }

   protected void executeSync(@Nonnull CommandContext context) {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (authManager.isSingleplayer()) {
         context.sendMessage(MESSAGE_SINGLEPLAYER);
      } else {
         if (authManager.cancelActiveFlow()) {
            context.sendMessage(MESSAGE_SUCCESS);
         } else {
            context.sendMessage(MESSAGE_NOTHING);
         }

      }
   }

   static {
      MESSAGE_SINGLEPLAYER = Message.translation("server.commands.auth.cancel.singleplayer").color(Color.RED);
      MESSAGE_SUCCESS = Message.translation("server.commands.auth.cancel.success").color(Color.YELLOW);
      MESSAGE_NOTHING = Message.translation("server.commands.auth.cancel.nothing").color(Color.YELLOW);
   }
}
