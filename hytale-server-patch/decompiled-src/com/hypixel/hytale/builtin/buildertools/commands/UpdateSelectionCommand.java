package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
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

public class UpdateSelectionCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<Integer> xMinArg;
   @Nonnull
   private final RequiredArg<Integer> yMinArg;
   @Nonnull
   private final RequiredArg<Integer> zMinArg;
   @Nonnull
   private final RequiredArg<Integer> xMaxArg;
   @Nonnull
   private final RequiredArg<Integer> yMaxArg;
   @Nonnull
   private final RequiredArg<Integer> zMaxArg;

   public UpdateSelectionCommand() {
      super("updateselection", "server.commands.updateselection.desc");
      this.xMinArg = this.withRequiredArg("xMin", "server.commands.updateselection.xMin.desc", ArgTypes.INTEGER);
      this.yMinArg = this.withRequiredArg("yMin", "server.commands.updateselection.yMin.desc", ArgTypes.INTEGER);
      this.zMinArg = this.withRequiredArg("zMin", "server.commands.updateselection.zMin.desc", ArgTypes.INTEGER);
      this.xMaxArg = this.withRequiredArg("xMax", "server.commands.updateselection.xMax.desc", ArgTypes.INTEGER);
      this.yMaxArg = this.withRequiredArg("yMax", "server.commands.updateselection.yMax.desc", ArgTypes.INTEGER);
      this.zMaxArg = this.withRequiredArg("zMax", "server.commands.updateselection.zMax.desc", ArgTypes.INTEGER);
      this.setPermissionGroup(GameMode.Creative);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         int xMin = (Integer)this.xMinArg.get(context);
         int yMin = (Integer)this.yMinArg.get(context);
         int zMin = (Integer)this.zMinArg.get(context);
         int xMax = (Integer)this.xMaxArg.get(context);
         int yMax = (Integer)this.yMaxArg.get(context);
         int zMax = (Integer)this.zMaxArg.get(context);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.update(xMin, yMin, zMin, xMax, yMax, zMax));
      }
   }
}
