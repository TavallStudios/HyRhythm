package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.sneakythrow.SneakyThrow;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;

public class FileUtil {
   public static final Set<OpenOption> DEFAULT_WRITE_OPTIONS;
   public static final Set<FileVisitOption> DEFAULT_WALK_TREE_OPTIONS_SET;
   public static final FileVisitOption[] DEFAULT_WALK_TREE_OPTIONS_ARRAY;
   public static final Pattern INVALID_FILENAME_CHARACTERS;

   public static void unzipFile(@Nonnull Path path, @Nonnull byte[] buffer, @Nonnull ZipInputStream zipStream, @Nonnull ZipEntry zipEntry, @Nonnull String name) throws IOException {
      Path filePath = path.resolve(name);
      if (!filePath.toAbsolutePath().startsWith(path)) {
         throw new ZipException("Entry is outside of the target dir: " + zipEntry.getName());
      } else {
         
         if (zipEntry.isDirectory()) {
            Files.createDirectory(filePath);
         } else {
            OutputStream stream = Files.newOutputStream(filePath);

            int len;
            try {
               while((len = zipStream.read(buffer)) > 0) {
                  stream.write(buffer, 0, len);
               }
            } catch (Throwable var10) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (stream != null) {
               stream.close();
            }
         }

         zipStream.closeEntry();
      }
   }

   public static void copyDirectory(@Nonnull Path origin, @Nonnull Path destination) throws IOException {
      Stream<Path> paths = Files.walk(origin);

      try {
         paths.forEach((originSubPath) -> {
            try {
               Path relative = origin.relativize(originSubPath);
               Path destinationSubPath = destination.resolve(relative);
               Files.copy(originSubPath, destinationSubPath);
            } catch (Throwable t) {
               throw new RuntimeException("Error copying path", t);
            }
         });
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

   }

   public static void moveDirectoryContents(@Nonnull Path origin, @Nonnull Path destination, CopyOption... options) throws IOException {
      Stream<Path> paths = Files.walk(origin);

      try {
         paths.forEach((originSubPath) -> {
            if (!originSubPath.equals(origin)) {
               try {
                  Path relative = origin.relativize(originSubPath);
                  Path destinationSubPath = destination.resolve(relative);
                  Files.move(originSubPath, destinationSubPath, options);
               } catch (Throwable t) {
                  throw new RuntimeException("Error moving path", t);
               }
            }
         });
      } catch (Throwable var7) {
         if (paths != null) {
            try {
               paths.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (paths != null) {
         paths.close();
      }

   }

   public static void deleteDirectory(@Nonnull Path path) throws IOException {
      Stream<Path> stream = Files.walk(path);

      try {
         stream.sorted(Comparator.reverseOrder()).forEach(SneakyThrow.sneakyConsumer(Files::delete));
      } catch (Throwable var5) {
         if (stream != null) {
            try {
               stream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (stream != null) {
         stream.close();
      }

   }

   public static void extractZip(@Nonnull Path zipFile, @Nonnull Path destDir) throws IOException {
      extractZip(Files.newInputStream(zipFile), destDir);
   }

   public static void extractZip(@Nonnull InputStream inputStream, @Nonnull Path destDir) throws IOException {
      ZipInputStream zis = new ZipInputStream(inputStream);

      ZipEntry entry;
      try {
         for(; (entry = zis.getNextEntry()) != null; zis.closeEntry()) {
            Path destPath = destDir.resolve(entry.getName()).normalize();
            if (!destPath.startsWith(destDir)) {
               throw new ZipException("Zip entry outside target directory: " + entry.getName());
            }

            if (entry.isDirectory()) {
               Files.createDirectories(destPath);
            } else {
               Files.createDirectories(destPath.getParent());
               Files.copy(zis, destPath, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }
         }
      } catch (Throwable var6) {
         try {
            zis.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      zis.close();
   }

   public static void writeStringAtomic(@Nonnull Path file, @Nonnull String content, boolean backup) throws IOException {
      Path parent = file.getParent();
      if (parent != null) {
         Files.createDirectories(parent);
      }

      Path tmpPath = Files.createTempFile(parent, String.valueOf(file.getFileName()) + ".", ".tmp");
      Path bakPath = file.resolveSibling(String.valueOf(file.getFileName()) + ".bak");

      try {
         Files.writeString(tmpPath, content);
         if (backup && Files.isRegularFile(file, new LinkOption[0])) {
            atomicMove(file, bakPath);
         }

         atomicMove(tmpPath, file);
      } finally {
         Files.deleteIfExists(tmpPath);
      }
   }

   public static void atomicMove(@Nonnull Path source, @Nonnull Path target) throws IOException {
      try {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException var3) {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
      }

   }

   public static void writeStringAtomic(@Nonnull Path file, @Nonnull String content) throws IOException {
      writeStringAtomic(file, content, true);
   }

   static {
      DEFAULT_WRITE_OPTIONS = Set.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
      DEFAULT_WALK_TREE_OPTIONS_SET = Set.of();
      DEFAULT_WALK_TREE_OPTIONS_ARRAY = new FileVisitOption[0];
      INVALID_FILENAME_CHARACTERS = Pattern.compile("[<>:\"|?*/\\\\]");
   }
}
