package com.hypixel.hytale.server.core.command.commands.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class GameModeCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_GAMEMODE_ALREADY_IN_MODE_SELF = Message.translation("server.commands.gamemode.alreadyInMode.self");
   @Nonnull
   private final RequiredArg<GameMode> gameModeArg;

   public GameModeCommand() {
      super("gamemode", "server.commands.gamemode.desc");
      this.gameModeArg = this.withRequiredArg("gamemode", "server.commands.gamemode.gamemode.desc", ArgTypes.GAME_MODE);
      this.addAliases(new String[]{"gm"});
      this.requirePermission(HytalePermissions.fromCommand("gamemode.self"));
      this.addUsageVariant(new GameModeOtherCommand());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      GameMode gameMode = (GameMode)this.gameModeArg.get(context);
      if (playerComponent.getGameMode() == gameMode) {
         context.sendMessage(MESSAGE_COMMANDS_GAMEMODE_ALREADY_IN_MODE_SELF);
      } else {
         Player.setGameMode(ref, gameMode, store);
         Message gameModeMessage = Message.translation("server.general.gamemodes." + gameMode.name().toLowerCase());
         context.sendMessage(Message.translation("server.commands.gamemode.success.self").param("mode", gameModeMessage));
      }
   }

   private static class GameModeOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<GameMode> gameModeArg;
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg;

      GameModeOtherCommand() {
         super("server.commands.gamemode.other.desc");
         this.gameModeArg = this.withRequiredArg("gamemode", "server.commands.gamemode.gamemode.desc", ArgTypes.GAME_MODE);
         this.playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
         this.requirePermission(HytalePermissions.fromCommand("gamemode.other"));
      }

      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = (PlayerRef)this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            world.execute(() -> {
               Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());
               if (playerComponent == null) {
                  context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
               } else {
                  PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  GameMode gameMode = (GameMode)this.gameModeArg.get(context);
                  if (playerComponent.getGameMode() == gameMode) {
                     context.sendMessage(Message.translation("server.commands.gamemode.alreadyInMode.other").param("username", playerRefComponent.getUsername()));
                  } else {
                     Player.setGameMode(ref, gameMode, store);
                     Message gameModeMessage = Message.translation("server.general.gamemodes." + gameMode.name().toLowerCase());
                     context.sendMessage(Message.translation("server.commands.gamemode.success.other").param("mode", gameModeMessage).param("username", playerRefComponent.getUsername()));
                  }
               }
            });
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
