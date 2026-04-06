package com.hypixel.hytale.builtin.blockphysics;

import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.ValidationOption;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabBufferValidator {
   @Nonnull
   private static final FillerBlockUtil.FillerFetcher<IPrefabBuffer, Void> FILLER_FETCHER = new FillerBlockUtil.FillerFetcher<IPrefabBuffer, Void>() {
      public int getBlock(@Nonnull IPrefabBuffer iPrefabBuffer, Void unused, int x, int y, int z) {
         return iPrefabBuffer.getBlockId(x, y, z);
      }

      public int getFiller(@Nonnull IPrefabBuffer iPrefabBuffer, Void unused, int x, int y, int z) {
         return iPrefabBuffer.getFiller(x, y, z);
      }

      public int getRotationIndex(@Nonnull IPrefabBuffer iPrefabBuffer, Void unused, int x, int y, int z) {
         return iPrefabBuffer.getRotationIndex(x, y, z);
      }
   };

   @Nonnull
   public static List<String> validateAllPrefabs(@Nonnull List<ValidationOption> list) {
      Set<ValidationOption> options = !list.isEmpty() ? EnumSet.copyOf(list) : EnumSet.of(ValidationOption.BLOCK_STATES, ValidationOption.ENTITIES, ValidationOption.BLOCKS, ValidationOption.BLOCK_FILLER);
      List<String> out = validatePrefabsInPath(PrefabStore.get().getWorldGenPrefabsPath(), options);
      out.addAll(validatePrefabsInPath(PrefabStore.get().getAssetPrefabsPath(), options));
      out.addAll(validatePrefabsInPath(PrefabStore.get().getServerPrefabsPath(), options));
      return out;
   }

   @Nonnull
   public static List<String> validatePrefabsInPath(@Nonnull Path dataFolder, @Nonnull Set<ValidationOption> options) {
      if (!Files.exists(dataFolder, new LinkOption[0])) {
         return new ArrayList();
      } else {
         try {
            Stream<Path> stream = Files.walk(dataFolder, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY);

            List var3;
            try {
               var3 = (List)stream.map((path) -> {
                  if (Files.isRegularFile(path, new LinkOption[0]) && path.toString().endsWith(".prefab.json")) {
                     try {
                        IPrefabBuffer prefab = PrefabBufferUtil.getCached(path);

                        String var4;
                        try {
                           String results = validate(prefab, options);
                           var4 = results != null ? String.valueOf(path) + "\n" + results : null;
                        } finally {
                           prefab.release();
                        }

                        return var4;
                     } catch (Throwable e) {
                        String var10000 = String.valueOf(path);
                        return var10000 + "\n\t" + ExceptionUtil.combineMessages(e, "\n\t");
                     }
                  } else {
                     return null;
                  }
               }).filter(Objects::nonNull).collect(Collectors.toList());
            } catch (Throwable var6) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stream != null) {
               stream.close();
            }

            return var3;
         } catch (IOException e) {
            throw SneakyThrow.sneakyThrow(e);
         }
      }
   }

   @Nullable
   public static String validate(@Nonnull IPrefabBuffer prefab, @Nonnull Set<ValidationOption> options) {
      ComponentType<EntityStore, UnknownComponents<EntityStore>> unknownComponentType = EntityStore.REGISTRY.getUnknownComponentType();
      StringBuilder sb = new StringBuilder();
      int offsetX = prefab.getAnchorX();
      int offsetY = prefab.getAnchorY();
      int offsetZ = prefab.getAnchorZ();
      IPrefabBuffer.RawBlockConsumer<Void> legacyValidator = WorldValidationUtil.blockValidator(offsetX, offsetY, offsetZ, sb, options);
      prefab.forEachRaw(IPrefabBuffer.iterateAllColumns(), (IPrefabBuffer.RawBlockConsumer)((x, y, z, mask, blockId, chance, holder, supportValue, rotation, filler, o) -> {
         legacyValidator.accept(x, y, z, mask, blockId, chance, holder, supportValue, rotation, filler, o);
         IEventDispatcher<ValidateBlockEvent, ValidateBlockEvent> dispatch = HytaleServer.get().getEventBus().dispatchFor(ValidateBlockEvent.class);
         if (dispatch.hasListener()) {
            dispatch.dispatch(new ValidateBlockEvent(x, y, z, blockId, supportValue, rotation, filler, holder, sb));
         }

         if (options.contains(ValidationOption.BLOCK_FILLER)) {
            FillerBlockUtil.ValidationResult fillerResult = FillerBlockUtil.validateBlock(x, y, z, blockId, rotation, filler, prefab, (Object)null, FILLER_FETCHER);
            switch (fillerResult) {
               case OK:
               default:
                  break;
               case INVALID_BLOCK:
                  BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(blockId);
                  sb.append("\tBlock ").append(blockType != null ? blockType.getId() : "<missing>").append(" at ").append(x).append(", ").append(y).append(", ").append(z).append(" is not valid filler").append('\n');
                  break;
               case INVALID_FILLER:
                  BlockType blockType = (BlockType)BlockType.getAssetMap().getAsset(blockId);
                  sb.append("\tBlock ").append(blockType != null ? blockType.getId() : "<missing>").append(" at ").append(x).append(", ").append(y).append(", ").append(z).append(" has invalid/missing filler blocks").append('\n');
            }
         }

      }), (IPrefabBuffer.FluidConsumer)((x, y, z, fluidId, level, unused) -> {
      }), (IPrefabBuffer.EntityConsumer)((x, z, holders, o) -> {
         if (holders != null) {
            if (options.contains(ValidationOption.ENTITIES)) {
               for(Holder<EntityStore> entityHolder : holders) {
                  UnknownComponents<EntityStore> unknownComponents = (UnknownComponents)entityHolder.getComponent(unknownComponentType);
                  if (unknownComponents != null && !unknownComponents.getUnknownComponents().isEmpty()) {
                     sb.append("\tUnknown Entity Components: ").append(unknownComponents.getUnknownComponents()).append("\n");
                  }
               }
            }

         }
      }), (Void)null);
      return !sb.isEmpty() ? sb.toString() : null;
   }

   public static record ValidateBlockEvent(int x, int y, int z, int blockId, int support, int rotation, int filler, @Nullable Holder<ChunkStore> holder, StringBuilder reason) implements IEvent<Void> {
   }
}
