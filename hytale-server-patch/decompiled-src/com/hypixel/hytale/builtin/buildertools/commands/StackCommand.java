package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDirection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StackCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
   @Nonnull
   private final OptionalArg<Integer> spacingArg;

   public StackCommand() {
      super("stack", "server.commands.stack.desc");
      this.spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new StackWithCountCommand());
      this.addUsageVariant(new StackWithDirectionAndCountCommand());
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      executeStack(store, ref, (RelativeDirection)null, 1, (Boolean)this.emptyFlag.get(context), this.spacingArg.provided(context) ? (Integer)this.spacingArg.get(context) : 0);
   }

   private static void executeStack(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nullable RelativeDirection direction, int count, boolean empty, int spacing) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         HeadRotation headRotationComponent = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3i directionVector = RelativeDirection.toDirectionVector(direction, headRotationComponent);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRefComponent, (r, s, componentAccessor) -> s.stack(r, directionVector, count, empty, spacing, componentAccessor));
      }
   }

   private static class StackWithCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> countArg;
      @Nonnull
      private final FlagArg emptyFlag;
      @Nonnull
      private final OptionalArg<Integer> spacingArg;

      public StackWithCountCommand() {
         super("server.commands.stack.desc");
         this.countArg = this.withRequiredArg("count", "server.commands.stack.count.desc", ArgTypes.INTEGER);
         this.emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
         this.spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);
         this.setPermissionGroup(GameMode.Creative);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         StackCommand.executeStack(store, ref, (RelativeDirection)null, (Integer)this.countArg.get(context), (Boolean)this.emptyFlag.get(context), this.spacingArg.provided(context) ? (Integer)this.spacingArg.get(context) : 0);
      }
   }

   private static class StackWithDirectionAndCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<RelativeDirection> directionArg;
      @Nonnull
      private final RequiredArg<Integer> countArg;
      @Nonnull
      private final FlagArg emptyFlag;
      @Nonnull
      private final OptionalArg<Integer> spacingArg;

      public StackWithDirectionAndCountCommand() {
         super("server.commands.stack.desc");
         this.directionArg = this.withRequiredArg("direction", "server.commands.stack.direction.desc", RelativeDirection.ARGUMENT_TYPE);
         this.countArg = this.withRequiredArg("count", "server.commands.stack.count.desc", ArgTypes.INTEGER);
         this.emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
         this.spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);
         this.setPermissionGroup(GameMode.Creative);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         StackCommand.executeStack(store, ref, (RelativeDirection)this.directionArg.get(context), (Integer)this.countArg.get(context), (Boolean)this.emptyFlag.get(context), this.spacingArg.provided(context) ? (Integer)this.spacingArg.get(context) : 0);
      }
   }
}
