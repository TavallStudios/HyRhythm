package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefablist.PrefabPage;
import com.hypixel.hytale.builtin.buildertools.prefablist.PrefabSavePage;
import com.hypixel.hytale.builtin.buildertools.utils.RecursivePrefabLoader;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabCommand extends AbstractCommandCollection {
   public PrefabCommand() {
      super("prefab", "server.commands.prefab.desc");
      this.addAliases(new String[]{"p"});
      this.setPermissionGroup(GameMode.Creative);
      this.addSubCommand(new PrefabSaveCommand());
      this.addSubCommand(new PrefabLoadCommand());
      this.addSubCommand(new PrefabDeleteCommand());
      this.addSubCommand(new PrefabListCommand());
   }

   private static class PrefabSaveCommand extends AbstractPlayerCommand {
      public PrefabSaveCommand() {
         super("save", "server.commands.prefab.save.desc");
         this.requirePermission("hytale.editor.prefab.manage");
         this.addUsageVariant(new PrefabSaveDirectCommand());
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new PrefabSavePage(playerRef));
      }
   }

   private static class PrefabSaveDirectCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> nameArg;
      @Nonnull
      private final FlagArg overwriteFlag;
      @Nonnull
      private final FlagArg entitiesFlag;
      @Nonnull
      private final FlagArg emptyFlag;
      @Nonnull
      private final FlagArg playerAnchorFlag;
      @Nonnull
      private final FlagArg clearSupportFlag;

      public PrefabSaveDirectCommand() {
         super("server.commands.prefab.save.desc");
         this.nameArg = this.withRequiredArg("name", "server.commands.prefab.save.name.desc", ArgTypes.STRING);
         this.overwriteFlag = this.withFlagArg("overwrite", "server.commands.prefab.save.overwrite.desc");
         this.entitiesFlag = this.withFlagArg("entities", "server.commands.prefab.save.entities.desc");
         this.emptyFlag = this.withFlagArg("empty", "server.commands.prefab.save.empty.desc");
         this.playerAnchorFlag = this.withFlagArg("playerAnchor", "server.commands.prefab.save.playerAnchor.desc");
         this.clearSupportFlag = this.withFlagArg("clearSupport", "server.commands.editprefab.save.clearSupport.desc");
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         String name = (String)this.nameArg.get(context);
         boolean overwrite = (Boolean)this.overwriteFlag.get(context);
         boolean entities = (Boolean)this.entitiesFlag.get(context);
         boolean empty = (Boolean)this.emptyFlag.get(context);
         boolean clearSupport = (Boolean)this.clearSupportFlag.get(context);
         Vector3i playerAnchor = this.getPlayerAnchor(ref, store, (Boolean)this.playerAnchorFlag.get(context));
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.saveFromSelection(r, name, true, overwrite, entities, empty, playerAnchor, clearSupport, componentAccessor));
      }

      @Nullable
      private Vector3i getPlayerAnchor(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, boolean usePlayerAnchor) {
         if (!usePlayerAnchor) {
            return null;
         } else {
            TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) {
               return null;
            } else {
               Vector3d position = transformComponent.getPosition();
               return new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ()));
            }
         }
      }
   }

   private static class PrefabLoadCommand extends AbstractPlayerCommand {
      public PrefabLoadCommand() {
         super("load", "server.commands.prefab.load.desc");
         this.requirePermission("hytale.editor.prefab.use");
         this.addUsageVariant(new PrefabLoadByNameCommand());
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         List<PrefabStore.AssetPackPrefabPath> assetPaths = PrefabStore.get().getAllAssetPrefabPaths();
         Path defaultRoot = assetPaths.isEmpty() ? PrefabStore.get().getServerPrefabsPath() : ((PrefabStore.AssetPackPrefabPath)assetPaths.getFirst()).prefabsPath();
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         playerComponent.getPageManager().openCustomPage(ref, store, new PrefabPage(playerRef, defaultRoot, builderState));
      }
   }

   private static class PrefabLoadByNameCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> nameArg;
      @Nonnull
      private final DefaultArg<String> storeTypeArg;
      @Nonnull
      private final DefaultArg<String> storeNameArg;
      @Nonnull
      private final FlagArg childrenFlag;

      public PrefabLoadByNameCommand() {
         super("server.commands.prefab.load.desc");
         this.nameArg = this.withRequiredArg("name", "server.commands.prefab.load.name.desc", ArgTypes.STRING);
         this.storeTypeArg = this.withDefaultArg("storeType", "server.commands.prefab.load.storeType.desc", ArgTypes.STRING, "asset", "server.commands.prefab.load.storeType.desc");
         this.storeNameArg = this.withDefaultArg("storeName", "server.commands.prefab.load.storeName.desc", ArgTypes.STRING, (Object)null, "");
         this.childrenFlag = this.withFlagArg("children", "server.commands.prefab.load.children.desc");
      }

      protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
         Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         String storeType = (String)this.storeTypeArg.get(context);
         String storeName = (String)this.storeNameArg.get(context);
         String name = (String)this.nameArg.get(context);
         if (!name.endsWith(".prefab.json")) {
            name = name + ".prefab.json";
         }

         Path prefabStorePath = null;
         Path resolvedPrefabPath = null;
         Function var10000;
         switch (storeType) {
            case "server":
               prefabStorePath = PrefabStore.get().getServerPrefabsPath();
               PrefabStore var20 = PrefabStore.get();
               Objects.requireNonNull(var20);
               var10000 = var20::getServerPrefab;
               break;
            case "asset":
               Path foundPath = PrefabStore.get().findAssetPrefabPath(name);
               if (foundPath != null) {
                  resolvedPrefabPath = foundPath;
                  prefabStorePath = foundPath.getParent();
                  var10000 = (key) -> PrefabStore.get().getPrefab(foundPath);
               } else {
                  prefabStorePath = PrefabStore.get().getAssetPrefabsPath();
                  PrefabStore var19 = PrefabStore.get();
                  Objects.requireNonNull(var19);
                  var10000 = var19::getAssetPrefab;
               }
               break;
            case "worldgen":
               Path storePath = PrefabStore.get().getWorldGenPrefabsPath(storeName);
               prefabStorePath = PrefabStore.get().getWorldGenPrefabsPath(storeName);
               var10000 = (key) -> PrefabStore.get().getWorldGenPrefab(storePath, key);
               break;
            default:
               context.sendMessage(Message.translation("server.commands.prefab.invalidStoreType").param("storeType", storeType));
               var10000 = null;
         }

         Function<String, BlockSelection> prefabGetter = var10000;
         if (prefabGetter != null) {
            BiFunction<String, Random, BlockSelection> loader;
            if ((Boolean)this.childrenFlag.get(context)) {
               loader = new RecursivePrefabLoader.BlockSelectionLoader(prefabStorePath, prefabGetter);
            } else {
               loader = (prefabFile, rand) -> (BlockSelection)prefabGetter.apply(prefabFile);
            }

            boolean prefabExists = resolvedPrefabPath != null && Files.isRegularFile(resolvedPrefabPath, new LinkOption[0]) || Files.isRegularFile(prefabStorePath.resolve(name), new LinkOption[0]);
            if (prefabExists) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.load(name, (BlockSelection)loader.apply(name, s.getRandom()), componentAccessor));
            } else {
               context.sendMessage(Message.translation("server.builderTools.prefab.prefabNotFound").param("name", name));
            }

         }
      }
   }

   private static class PrefabDeleteCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> nameArg;

      public PrefabDeleteCommand() {
         super("delete", "server.commands.prefab.delete.desc", true);
         this.nameArg = this.withRequiredArg("name", "server.commands.prefab.delete.name.desc", ArgTypes.STRING);
         this.requirePermission("hytale.editor.prefab.manage");
      }

      protected void executeSync(@Nonnull CommandContext context) {
         String name = (String)this.nameArg.get(context);
         if (!name.endsWith(".prefab.json")) {
            name = name + ".prefab.json";
         }

         PrefabStore module = PrefabStore.get();
         Path serverPrefabsPath = module.getServerPrefabsPath();
         Path resolve = serverPrefabsPath.resolve(name);

         try {
            Ref<EntityStore> ref = context.senderAsPlayerRef();
            boolean isOwner = false;
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());
               if (playerRefComponent != null) {
                  isOwner = SingleplayerModule.isOwner(playerRefComponent);
               }
            }

            if (!PathUtil.isChildOf(serverPrefabsPath, resolve) && !isOwner) {
               context.sendMessage(Message.translation("server.builderTools.attemptedToSaveOutsidePrefabsDir"));
               return;
            }

            Path relativize = PathUtil.relativize(serverPrefabsPath, resolve);
            if (Files.isRegularFile(resolve, new LinkOption[0])) {
               Files.delete(resolve);
               context.sendMessage(Message.translation("server.builderTools.prefab.deleted").param("name", relativize.toString()));
            } else {
               context.sendMessage(Message.translation("server.builderTools.prefab.prefabNotFound").param("name", relativize.toString()));
            }
         } catch (IOException e) {
            context.sendMessage(Message.translation("server.builderTools.prefab.errorOccured").param("reason", e.getMessage()));
         }

      }
   }

   private static class PrefabListCommand extends CommandBase {
      @Nonnull
      private final DefaultArg<String> storeTypeArg;
      @Nonnull
      private final FlagArg textFlag;

      public PrefabListCommand() {
         super("list", "server.commands.prefab.list.desc");
         this.storeTypeArg = this.withDefaultArg("storeType", "server.commands.prefab.list.storeType.desc", ArgTypes.STRING, "asset", "server.commands.prefab.list.storeType.desc");
         this.textFlag = this.withFlagArg("text", "server.commands.prefab.list.text.desc");
      }

      protected void executeSync(@Nonnull CommandContext context) {
         Path var10000;
         switch ((String)this.storeTypeArg.get(context)) {
            case "server":
               var10000 = PrefabStore.get().getServerPrefabsPath();
               break;
            case "asset":
               List<PrefabStore.AssetPackPrefabPath> assetPaths = PrefabStore.get().getAllAssetPrefabPaths();
               var10000 = assetPaths.isEmpty() ? PrefabStore.get().getAssetPrefabsPath() : ((PrefabStore.AssetPackPrefabPath)assetPaths.getFirst()).prefabsPath();
               break;
            case "worldgen":
               var10000 = PrefabStore.get().getWorldGenPrefabsPath();
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + storeType);
         }

         final Path prefabStorePath = var10000;
         Ref<EntityStore> ref = context.senderAsPlayerRef();
         if (ref != null && ref.isValid() && !(Boolean)this.textFlag.get(context)) {
            Store<EntityStore> store = ref.getStore();
            World world = ((EntityStore)store.getExternalData()).getWorld();
            world.execute(() -> {
               Player playerComponent = (Player)store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               PlayerRef playerRefComponent = (PlayerRef)store.getComponent(ref, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRefComponent);
               playerComponent.getPageManager().openCustomPage(ref, store, new PrefabPage(playerRefComponent, prefabStorePath, builderState));
            });
         } else {
            try {
               final List<Message> prefabFiles = new ObjectArrayList();
               if ("asset".equals(storeType)) {
                  for(PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
                     final Path path = packPath.prefabsPath();
                     final String packPrefix = packPath.isBasePack() ? "" : "[" + packPath.getPackName() + "] ";
                     if (Files.isDirectory(path, new LinkOption[0])) {
                        Files.walkFileTree(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, 2147483647, new SimpleFileVisitor<Path>() {
                           @Nonnull
                           public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                              String fileName = file.getFileName().toString();
                              if (fileName.endsWith(".prefab.json")) {
                                 String var10001 = packPrefix;
                                 prefabFiles.add(Message.raw(var10001 + PathUtil.relativize(path, file).toString()));
                              }

                              return FileVisitResult.CONTINUE;
                           }
                        });
                     }
                  }
               } else if (Files.isDirectory(prefabStorePath, new LinkOption[0])) {
                  Files.walkFileTree(prefabStorePath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, 2147483647, new SimpleFileVisitor<Path>() {
                     @Nonnull
                     public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".prefab.json")) {
                           prefabFiles.add(Message.raw(PathUtil.relativize(prefabStorePath, file).toString()));
                        }

                        return FileVisitResult.CONTINUE;
                     }
                  });
               }

               context.sendMessage(MessageFormat.list(Message.translation("server.commands.prefab.list.header"), prefabFiles));
            } catch (IOException e) {
               context.sendMessage(Message.translation("server.builderTools.prefab.errorListingPrefabs").param("reason", e.getMessage()));
            }

         }
      }
   }
}
