package com.hypixel.hytale.server.core.asset.type.buildertool.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabListAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, PrefabListAsset>> {
   public static final AssetBuilderCodec<String, PrefabListAsset> CODEC;
   private static AssetStore<String, PrefabListAsset, DefaultAssetMap<String, PrefabListAsset>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private String id;
   private Path[] prefabPaths;
   private PrefabReference[] prefabReferences;
   private AssetExtraInfo.Data data;

   public static AssetStore<String, PrefabListAsset, DefaultAssetMap<String, PrefabListAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, PrefabListAsset, DefaultAssetMap<String, PrefabListAsset>>getAssetStore(PrefabListAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, PrefabListAsset> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   private void convertPrefabReferencesToPrefabPaths() {
      if (this.prefabReferences != null) {
         ObjectArrayList<Path> paths = new ObjectArrayList();

         for(PrefabReference prefabReference : this.prefabReferences) {
            paths.addAll(prefabReference.prefabPaths);
         }

         this.prefabPaths = (Path[])paths.toArray((x$0) -> new Path[x$0]);
      }
   }

   public Path[] getPrefabPaths() {
      return this.prefabPaths;
   }

   @Nonnull
   public PrefabReference[] getPrefabReferences() {
      return this.prefabReferences != null ? this.prefabReferences : new PrefabReference[0];
   }

   @Nullable
   public Path getRandomPrefab() {
      return this.prefabPaths.length == 0 ? null : this.prefabPaths[ThreadLocalRandom.current().nextInt(this.prefabPaths.length)];
   }

   public String getId() {
      return this.id;
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(PrefabListAsset.class, PrefabListAsset::new, Codec.STRING, (builder, id) -> builder.id = id, (builder) -> builder.id, (builder, data) -> builder.data = data, (builder) -> builder.data).append(new KeyedCodec("Prefabs", new ArrayCodec(PrefabListAsset.PrefabReference.CODEC, (x$0) -> new PrefabReference[x$0]), true), (builder, prefabPaths) -> builder.prefabReferences = prefabPaths, (builder) -> builder.prefabReferences).add()).afterDecode(PrefabListAsset::convertPrefabReferencesToPrefabPaths)).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(PrefabListAsset::getAssetStore));
   }

   public static enum PrefabRootDirectory {
      Server(() -> PrefabStore.get().getServerPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.server"),
      Asset(() -> PrefabStore.get().getAssetPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.asset"),
      Worldgen(() -> PrefabStore.get().getWorldGenPrefabsPath(), "server.commands.editprefab.ui.rootDirectory.worldGen");

      private final Supplier<Path> prefabPath;
      private final String localizationString;

      private PrefabRootDirectory(Supplier<Path> prefabPath, String localizationString) {
         this.prefabPath = prefabPath;
         this.localizationString = localizationString;
      }

      public Path getPrefabPath() {
         return (Path)this.prefabPath.get();
      }

      public String getLocalizationString() {
         return this.localizationString;
      }
   }

   public static class PrefabReference {
      public static final BuilderCodec<PrefabReference> CODEC;
      public PrefabRootDirectory rootDirectory;
      public String unprocessedPrefabPath;
      public boolean recursive = false;
      @Nonnull
      public List<Path> prefabPaths = new ObjectArrayList();

      public void processPrefabPath() {
         if (this.unprocessedPrefabPath != null) {
            this.unprocessedPrefabPath = this.unprocessedPrefabPath.replace('\\', '/');
            Path prefabRoot = this.rootDirectory.getPrefabPath();
            if (!this.unprocessedPrefabPath.endsWith("/")) {
               if (!this.unprocessedPrefabPath.endsWith(".prefab.json")) {
                  this.unprocessedPrefabPath = this.unprocessedPrefabPath + ".prefab.json";
               }

               Path resolved = PathUtil.resolvePathWithinDir(prefabRoot, this.unprocessedPrefabPath);
               if (resolved == null) {
                  PrefabListAsset.getAssetStore().getLogger().at(Level.SEVERE).log("Invalid prefab path: %s", this.unprocessedPrefabPath);
               } else {
                  this.prefabPaths.add(resolved);
               }
            } else {
               Path resolved = PathUtil.resolvePathWithinDir(prefabRoot, this.unprocessedPrefabPath);
               if (resolved == null) {
                  PrefabListAsset.getAssetStore().getLogger().at(Level.SEVERE).log("Invalid prefab path: %s", this.unprocessedPrefabPath);
               } else {
                  try {
                     Stream<Path> walk = Files.walk(resolved, this.recursive ? 5 : 1, new FileVisitOption[0]);

                     try {
                        Stream var10000 = walk.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).filter((path) -> path.toString().endsWith(".prefab.json"));
                        List var10001 = this.prefabPaths;
                        Objects.requireNonNull(var10001);
                        var10000.forEach(var10001::add);
                     } catch (Throwable var7) {
                        if (walk != null) {
                           try {
                              walk.close();
                           } catch (Throwable var6) {
                              var7.addSuppressed(var6);
                           }
                        }

                        throw var7;
                     }

                     if (walk != null) {
                        walk.close();
                     }
                  } catch (IOException e) {
                     ((HytaleLogger.Api)PrefabListAsset.getAssetStore().getLogger().at(Level.SEVERE).withCause(e)).log("Failed to process prefab path: %s", this.unprocessedPrefabPath);
                  }

               }
            }
         }
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PrefabReference.class, PrefabReference::new).append(new KeyedCodec("RootDirectory", new EnumCodec(PrefabRootDirectory.class), true), (prefabReference, rootDirectory) -> prefabReference.rootDirectory = rootDirectory, (prefabReference) -> prefabReference.rootDirectory).add()).append(new KeyedCodec("Path", Codec.STRING, true), (prefabReference, unprocessedPrefabPath) -> prefabReference.unprocessedPrefabPath = unprocessedPrefabPath, (prefabReference) -> prefabReference.unprocessedPrefabPath).add()).append(new KeyedCodec("Recursive", Codec.BOOLEAN, false), (prefabReference, recursive) -> prefabReference.recursive = recursive, (prefabReference) -> prefabReference.recursive).add()).afterDecode(PrefabReference::processPrefabPath)).build();
      }
   }
}
