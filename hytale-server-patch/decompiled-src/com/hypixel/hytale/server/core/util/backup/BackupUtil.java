package com.hypixel.hytale.server.core.util.backup;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.interface_.WorldSavingStatus;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class BackupUtil {
   static void walkFileTreeAndZip(@Nonnull Path sourceDir, @Nonnull Path zipPath) throws IOException {
      ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipPath));

      try {
         zipOutputStream.setMethod(0);
         zipOutputStream.setLevel(0);
         String var10001 = ManifestUtil.getImplementationVersion();
         zipOutputStream.setComment("Automated backup by HytaleServer - Version: " + var10001 + ", Revision: " + ManifestUtil.getImplementationRevisionId());
         Stream<Path> stream = Files.walk(sourceDir);

         try {
            for(Path path : stream.filter((x$0) -> Files.isRegularFile(x$0, new LinkOption[0])).toList()) {
               long size = Files.size(path);
               CRC32 crc = new CRC32();
               InputStream inputStream = Files.newInputStream(path);

               try {
                  byte[] buffer = new byte[16384];

                  int len;
                  while((len = inputStream.read(buffer)) != -1) {
                     crc.update(buffer, 0, len);
                  }
               } catch (Throwable var16) {
                  if (inputStream != null) {
                     try {
                        inputStream.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (inputStream != null) {
                  inputStream.close();
               }

               ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
               zipEntry.setSize(size);
               zipEntry.setCompressedSize(size);
               zipEntry.setCrc(crc.getValue());
               zipOutputStream.putNextEntry(zipEntry);
               Files.copy(path, zipOutputStream);
               zipOutputStream.closeEntry();
            }
         } catch (Throwable var17) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var14) {
                  var17.addSuppressed(var14);
               }
            }

            throw var17;
         }

         if (stream != null) {
            stream.close();
         }
      } catch (Throwable var18) {
         try {
            zipOutputStream.close();
         } catch (Throwable var13) {
            var18.addSuppressed(var13);
         }

         throw var18;
      }

      zipOutputStream.close();
   }

   static void broadcastBackupStatus(boolean isWorldSaving) {
      Universe.get().broadcastPacket((ToClientPacket)(new WorldSavingStatus(isWorldSaving)));
   }

   static void broadcastBackupError(Throwable cause) {
      Message message = Message.translation("server.universe.backup.error").param("message", cause.getLocalizedMessage());
      Universe.get().getPlayers().forEach((player) -> {
         boolean hasPermission = PermissionsModule.get().hasPermission(player.getUuid(), "hytale.status.backup.error");
         if (hasPermission) {
            player.sendMessage(message);
         }

      });
   }

   @Nullable
   static List<Path> findOldBackups(@Nonnull Path backupDirectory, int maxBackupCount) throws IOException {
      if (!backupDirectory.toFile().isDirectory()) {
         return null;
      } else {
         Stream<Path> files = Files.list(backupDirectory);

         List var4;
         label44: {
            try {
               List<Path> zipFiles = files.filter((p) -> p.getFileName().toString().endsWith(".zip")).sorted(Comparator.comparing((p) -> {
                  try {
                     return Files.readAttributes(p, BasicFileAttributes.class).creationTime();
                  } catch (IOException var2) {
                     return FileTime.fromMillis(0L);
                  }
               })).toList();
               if (zipFiles.size() > maxBackupCount) {
                  var4 = zipFiles.subList(0, zipFiles.size() - maxBackupCount);
                  break label44;
               }
            } catch (Throwable var6) {
               if (files != null) {
                  try {
                     files.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (files != null) {
               files.close();
            }

            return null;
         }

         if (files != null) {
            files.close();
         }

         return var4;
      }
   }
}
