package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ExtendFaceCommand extends AbstractCommandCollection {
   public ExtendFaceCommand() {
      super("extendface", "server.commands.extendface.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new ExtendFaceBasicCommand());
      this.addUsageVariant(new ExtendFaceWithRegionCommand());
   }

   private static class ExtendFaceBasicCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> xArg;
      @Nonnull
      private final RequiredArg<Integer> yArg;
      @Nonnull
      private final RequiredArg<Integer> zArg;
      @Nonnull
      private final RequiredArg<Integer> normalXArg;
      @Nonnull
      private final RequiredArg<Integer> normalYArg;
      @Nonnull
      private final RequiredArg<Integer> normalZArg;
      @Nonnull
      private final RequiredArg<Integer> toolParamArg;
      @Nonnull
      private final RequiredArg<Integer> shapeRangeArg;
      @Nonnull
      private final RequiredArg<String> blockTypeArg;

      public ExtendFaceBasicCommand() {
         super("server.commands.extendface.desc");
         this.xArg = this.withRequiredArg("x", "server.commands.extendface.x.desc", ArgTypes.INTEGER);
         this.yArg = this.withRequiredArg("y", "server.commands.extendface.y.desc", ArgTypes.INTEGER);
         this.zArg = this.withRequiredArg("z", "server.commands.extendface.z.desc", ArgTypes.INTEGER);
         this.normalXArg = this.withRequiredArg("normalX", "server.commands.extendface.normalX.desc", ArgTypes.INTEGER);
         this.normalYArg = this.withRequiredArg("normalY", "server.commands.extendface.normalY.desc", ArgTypes.INTEGER);
         this.normalZArg = this.withRequiredArg("normalZ", "server.commands.extendface.normalZ.desc", ArgTypes.INTEGER);
         this.toolParamArg = this.withRequiredArg("toolParam", "server.commands.extendface.toolParam.desc", ArgTypes.INTEGER);
         this.shapeRangeArg = this.withRequiredArg("shapeRange", "server.commands.extendface.shapeRange.desc", ArgTypes.INTEGER);
         this.blockTypeArg = this.withRequiredArg("blockType", "server.commands.extendface.blockType.desc", ArgTypes.STRING);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            int x = (Integer)this.xArg.get(context);
            int y = (Integer)this.yArg.get(context);
            int z = (Integer)this.zArg.get(context);
            int normalX = (Integer)this.normalXArg.get(context);
            int normalY = (Integer)this.normalYArg.get(context);
            int normalZ = (Integer)this.normalZArg.get(context);
            int toolParam = (Integer)this.toolParamArg.get(context);
            int shapeRange = (Integer)this.shapeRangeArg.get(context);
            String key = (String)this.blockTypeArg.get(context);
            if (BlockType.getAssetMap().getAsset(key) == null) {
               context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
            } else {
               int index = BlockType.getAssetMap().getIndex(key);
               if (index == -2147483648) {
                  context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
               } else {
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.extendFace(x, y, z, normalX, normalY, normalZ, toolParam, shapeRange, index, (Vector3i)null, (Vector3i)null, componentAccessor));
               }
            }
         }
      }
   }

   private static class ExtendFaceWithRegionCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> xArg;
      @Nonnull
      private final RequiredArg<Integer> yArg;
      @Nonnull
      private final RequiredArg<Integer> zArg;
      @Nonnull
      private final RequiredArg<Integer> normalXArg;
      @Nonnull
      private final RequiredArg<Integer> normalYArg;
      @Nonnull
      private final RequiredArg<Integer> normalZArg;
      @Nonnull
      private final RequiredArg<Integer> toolParamArg;
      @Nonnull
      private final RequiredArg<Integer> shapeRangeArg;
      @Nonnull
      private final RequiredArg<String> blockTypeArg;
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

      public ExtendFaceWithRegionCommand() {
         super("server.commands.extendface.desc");
         this.xArg = this.withRequiredArg("x", "server.commands.extendface.x.desc", ArgTypes.INTEGER);
         this.yArg = this.withRequiredArg("y", "server.commands.extendface.y.desc", ArgTypes.INTEGER);
         this.zArg = this.withRequiredArg("z", "server.commands.extendface.z.desc", ArgTypes.INTEGER);
         this.normalXArg = this.withRequiredArg("normalX", "server.commands.extendface.normalX.desc", ArgTypes.INTEGER);
         this.normalYArg = this.withRequiredArg("normalY", "server.commands.extendface.normalY.desc", ArgTypes.INTEGER);
         this.normalZArg = this.withRequiredArg("normalZ", "server.commands.extendface.normalZ.desc", ArgTypes.INTEGER);
         this.toolParamArg = this.withRequiredArg("toolParam", "server.commands.extendface.toolParam.desc", ArgTypes.INTEGER);
         this.shapeRangeArg = this.withRequiredArg("shapeRange", "server.commands.extendface.shapeRange.desc", ArgTypes.INTEGER);
         this.blockTypeArg = this.withRequiredArg("blockType", "server.commands.extendface.blockType.desc", ArgTypes.STRING);
         this.xMinArg = this.withRequiredArg("xMin", "server.commands.extendface.xMin.desc", ArgTypes.INTEGER);
         this.yMinArg = this.withRequiredArg("yMin", "server.commands.extendface.yMin.desc", ArgTypes.INTEGER);
         this.zMinArg = this.withRequiredArg("zMin", "server.commands.extendface.zMin.desc", ArgTypes.INTEGER);
         this.xMaxArg = this.withRequiredArg("xMax", "server.commands.extendface.xMax.desc", ArgTypes.INTEGER);
         this.yMaxArg = this.withRequiredArg("yMax", "server.commands.extendface.yMax.desc", ArgTypes.INTEGER);
         this.zMaxArg = this.withRequiredArg("zMax", "server.commands.extendface.zMax.desc", ArgTypes.INTEGER);
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            int x = (Integer)this.xArg.get(context);
            int y = (Integer)this.yArg.get(context);
            int z = (Integer)this.zArg.get(context);
            int normalX = (Integer)this.normalXArg.get(context);
            int normalY = (Integer)this.normalYArg.get(context);
            int normalZ = (Integer)this.normalZArg.get(context);
            int toolParam = (Integer)this.toolParamArg.get(context);
            int shapeRange = (Integer)this.shapeRangeArg.get(context);
            String key = (String)this.blockTypeArg.get(context);
            if (BlockType.getAssetMap().getAsset(key) == null) {
               context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
            } else {
               int index = BlockType.getAssetMap().getIndex(key);
               if (index == -2147483648) {
                  context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", key).param("key", key));
               } else {
                  int xMin = (Integer)this.xMinArg.get(context);
                  int yMin = (Integer)this.yMinArg.get(context);
                  int zMin = (Integer)this.zMinArg.get(context);
                  int xMax = (Integer)this.xMaxArg.get(context);
                  int yMax = (Integer)this.yMaxArg.get(context);
                  int zMax = (Integer)this.zMaxArg.get(context);
                  Vector3i min = new Vector3i(xMin, yMin, zMin);
                  Vector3i max = new Vector3i(xMax, yMax, zMax);
                  BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.extendFace(x, y, z, normalX, normalY, normalZ, toolParam, shapeRange, index, min, max, componentAccessor));
               }
            }
         }
      }
   }
}
