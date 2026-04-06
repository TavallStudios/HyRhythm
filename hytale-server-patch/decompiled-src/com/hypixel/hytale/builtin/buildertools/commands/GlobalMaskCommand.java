package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class GlobalMaskCommand extends AbstractPlayerCommand {
   public GlobalMaskCommand() {
      super("gmask", "server.commands.globalmask.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new GlobalMaskSetCommand());
      this.addSubCommand(new GlobalMaskClearCommand());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> {
         BlockMask currentMask = s.getGlobalMask();
         if (currentMask == null) {
            context.sendMessage(Message.translation("server.builderTools.globalmask.current.none"));
         } else {
            context.sendMessage(Message.translation("server.builderTools.globalmask.current").param("mask", currentMask.informativeToString()));
         }

      });
   }

   private static class GlobalMaskSetCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<BlockMask> maskArg;

      public GlobalMaskSetCommand() {
         super("server.commands.globalmask.desc");
         this.maskArg = this.withRequiredArg("mask", "server.commands.globalmask.mask.desc", ArgTypes.BLOCK_MASK);
         this.setPermissionGroup(GameMode.Creative);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BlockMask mask = (BlockMask)this.maskArg.get(context);

         try {
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.setGlobalMask(mask, componentAccessor));
         } catch (IllegalArgumentException e) {
            context.sendMessage(Message.translation("server.builderTools.globalmask.setFailed").param("reason", e.getMessage()));
         }

      }
   }

   private static class GlobalMaskClearCommand extends AbstractPlayerCommand {
      public GlobalMaskClearCommand() {
         super("clear", "server.commands.globalmask.clear.desc");
         this.setPermissionGroup(GameMode.Creative);
         this.addAliases(new String[]{"disable", "c"});
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.setGlobalMask((BlockMask)null, componentAccessor));
      }
   }
}
