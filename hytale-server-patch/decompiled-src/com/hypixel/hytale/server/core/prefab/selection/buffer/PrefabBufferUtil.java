package com.hypixel.hytale.server.core.prefab.selection.buffer;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableSupplier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class PrefabBufferUtil {
   public static final Path CACHE_PATH;
   public static final String LPF_FILE_SUFFIX = ".lpf";
   public static final String JSON_FILE_SUFFIX = ".prefab.json";
   public static final String JSON_LPF_FILE_SUFFIX = ".prefab.json.lpf";
   public static final String FILE_SUFFIX_REGEX = "((!\\.prefab\\.json)\\.lpf|\\.prefab\\.json)$";
   public static final Pattern FILE_SUFFIX_PATTERN;
   public static final HytaleLogger LOGGER;
   private static final Map<Path, WeakReference<CachedEntry>> CACHE;

   @Nonnull
   public static IPrefabBuffer getCached(@Nonnull Path path) {
      WeakReference<CachedEntry> reference = (WeakReference)CACHE.get(path);
      CachedEntry cachedPrefab = reference != null ? (CachedEntry)reference.get() : null;
      if (cachedPrefab != null) {
         long stamp = cachedPrefab.lock.readLock();

         try {
            if (cachedPrefab.buffer != null) {
               PrefabBuffer.PrefabBufferAccessor var5 = cachedPrefab.buffer.newAccess();
               return var5;
            }
         } finally {
            cachedPrefab.lock.unlockRead(stamp);
         }
      }

      cachedPrefab = getOrCreateCacheEntry(path);
      long stamp = cachedPrefab.lock.writeLock();

      PrefabBuffer.PrefabBufferAccessor var16;
      try {
         if (cachedPrefab.buffer == null) {
            cachedPrefab.buffer = loadBuffer(path);
            var16 = cachedPrefab.buffer.newAccess();
            return var16;
         }

         var16 = cachedPrefab.buffer.newAccess();
      } finally {
         cachedPrefab.lock.unlockWrite(stamp);
      }

      return var16;
   }

   @Nonnull
   public static PrefabBuffer loadBuffer(@Nonnull Path path) {
      String fileNameStr = path.getFileName().toString();
      String fileName = fileNameStr.replace(".prefab.json.lpf", "").replace(".prefab.json", "");
      Path lpfPath = path.resolveSibling(fileName + ".lpf");
      if (Files.exists(lpfPath, new LinkOption[0])) {
         return loadFromLPF(path, lpfPath);
      } else {
         Path cachedLpfPath;
         AssetPack pack;
         if (AssetModule.get().isAssetPathImmutable(path)) {
            Path lpfConvertedPath = path.resolveSibling(fileName + ".prefab.json.lpf");
            if (Files.exists(lpfConvertedPath, new LinkOption[0])) {
               return loadFromLPF(path, lpfConvertedPath);
            }

            pack = AssetModule.get().findAssetPackForPath(path);
            if (pack != null) {
               String safePackName = FileUtil.INVALID_FILENAME_CHARACTERS.matcher(pack.getName()).replaceAll("_");
               cachedLpfPath = CACHE_PATH.resolve(safePackName).resolve(pack.getRoot().relativize(lpfConvertedPath).toString());
            } else if (lpfConvertedPath.getRoot() != null) {
               cachedLpfPath = CACHE_PATH.resolve(lpfConvertedPath.subpath(1, lpfConvertedPath.getNameCount()).toString());
            } else {
               cachedLpfPath = CACHE_PATH.resolve(lpfConvertedPath.toString());
            }
         } else {
            cachedLpfPath = path.resolveSibling(fileName + ".prefab.json.lpf");
            pack = null;
         }

         Path jsonPath = path.resolveSibling(fileName + ".prefab.json");
         if (!Files.exists(jsonPath, new LinkOption[0])) {
            try {
               Files.deleteIfExists(cachedLpfPath);
            } catch (IOException var8) {
            }

            throw new Error("Error loading Prefab from " + String.valueOf(jsonPath.toAbsolutePath()) + " (.lpf and .prefab.json) File NOT found!");
         } else {
            try {
               return loadFromJson(pack, path, cachedLpfPath, jsonPath);
            } catch (IOException e) {
               throw SneakyThrow.sneakyThrow(e);
            }
         }
      }
   }

   @Nonnull
   public static CompletableFuture<Void> writeToFileAsync(@Nonnull PrefabBuffer prefab, @Nonnull Path path) {
      return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> {
         SeekableByteChannel channel = Files.newByteChannel(path, FileUtil.DEFAULT_WRITE_OPTIONS);

         try {
            channel.write(BinaryPrefabBufferCodec.INSTANCE.serialize(prefab).nioBuffer());
         } catch (Throwable var6) {
            if (channel != null) {
               try {
                  channel.close();
               } catch (Throwable x2) {
                  var6.addSuppressed(x2);
               }
            }

            throw var6;
         }

         if (channel != null) {
            channel.close();
         }

      }));
   }

   public static PrefabBuffer readFromFile(@Nonnull Path path) {
      return (PrefabBuffer)readFromFileAsync(path).join();
   }

   @Nonnull
   public static CompletableFuture<PrefabBuffer> readFromFileAsync(@Nonnull Path path) {
      return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier((ThrowableSupplier)(() -> {
         SeekableByteChannel channel = Files.newByteChannel(path);

         PrefabBuffer var4;
         try {
            int size = (int)channel.size();
            ByteBuf buf = Unpooled.buffer(size);
            buf.writerIndex(size);
            if (channel.read(buf.internalNioBuffer(0, size)) != size) {
               throw new IOException("Didn't read full file!");
            }

            var4 = BinaryPrefabBufferCodec.INSTANCE.deserialize(path, buf);
         } catch (Throwable var6) {
            if (channel != null) {
               try {
                  channel.close();
               } catch (Throwable x2) {
                  var6.addSuppressed(x2);
               }
            }

            throw var6;
         }

         if (channel != null) {
            channel.close();
         }

         return var4;
      })));
   }

   @Nonnull
   public static PrefabBuffer loadFromLPF(@Nonnull Path path, @Nonnull Path realPath) {
      try {
         return readFromFile(realPath);
      } catch (Exception e) {
         throw new Error("Error while loading prefab " + String.valueOf(path.toAbsolutePath()) + " from " + String.valueOf(realPath.toAbsolutePath()), e);
      }
   }

   @Nonnull
   public static PrefabBuffer loadFromJson(@Nullable AssetPack pack, Path path, @Nonnull Path cachedLpfPath, @Nonnull Path jsonPath) throws IOException {
      BasicFileAttributes cachedAttr = null;

      try {
         cachedAttr = Files.readAttributes(cachedLpfPath, BasicFileAttributes.class);
      } catch (IOException var10) {
      }

      FileTime targetModifiedTime;
      if (pack != null && pack.isImmutable()) {
         targetModifiedTime = Files.readAttributes(pack.getPackLocation(), BasicFileAttributes.class).lastModifiedTime();
      } else {
         targetModifiedTime = Files.readAttributes(jsonPath, BasicFileAttributes.class).lastModifiedTime();
      }

      if (cachedAttr != null && targetModifiedTime.compareTo(cachedAttr.lastModifiedTime()) <= 0) {
         try {
            return readFromFile(cachedLpfPath);
         } catch (CompletionException e) {
            if (!Options.getOptionSet().has(Options.VALIDATE_PREFABS)) {
               if (e.getCause() instanceof UpdateBinaryPrefabException) {
                  LOGGER.at(Level.FINE).log("Ignoring LPF %s due to: %s", path, e.getMessage());
               } else {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(new SkipSentryException(e))).log("Failed to load %s", cachedLpfPath);
               }
            }
         }
      }

      try {
         PrefabBuffer buffer = BsonPrefabBufferDeserializer.INSTANCE.deserialize(jsonPath, (BsonDocument)BsonUtil.readDocument(jsonPath, false).join());
         if (!Options.getOptionSet().has(Options.DISABLE_CPB_BUILD)) {
            try {
               Files.createDirectories(cachedLpfPath.getParent());
               writeToFileAsync(buffer, cachedLpfPath).thenRun(() -> {
                  try {
                     Files.setLastModifiedTime(cachedLpfPath, targetModifiedTime);
                  } catch (IOException var3) {
                  }

               }).exceptionally((throwable) -> {
                  ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.FINE).withCause(new SkipSentryException(throwable))).log("Failed to save prefab cache %s", cachedLpfPath);
                  return null;
               });
            } catch (IOException e) {
               LOGGER.at(Level.FINE).log("Cannot create cache directory for %s: %s", cachedLpfPath, e.getMessage());
            }
         }

         return buffer;
      } catch (Exception e) {
         throw new Error("Error while loading Prefab from " + String.valueOf(jsonPath.toAbsolutePath()), e);
      }
   }

   @Nonnull
   private static CachedEntry getOrCreateCacheEntry(Path path) {
      CachedEntry[] temp = new CachedEntry[1];
      CACHE.compute(path, (p, ref) -> {
         if (ref != null) {
            CachedEntry cached = (CachedEntry)ref.get();
            temp[0] = cached;
            if (cached != null) {
               return ref;
            }
         }

         return new WeakReference(temp[0] = new CachedEntry());
      });
      return temp[0];
   }

   static {
      CACHE_PATH = (Path)Options.getOrDefault(Options.PREFAB_CACHE_DIRECTORY, Options.getOptionSet(), Path.of(".cache/prefabs"));
      FILE_SUFFIX_PATTERN = Pattern.compile("((!\\.prefab\\.json)\\.lpf|\\.prefab\\.json)$");
      LOGGER = HytaleLogger.forEnclosingClass();
      CACHE = new ConcurrentHashMap();
   }

   private static class CachedEntry {
      private final StampedLock lock = new StampedLock();
      private PrefabBuffer buffer;
   }
}
