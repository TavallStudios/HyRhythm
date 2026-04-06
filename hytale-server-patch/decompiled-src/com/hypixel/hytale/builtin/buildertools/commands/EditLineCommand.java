package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.buildertools.BrushOrigin;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeVector3i;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EditLineCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<RelativeVector3i> startArg;
   @Nonnull
   private final RequiredArg<RelativeVector3i> endArg;
   @Nonnull
   private final RequiredArg<String> materialArg;
   @Nonnull
   private final DefaultArg<Integer> widthArg;
   @Nonnull
   private final DefaultArg<Integer> heightArg;
   @Nonnull
   private final DefaultArg<Integer> wallThicknessArg;
   @Nonnull
   private final DefaultArg<String> shapeArg;
   @Nonnull
   private final DefaultArg<String> originArg;
   @Nonnull
   private final DefaultArg<Integer> spacingArg;
   @Nonnull
   private final DefaultArg<Integer> densityArg;

   public EditLineCommand() {
      super("editline", "server.commands.editline.desc");
      this.startArg = this.withRequiredArg("start", "server.commands.editline.start.desc", ArgTypes.RELATIVE_VECTOR3I);
      this.endArg = this.withRequiredArg("end", "server.commands.editline.end.desc", ArgTypes.RELATIVE_VECTOR3I);
      this.materialArg = this.withRequiredArg("material", "server.commands.editline.material.desc", ArgTypes.STRING);
      this.widthArg = this.withDefaultArg("width", "server.commands.editline.width.desc", ArgTypes.INTEGER, 1, "1");
      this.heightArg = this.withDefaultArg("height", "server.commands.editline.height.desc", ArgTypes.INTEGER, 1, "1");
      this.wallThicknessArg = this.withDefaultArg("wallThickness", "server.commands.editline.wallThickness.desc", ArgTypes.INTEGER, 0, "0");
      this.shapeArg = this.withDefaultArg("shape", "server.commands.editline.shape.desc", ArgTypes.STRING, "Cube", "Cube");
      this.originArg = this.withDefaultArg("origin", "server.commands.editline.origin.desc", ArgTypes.STRING, "Center", "Center");
      this.spacingArg = this.withDefaultArg("spacing", "server.commands.editline.spacing.desc", ArgTypes.INTEGER, 1, "1");
      this.densityArg = this.withDefaultArg("density", "server.commands.editline.density.desc", ArgTypes.INTEGER, 100, "100");
      this.setPermissionGroup(GameMode.Creative);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d playerPos = transformComponent.getPosition();
      int baseX = MathUtil.floor(playerPos.getX());
      int baseY = MathUtil.floor(playerPos.getY());
      int baseZ = MathUtil.floor(playerPos.getZ());
      Vector3i start = ((RelativeVector3i)this.startArg.get(context)).resolve(baseX, baseY, baseZ);
      Vector3i end = ((RelativeVector3i)this.endArg.get(context)).resolve(baseX, baseY, baseZ);
      BlockPattern material = BlockPattern.parse((String)this.materialArg.get(context));
      int width = (Integer)this.widthArg.get(context);
      int height = (Integer)this.heightArg.get(context);
      int wallThickness = (Integer)this.wallThicknessArg.get(context);
      BrushShape shape = BrushShape.valueOf((String)this.shapeArg.get(context));
      BrushOrigin origin = BrushOrigin.valueOf((String)this.originArg.get(context));
      int spacing = (Integer)this.spacingArg.get(context);
      int density = (Integer)this.densityArg.get(context);
      BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.editLine(start.x, start.y, start.z, end.x, end.y, end.z, material, width, height, wallThickness, shape, origin, spacing, density, s.getGlobalMask(), componentAccessor));
   }
}
