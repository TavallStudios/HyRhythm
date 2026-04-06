package com.hypixel.hytale.server.core.universe.world.commands.block;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFlipType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.SelectionManager;
import com.hypixel.hytale.server.core.prefab.selection.SelectionProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class BlockSelectCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final SingleArgumentType<BlockFlipType> BLOCK_FLIP_TYPE = new EnumArgumentType<BlockFlipType>("server.commands.parsing.argtype.blockfliptype.name", BlockFlipType.class);
   @Nonnull
   private static final SingleArgumentType<VariantRotation> VARIANT_ROTATION = new EnumArgumentType<VariantRotation>("server.commands.parsing.argtype.variantrotation.name", VariantRotation.class);
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_SELECT_DONE = Message.translation("server.commands.block.select.done");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BLOCK_SELECT_NO_SELECTION_PROVIDER = Message.translation("server.commands.block.select.noSelectionProvider");
   @Nonnull
   private final OptionalArg<String> regexArg;
   @Nonnull
   private final FlagArg allFlag;
   @Nonnull
   private final OptionalArg<String> sortArg;
   @Nonnull
   private final OptionalArg<BlockFlipType> flipTypeArg;
   @Nonnull
   private final OptionalArg<VariantRotation> variantRotationArg;
   @Nonnull
   private final DefaultArg<Integer> paddingArg;
   @Nonnull
   private final OptionalArg<String> groundArg;

   public BlockSelectCommand() {
      super("blockselect", "server.commands.block.select.desc");
      this.regexArg = this.withOptionalArg("regex", "server.commands.block.select.regex.desc", ArgTypes.STRING);
      this.allFlag = this.withFlagArg("all", "server.commands.block.select.all.desc");
      this.sortArg = this.withOptionalArg("sort", "server.commands.block.select.sort.desc", ArgTypes.STRING);
      this.flipTypeArg = this.withOptionalArg("fliptype", "server.commands.block.select.fliptype.desc", BLOCK_FLIP_TYPE);
      this.variantRotationArg = this.withOptionalArg("variantrotation", "server.commands.block.select.variantrotation.desc", VARIANT_ROTATION);
      this.paddingArg = this.withDefaultArg("padding", "server.commands.block.select.padding.desc", ArgTypes.INTEGER, 1, "1");
      this.groundArg = this.withOptionalArg("ground", "server.commands.block.select.ground.desc", ArgTypes.STRING);
   }

   protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
      Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      SelectionProvider selectionProvider = SelectionManager.getSelectionProvider();
      if (selectionProvider == null) {
         context.sendMessage(MESSAGE_COMMANDS_BLOCK_SELECT_NO_SELECTION_PROVIDER);
      } else {
         Pattern pattern = this.regexArg.provided(context) ? Pattern.compile((String)this.regexArg.get(context)) : null;
         Stream<Map.Entry<String, BlockType>> stream = BlockType.getAssetMap().getAssetMap().entrySet().parallelStream().filter((e) -> !((BlockType)e.getValue()).isUnknown()).filter((e) -> pattern == null || pattern.matcher((CharSequence)e.getKey()).matches());
         if (!(Boolean)this.allFlag.get(context)) {
            stream = stream.filter((e) -> !((BlockType)e.getValue()).isState());
         }

         if (this.flipTypeArg.provided(context)) {
            BlockFlipType flipType = (BlockFlipType)this.flipTypeArg.get(context);
            stream = stream.filter((e) -> ((BlockType)e.getValue()).getFlipType() == flipType);
         }

         if (this.variantRotationArg.provided(context)) {
            VariantRotation variantRotation = (VariantRotation)this.variantRotationArg.get(context);
            stream = stream.filter((e) -> ((BlockType)e.getValue()).getVariantRotation() == variantRotation);
         }

         if (this.sortArg.provided(context)) {
            String sort = (String)this.sortArg.get(context);
            if (sort.isEmpty()) {
               stream = stream.sorted(Entry.comparingByKey());
            } else {
               for(String sortType : sort.split(",")) {
                  Stream var10000;
                  switch (sortType) {
                     case "key" -> var10000 = stream.sorted(Entry.comparingByKey());
                     case "name" -> var10000 = stream.sorted(Entry.comparingByKey());
                     case "reverse" -> var10000 = stream.sorted(Collections.reverseOrder());
                     default -> throw new GeneralCommandException(Message.translation("server.commands.block.select.invalidSortType").param("sortType", sortType));
                  }

                  stream = var10000;
               }
            }
         }

         List<Map.Entry<String, BlockBoundingBoxes>> blocks = stream.map((e) -> Map.entry((String)e.getKey(), (BlockBoundingBoxes)BlockBoundingBoxes.getAssetMap().getAsset(((BlockType)e.getValue()).getHitboxTypeIndex()))).toList();
         context.sendMessage(Message.translation("server.commands.block.select.select").param("count", blocks.size()));
         Box largestBox = new Box();

         for(Map.Entry<String, BlockBoundingBoxes> block : blocks) {
            largestBox.union(((BlockBoundingBoxes)block.getValue()).get(0).getBoundingBox());
         }

         int paddingSize = (Integer)this.paddingArg.get(context);
         int sqrt = MathUtil.ceil(Math.sqrt((double)blocks.size())) + 1;
         int strideX = MathUtil.ceil(largestBox.width()) + paddingSize;
         int strideZ = MathUtil.ceil(largestBox.depth()) + paddingSize;
         int halfStrideX = strideX / 2;
         int halfStrideZ = strideZ / 2;
         double height = largestBox.height();
         String groundBlock;
         if (this.groundArg.provided(context)) {
            groundBlock = (String)this.groundArg.get(context);
         } else {
            String rockStone = "Rock_Stone";
            if (BlockType.getAssetMap().getAsset("Rock_Stone") != null) {
               groundBlock = "Rock_Stone";
            } else {
               groundBlock = "Unknown";
            }
         }

         selectionProvider.computeSelectionCopy(ref, playerComponent, (selection) -> {
            BlockTypeAssetMap<String, BlockType> blockTypeAssetMap = BlockType.getAssetMap();
            int groundId = blockTypeAssetMap.getIndex(groundBlock);

            for(int x = -paddingSize; x < sqrt * strideX; ++x) {
               for(int z = -paddingSize; z < sqrt * strideZ; ++z) {
                  selection.addBlockAtWorldPos(x, 0, z, groundId, 0, 0, 0);

                  for(int y = 1; (double)y < height; ++y) {
                     selection.addBlockAtWorldPos(x, y, z, 0, 0, 0, 0);
                  }
               }
            }

            for(int i = 0; i < blocks.size(); ++i) {
               Map.Entry<String, BlockBoundingBoxes> entry = (Map.Entry)blocks.get(i);
               BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = ((BlockBoundingBoxes)entry.getValue()).get(0);
               Box boundingBox = rotatedBoxes.getBoundingBox();
               int x = i % sqrt * strideX + halfStrideX + MathUtil.floor(boundingBox.middleX());
               int z = i / sqrt * strideZ + halfStrideZ + MathUtil.floor(boundingBox.middleZ());
               int blockId = blockTypeAssetMap.getIndex((String)entry.getKey());
               selection.addBlockAtWorldPos(x, 1, z, blockId, 0, 0, 0);
               if (((BlockBoundingBoxes)entry.getValue()).protrudesUnitBox()) {
                  FillerBlockUtil.forEachFillerBlock(rotatedBoxes, (x1, y1, z1) -> {
                     if (x1 != 0 || y1 != 0 || z1 != 0) {
                        int filler = FillerBlockUtil.pack(x1, y1, z1);
                        selection.addBlockAtWorldPos(x + x1, 1 + y1, z + z1, blockId, 0, filler, 0);
                     }
                  });
               }
            }

            context.sendMessage(MESSAGE_COMMANDS_BLOCK_SELECT_DONE);
         }, store);
      }
   }
}
