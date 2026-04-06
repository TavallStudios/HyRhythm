package com.hypixel.hytale.server.core.util.io;

import com.hypixel.hytale.server.core.Options;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;

public abstract class BlockingDiskFile {
   protected final ReadWriteLock fileLock = new ReentrantReadWriteLock();
   protected final Path path;

   public BlockingDiskFile(Path path) {
      this.path = path;
   }

   protected abstract void read(BufferedReader var1) throws IOException;

   protected abstract void write(BufferedWriter var1) throws IOException;

   protected abstract void create(BufferedWriter var1) throws IOException;

   public void syncLoad() {
      this.fileLock.writeLock().lock();

      try {
         File file = this.toLocalFile();

         try {
            if (!file.exists()) {
               if (Options.getOptionSet().has(Options.BARE)) {
                  ByteArrayOutputStream out = new ByteArrayOutputStream();

                  byte[] bytes;
                  try {
                     BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out));

                     try {
                        this.create(buf);
                        bytes = out.toByteArray();
                     } catch (Throwable var26) {
                        try {
                           buf.close();
                        } catch (Throwable var24) {
                           var26.addSuppressed(var24);
                        }

                        throw var26;
                     }

                     buf.close();
                  } catch (Throwable var27) {
                     try {
                        out.close();
                     } catch (Throwable var23) {
                        var27.addSuppressed(var23);
                     }

                     throw var27;
                  }

                  out.close();
                  BufferedReader buf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));

                  try {
                     this.read(buf);
                  } catch (Throwable var25) {
                     try {
                        buf.close();
                     } catch (Throwable var22) {
                        var25.addSuppressed(var22);
                     }

                     throw var25;
                  }

                  buf.close();
                  return;
               }

               BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath());

               try {
                  this.create(fileWriter);
               } catch (Throwable var29) {
                  if (fileWriter != null) {
                     try {
                        fileWriter.close();
                     } catch (Throwable var21) {
                        var29.addSuppressed(var21);
                     }
                  }

                  throw var29;
               }

               if (fileWriter != null) {
                  fileWriter.close();
               }
            }

            BufferedReader fileReader = Files.newBufferedReader(file.toPath());

            try {
               this.read(fileReader);
            } catch (Throwable var28) {
               if (fileReader != null) {
                  try {
                     fileReader.close();
                  } catch (Throwable var20) {
                     var28.addSuppressed(var20);
                  }
               }

               throw var28;
            }

            if (fileReader != null) {
               fileReader.close();
            }

         } catch (Exception ex) {
            throw new RuntimeException("Failed to syncLoad() " + file.getAbsolutePath(), ex);
         }
      } finally {
         this.fileLock.writeLock().unlock();
      }
   }

   public void syncSave() {
      File file = null;
      this.fileLock.readLock().lock();

      try {
         file = this.toLocalFile();
         BufferedWriter fileWriter = Files.newBufferedWriter(file.toPath());

         try {
            this.write(fileWriter);
         } catch (Throwable var11) {
            if (fileWriter != null) {
               try {
                  fileWriter.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (fileWriter != null) {
            fileWriter.close();
         }
      } catch (Exception ex) {
         throw new RuntimeException("Failed to syncSave() " + (file != null ? file.getAbsolutePath() : null), ex);
      } finally {
         this.fileLock.readLock().unlock();
      }

   }

   @Nonnull
   protected File toLocalFile() {
      return this.path.toFile();
   }
}
