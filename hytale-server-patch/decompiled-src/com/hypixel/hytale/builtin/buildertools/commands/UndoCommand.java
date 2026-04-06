package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class UndoCommand extends AbstractPlayerCommand {
   public UndoCommand() {
      super("undo", "server.commands.undo.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.history");
      this.addAliases(new String[]{"u"});
      this.addUsageVariant(new UndoWithCountCommand());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      executeUndo(store, ref, 1);
   }

   private static void executeUndo(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, int count) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.addToQueue(playerComponent, playerRefComponent, (r, s, c) -> s.undo(r, count, c));
   }

   private static class UndoWithCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> countArg;

      public UndoWithCountCommand() {
         super("server.commands.undo.desc");
         this.countArg = this.withRequiredArg("count", "server.commands.undo.count.desc", ArgTypes.INTEGER);
         this.setPermissionGroup(GameMode.Creative);
         this.requirePermission("hytale.editor.history");
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         UndoCommand.executeUndo(store, ref, (Integer)this.countArg.get(context));
      }
   }
}
