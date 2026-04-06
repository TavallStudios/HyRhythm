package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.commands.PrefabEditLoadCommand;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabAlignment;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRootDirectory;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRowSplitMode;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabStackingAxis;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.WorldGenType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabEditorCreationSettings implements PrefabEditorCreationContext, JsonAssetWithMap<String, DefaultAssetMap<String, PrefabEditorCreationSettings>> {
   private static final int RECURSIVE_SEARCH_MAX_DEPTH = 10;
   public static final AssetBuilderCodec<String, PrefabEditorCreationSettings> CODEC;
   private static AssetStore<String, PrefabEditorCreationSettings, DefaultAssetMap<String, PrefabEditorCreationSettings>> ASSET_STORE;
   private String id;
   private AssetExtraInfo.Data data;
   private transient Player player;
   private transient PlayerRef playerRef;
   private PrefabRootDirectory prefabRootDirectory;
   private final transient List<Path> prefabPaths;
   private List<String> unprocessedPrefabPaths;
   private int pasteYLevelGoal;
   private int blocksBetweenEachPrefab;
   private WorldGenType worldGenType;
   private int blocksAboveSurface;
   private PrefabStackingAxis stackingAxis;
   private PrefabAlignment alignment;
   private boolean recursive;
   private boolean loadChildren;
   private boolean loadEntities;
   private boolean enableWorldTicking;
   private PrefabRowSplitMode rowSplitMode;
   private String environment;
   private String grassTint;

   public static AssetStore<String, PrefabEditorCreationSettings, DefaultAssetMap<String, PrefabEditorCreationSettings>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, PrefabEditorCreationSettings, DefaultAssetMap<String, PrefabEditorCreationSettings>>getAssetStore(PrefabEditorCreationSettings.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, PrefabEditorCreationSettings> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   private PrefabEditorCreationSettings() {
      this.prefabRootDirectory = PrefabRootDirectory.ASSET;
      this.prefabPaths = new ObjectArrayList();
      this.unprocessedPrefabPaths = new ObjectArrayList();
      this.pasteYLevelGoal = 55;
      this.blocksBetweenEachPrefab = 15;
      this.worldGenType = PrefabEditLoadCommand.DEFAULT_WORLD_GEN_TYPE;
      this.blocksAboveSurface = 0;
      this.stackingAxis = PrefabEditLoadCommand.DEFAULT_PREFAB_STACKING_AXIS;
      this.alignment = PrefabEditLoadCommand.DEFAULT_PREFAB_ALIGNMENT;
      this.enableWorldTicking = false;
      this.rowSplitMode = PrefabRowSplitMode.BY_ALL_SUBFOLDERS;
      this.environment = "Env_Zone1_Plains";
      this.grassTint = "#5B9E28";
   }

   public PrefabEditorCreationSettings(PrefabRootDirectory prefabRootDirectory, List<String> unprocessedPrefabPaths, int pasteYLevelGoal, int blocksBetweenEachPrefab, WorldGenType worldGenType, int blocksAboveSurface, PrefabStackingAxis stackingAxis, PrefabAlignment alignment, boolean recursive, boolean loadChildren, boolean loadEntities, boolean enableWorldTicking, PrefabRowSplitMode rowSplitMode, String environment, String grassTint) {
      this.prefabRootDirectory = PrefabRootDirectory.ASSET;
      this.prefabPaths = new ObjectArrayList();
      this.unprocessedPrefabPaths = new ObjectArrayList();
      this.pasteYLevelGoal = 55;
      this.blocksBetweenEachPrefab = 15;
      this.worldGenType = PrefabEditLoadCommand.DEFAULT_WORLD_GEN_TYPE;
      this.blocksAboveSurface = 0;
      this.stackingAxis = PrefabEditLoadCommand.DEFAULT_PREFAB_STACKING_AXIS;
      this.alignment = PrefabEditLoadCommand.DEFAULT_PREFAB_ALIGNMENT;
      this.enableWorldTicking = false;
      this.rowSplitMode = PrefabRowSplitMode.BY_ALL_SUBFOLDERS;
      this.environment = "Env_Zone1_Plains";
      this.grassTint = "#5B9E28";
      this.prefabRootDirectory = prefabRootDirectory;
      this.unprocessedPrefabPaths = unprocessedPrefabPaths;
      this.pasteYLevelGoal = pasteYLevelGoal;
      this.blocksBetweenEachPrefab = blocksBetweenEachPrefab;
      this.worldGenType = worldGenType;
      this.blocksAboveSurface = blocksAboveSurface;
      this.stackingAxis = stackingAxis;
      this.alignment = alignment;
      this.recursive = recursive;
      this.loadChildren = loadChildren;
      this.loadEntities = loadEntities;
      this.enableWorldTicking = enableWorldTicking;
      this.rowSplitMode = rowSplitMode;
      this.environment = environment;
      this.grassTint = grassTint;
   }

   @Nullable
   PrefabEditorCreationContext finishProcessing(Player editor, PlayerRef playerRef, boolean creatingNewPrefab) {
      this.prefabPaths.clear();
      this.player = editor;
      this.playerRef = playerRef;

      for(String inputPrefabName : this.unprocessedPrefabPaths) {
         inputPrefabName = StringUtil.stripQuotes(inputPrefabName);
         inputPrefabName = inputPrefabName.replace('/', File.separatorChar);
         inputPrefabName = inputPrefabName.replace('\\', File.separatorChar);
         if (!SingleplayerModule.isOwner(playerRef) && !inputPrefabName.isEmpty() && Path.of(inputPrefabName).isAbsolute()) {
            this.player.sendMessage(Message.translation("server.commands.editprefab.error.absolutePathNotAllowed"));
            return null;
         }

         if (!inputPrefabName.endsWith(File.separator)) {
            if (!stringEndsWithPrefabPath(inputPrefabName)) {
               inputPrefabName = inputPrefabName + ".prefab.json";
            }

            try {
               Path rootPath = this.resolveRootPathForInput(inputPrefabName);
               String relativePath = this.getRelativePathForInput(inputPrefabName);
               Path resolvedPath = rootPath.resolve(relativePath);
               if (!SingleplayerModule.isOwner(playerRef) && !PathUtil.isChildOf(rootPath, resolvedPath)) {
                  this.player.sendMessage(Message.translation("server.commands.editprefab.error.pathTraversal"));
                  return null;
               }

               this.prefabPaths.add(resolvedPath);
            } catch (Exception e) {
               e.printStackTrace();
               this.player.sendMessage(Message.translation("server.commands.editprefab.finishProcessingError").param("error", e.getMessage()));
               return null;
            }
         } else {
            Path rootPath = this.resolveRootPathForInput(inputPrefabName);
            String relativePath = this.getRelativePathForInput(inputPrefabName);
            Path resolvedDir = rootPath.resolve(relativePath);
            if (!SingleplayerModule.isOwner(playerRef) && !PathUtil.isChildOf(rootPath, resolvedDir)) {
               this.player.sendMessage(Message.translation("server.commands.editprefab.error.pathTraversal"));
               return null;
            }

            try {
               Stream<Path> walk = Files.walk(resolvedDir, this.recursive ? 10 : 1, new FileVisitOption[0]);

               try {
                  Stream var10000 = walk.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).filter((path) -> path.toString().endsWith(".prefab.json"));
                  List var10001 = this.prefabPaths;
                  Objects.requireNonNull(var10001);
                  var10000.forEach(var10001::add);
               } catch (Throwable var14) {
                  if (walk != null) {
                     try {
                        walk.close();
                     } catch (Throwable var12) {
                        var14.addSuppressed(var12);
                     }
                  }

                  throw var14;
               }

               if (walk != null) {
                  walk.close();
               }
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      if (!creatingNewPrefab) {
         for(Path processedPrefabPath : this.prefabPaths) {
            if (!Files.exists(processedPrefabPath, new LinkOption[0])) {
               this.player.sendMessage(Message.translation("server.commands.editprefab.load.error.prefabNotFound").param("path", processedPrefabPath.toString()));
               return null;
            }
         }
      }

      if (this.prefabPaths.isEmpty()) {
         Message header = Message.translation("server.commands.editprefab.noPrefabsInPath");
         Set<Message> values = (Set)this.unprocessedPrefabPaths.stream().map((p) -> this.prefabRootDirectory.getPrefabPath().resolve(p)).map(Path::toString).map(Message::raw).collect(Collectors.toSet());
         this.player.sendMessage(MessageFormat.list(header, values));
         return null;
      } else {
         return this;
      }
   }

   @Nonnull
   private Path resolveRootPathForInput(@Nonnull String inputPath) {
      if (this.prefabRootDirectory != PrefabRootDirectory.ASSET) {
         return this.prefabRootDirectory.getPrefabPath();
      } else {
         String firstComponent = inputPath.contains(File.separator) ? inputPath.substring(0, inputPath.indexOf(File.separator)) : inputPath;

         for(PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
            if (packPath.getDisplayName().equals(firstComponent)) {
               return packPath.prefabsPath();
            }
         }

         return this.prefabRootDirectory.getPrefabPath();
      }
   }

   @Nonnull
   private String getRelativePathForInput(@Nonnull String inputPath) {
      if (this.prefabRootDirectory != PrefabRootDirectory.ASSET) {
         return inputPath;
      } else {
         String firstComponent = inputPath.contains(File.separator) ? inputPath.substring(0, inputPath.indexOf(File.separator)) : inputPath;

         for(PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
            if (packPath.getDisplayName().equals(firstComponent)) {
               if (inputPath.contains(File.separator)) {
                  return inputPath.substring(inputPath.indexOf(File.separator) + 1);
               }

               return "";
            }
         }

         return inputPath;
      }
   }

   public static boolean stringEndsWithPrefabPath(@Nonnull String input) {
      return input.endsWith(".prefab.json") || input.endsWith(".prefab.json.lpf") || input.endsWith(".lpf");
   }

   @Nonnull
   public static CompletableFuture<PrefabEditorCreationSettings> load(@Nonnull String name) {
      return CompletableFuture.supplyAsync(() -> (PrefabEditorCreationSettings)getAssetMap().getAsset(name));
   }

   @Nonnull
   public static CompletableFuture<Void> save(@Nonnull String name, PrefabEditorCreationSettings settings) {
      return CompletableFuture.runAsync(() -> {
         try {
            getAssetStore().writeAssetToDisk(AssetModule.get().getBaseAssetPack(), Map.of(Path.of(name + ".json"), settings));
         } catch (IOException e) {
            e.printStackTrace();
         }

      });
   }

   public Player getEditor() {
      return this.player;
   }

   public PlayerRef getEditorRef() {
      return this.playerRef;
   }

   public List<Path> getPrefabPaths() {
      return this.prefabPaths;
   }

   public int getBlocksBetweenEachPrefab() {
      return this.blocksBetweenEachPrefab;
   }

   public int getPasteLevelGoal() {
      return this.pasteYLevelGoal;
   }

   public boolean loadChildPrefabs() {
      return this.loadChildren;
   }

   public boolean shouldLoadEntities() {
      return this.loadEntities;
   }

   public PrefabStackingAxis getStackingAxis() {
      return this.stackingAxis;
   }

   public WorldGenType getWorldGenType() {
      return this.worldGenType;
   }

   public int getBlocksAboveSurface() {
      return this.blocksAboveSurface;
   }

   public PrefabAlignment getAlignment() {
      return this.alignment;
   }

   public String getId() {
      return this.id;
   }

   public PrefabRootDirectory getPrefabRootDirectory() {
      return this.prefabRootDirectory;
   }

   public List<String> getUnprocessedPrefabPaths() {
      return this.unprocessedPrefabPaths;
   }

   public int getPasteYLevelGoal() {
      return this.pasteYLevelGoal;
   }

   public boolean isRecursive() {
      return this.recursive;
   }

   public boolean isLoadChildren() {
      return this.loadChildren;
   }

   public boolean isWorldTickingEnabled() {
      return this.enableWorldTicking;
   }

   public PrefabRowSplitMode getRowSplitMode() {
      return this.rowSplitMode;
   }

   public String getEnvironment() {
      return this.environment;
   }

   public String getGrassTint() {
      return this.grassTint;
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(PrefabEditorCreationSettings.class, PrefabEditorCreationSettings::new, Codec.STRING, (builder, id) -> builder.id = id, (builder) -> builder.id, (builder, data) -> builder.data = data, (builder) -> builder.data).append(new KeyedCodec("RootDirectory", new EnumCodec(PrefabRootDirectory.class)), (o, rootDirectory) -> o.prefabRootDirectory = rootDirectory, (o) -> o.prefabRootDirectory).add()).append(new KeyedCodec("UnprocessedPrefabPaths", new ArrayCodec(Codec.STRING, (x$0) -> new String[x$0])), (o, unprocessedPrefabPaths) -> o.unprocessedPrefabPaths = List.of(unprocessedPrefabPaths), (o) -> (String[])o.unprocessedPrefabPaths.toArray((x$0) -> new String[x$0])).add()).append(new KeyedCodec("PasteYLevelGoal", Codec.INTEGER), (o, pasteYLevelGoal) -> o.pasteYLevelGoal = pasteYLevelGoal, (o) -> o.pasteYLevelGoal).add()).append(new KeyedCodec("BlocksBetweenEachPrefab", Codec.INTEGER), (o, blocksBetweenEachPrefab) -> o.blocksBetweenEachPrefab = blocksBetweenEachPrefab, (o) -> o.blocksBetweenEachPrefab).add()).append(new KeyedCodec("WorldGenType", new EnumCodec(WorldGenType.class)), (o, worldGenType) -> o.worldGenType = worldGenType, (o) -> o.worldGenType).add()).append(new KeyedCodec("BlocksAboveSurface", Codec.INTEGER), (o, blocksAboveSurface) -> o.blocksAboveSurface = blocksAboveSurface, (o) -> o.blocksAboveSurface).add()).append(new KeyedCodec("PrefabStackingAxis", new EnumCodec(PrefabStackingAxis.class)), (o, stackingAxis) -> o.stackingAxis = stackingAxis, (o) -> o.stackingAxis).add()).append(new KeyedCodec("PrefabAlignment", new EnumCodec(PrefabAlignment.class)), (o, alignment) -> o.alignment = alignment, (o) -> o.alignment).add()).append(new KeyedCodec("RecursiveSearch", Codec.BOOLEAN), (o, recursive) -> o.recursive = recursive, (o) -> o.recursive).add()).append(new KeyedCodec("LoadChildren", Codec.BOOLEAN), (o, loadChildren) -> o.loadChildren = loadChildren, (o) -> o.loadChildren).add()).append(new KeyedCodec("LoadEntities", Codec.BOOLEAN), (o, loadEntities) -> o.loadEntities = loadEntities, (o) -> o.loadEntities).add()).append(new KeyedCodec("EnableWorldTicking", Codec.BOOLEAN), (o, enableWorldTicking) -> o.enableWorldTicking = enableWorldTicking, (o) -> o.enableWorldTicking).add()).append(new KeyedCodec("RowSplitMode", new EnumCodec(PrefabRowSplitMode.class)), (o, rowSplitMode) -> o.rowSplitMode = rowSplitMode, (o) -> o.rowSplitMode).add()).append(new KeyedCodec("Environment", Codec.STRING), (o, environment) -> o.environment = environment, (o) -> o.environment).addValidator(Environment.VALIDATOR_CACHE.getValidator()).add()).append(new KeyedCodec("GrassTint", Codec.STRING), (o, grassTint) -> o.grassTint = grassTint, (o) -> o.grassTint).add()).build();
   }
}
