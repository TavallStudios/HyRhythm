package com.hypixel.hytale.server.core.universe.datastore;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.BsonUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class DiskDataStore<T> implements DataStore<T> {
   private static final String EXTENSION = ".json";
   private static final int EXTENSION_LEN = ".json".length();
   private static final String EXTENSION_BACKUP = ".json.bak";
   private static final String GLOB = "*.json";
   private static final String GLOB_WITH_BACKUP = "*.{json,json.bak}";
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final Path path;
   private final BuilderCodec<T> codec;

   public DiskDataStore(@Nonnull String path, BuilderCodec<T> codec) {
      this.logger = HytaleLogger.get("DataStore|" + path);
      Path universePath = Universe.get().getPath();
      Path resolved = PathUtil.resolvePathWithinDir(universePath, path);
      if (resolved == null) {
         throw new IllegalStateException("Data store path must be within universe directory: " + path);
      } else {
         this.path = resolved;
         this.codec = codec;
         if (Files.isDirectory(this.path, new LinkOption[0])) {
            try {
               DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.bson");

               try {
                  for(Path oldPath : paths) {
                     Path newPath = getPathFromId(this.path, getIdFromPath(oldPath));

                     try {
                        Files.move(oldPath, newPath);
                     } catch (IOException var11) {
                     }
                  }
               } catch (Throwable var12) {
                  if (paths != null) {
                     try {
                        paths.close();
                     } catch (Throwable var10) {
                        var12.addSuppressed(var10);
                     }
                  }

                  throw var12;
               }

               if (paths != null) {
                  paths.close();
               }
            } catch (IOException e) {
               ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(e)).log("Failed to migrate files form .bson to .json!");
            }
         }

      }
   }

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   public BuilderCodec<T> getCodec() {
      return this.codec;
   }

   @Nullable
   public T load(String id) throws IOException {
      Path filePath = getPathFromId(this.path, id);
      return (T)(Files.exists(filePath, new LinkOption[0]) ? this.load0(filePath) : null);
   }

   public void save(String id, T value) {
      ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
      BsonDocument bsonValue = this.codec.encode(value, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.logger);
      BsonUtil.writeDocument(getPathFromId(this.path, id), bsonValue.asDocument()).join();
   }

   public void remove(String id) throws IOException {
      Files.deleteIfExists(getPathFromId(this.path, id));
      Files.deleteIfExists(getBackupPathFromId(this.path, id));
   }

   @Nonnull
   public List<String> list() throws IOException {
      List<String> list = new ObjectArrayList();
      DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.json");

      try {
         for(Path path : paths) {
            list.add(getIdFromPath(path));
         }
      } catch (Throwable var6) {
         if (paths != null) {
            try {
               paths.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (paths != null) {
         paths.close();
      }

      return list;
   }

   @Nonnull
   public Map<String, T> loadAll() throws IOException {
      Map<String, T> map = new Object2ObjectOpenHashMap();
      DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.json");

      try {
         for(Path path : paths) {
            map.put(getIdFromPath(path), this.load0(path));
         }
      } catch (Throwable var6) {
         if (paths != null) {
            try {
               paths.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (paths != null) {
         paths.close();
      }

      return map;
   }

   public void removeAll() throws IOException {
      DirectoryStream<Path> paths = Files.newDirectoryStream(this.path, "*.{json,json.bak}");

      try {
         for(Path path : paths) {
            Files.delete(path);
         }
      } catch (Throwable var5) {
         if (paths != null) {
            try {
               paths.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (paths != null) {
         paths.close();
      }

   }

   @Nullable
   protected T load0(@Nonnull Path path) throws IOException {
      return (T)RawJsonReader.readSync(path, this.codec, this.logger);
   }

   @Nonnull
   protected static Path getPathFromId(@Nonnull Path path, String id) {
      if (!PathUtil.isValidName(id)) {
         throw new IllegalArgumentException("Invalid ID: " + id);
      } else {
         return path.resolve(id + ".json");
      }
   }

   @Nonnull
   protected static Path getBackupPathFromId(@Nonnull Path path, String id) {
      if (!PathUtil.isValidName(id)) {
         throw new IllegalArgumentException("Invalid ID: " + id);
      } else {
         return path.resolve(id + ".json.bak");
      }
   }

   @Nonnull
   protected static String getIdFromPath(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      return fileName.substring(0, fileName.length() - EXTENSION_LEN);
   }
}
