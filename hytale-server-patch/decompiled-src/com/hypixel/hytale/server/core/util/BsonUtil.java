package com.hypixel.hytale.server.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.io.ByteBufUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import com.hypixel.hytale.sneakythrow.supplier.ThrowableSupplier;
import io.netty.buffer.ByteBuf;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

public class BsonUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final JsonWriterSettings SETTINGS;
   private static final BsonDocumentCodec codec;
   private static final DecoderContext decoderContext;
   private static final EncoderContext encoderContext;
   public static final BsonDocumentCodec BSON_DOCUMENT_CODEC;

   public static byte[] writeToBytes(@Nullable BsonDocument document) {
      if (document == null) {
         return ArrayUtil.EMPTY_BYTE_ARRAY;
      } else {
         BasicOutputBuffer buffer = new BasicOutputBuffer();

         byte[] var2;
         try {
            codec.encode(new BsonBinaryWriter(buffer), document, encoderContext);
            var2 = buffer.toByteArray();
         } catch (Throwable var5) {
            try {
               buffer.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         buffer.close();
         return var2;
      }
   }

   public static BsonDocument readFromBytes(@Nullable byte[] buf) {
      return buf != null && buf.length != 0 ? codec.decode(new BsonBinaryReader(ByteBuffer.wrap(buf)), decoderContext) : null;
   }

   public static BsonDocument readFromBuffer(@Nullable ByteBuffer buf) {
      return buf != null && buf.hasRemaining() ? codec.decode(new BsonBinaryReader(buf), decoderContext) : null;
   }

   public static BsonDocument readFromBinaryStream(@Nonnull ByteBuf buf) {
      return readFromBytes(ByteBufUtil.readByteArray(buf));
   }

   public static void writeToBinaryStream(@Nonnull ByteBuf buf, BsonDocument doc) {
      ByteBufUtil.writeByteArray(buf, writeToBytes(doc));
   }

   @Nonnull
   public static CompletableFuture<Void> writeDocument(@Nonnull Path file, BsonDocument document) {
      return writeDocument(file, document, true);
   }

   @Nonnull
   public static CompletableFuture<Void> writeDocument(@Nonnull Path file, BsonDocument document, boolean backup) {
      try {
         Path parent = PathUtil.getParent(file);
         if (!Files.exists(parent, new LinkOption[0])) {
            Files.createDirectories(parent);
         }

         String json = toJson(document);
         return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> FileUtil.writeStringAtomic(file, json, backup)));
      } catch (IOException e) {
         return CompletableFuture.failedFuture(e);
      }
   }

   @Nonnull
   public static CompletableFuture<BsonDocument> readDocument(@Nonnull Path file) {
      return readDocument(file, true);
   }

   @Nonnull
   public static CompletableFuture<BsonDocument> readDocument(@Nonnull Path file, boolean backup) {
      BasicFileAttributes attributes;
      try {
         attributes = Files.readAttributes(file, BasicFileAttributes.class);
      } catch (IOException var4) {
         if (backup) {
            return readDocumentBak(file);
         }

         return CompletableFuture.completedFuture((Object)null);
      }

      if (attributes.size() == 0L) {
         LOGGER.at(Level.WARNING).log("Error loading file %s, file was found to be entirely empty", file);
         return backup ? readDocumentBak(file) : CompletableFuture.completedFuture((Object)null);
      } else {
         CompletableFuture<BsonDocument> future = CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier((ThrowableSupplier)(() -> Files.readString(file)))).thenApply(BsonDocument::parse);
         return backup ? future.exceptionallyCompose((t) -> readDocumentBak(file)) : future;
      }
   }

   @Nullable
   public static BsonDocument readDocumentNow(@Nonnull Path file) {
      BasicFileAttributes attributes;
      try {
         attributes = Files.readAttributes(file, BasicFileAttributes.class);
      } catch (IOException e) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log(ExceptionUtil.toStringWithStack(e));
         return null;
      }

      if (attributes.size() == 0L) {
         return null;
      } else {
         String contentsString;
         try {
            contentsString = Files.readString(file);
         } catch (IOException var4) {
            return null;
         }

         return BsonDocument.parse(contentsString);
      }
   }

   @Nonnull
   public static CompletableFuture<BsonDocument> readDocumentBak(@Nonnull Path fileOrig) {
      Path file = fileOrig.resolveSibling(String.valueOf(fileOrig.getFileName()) + ".bak");

      BasicFileAttributes attributes;
      try {
         attributes = Files.readAttributes(file, BasicFileAttributes.class);
      } catch (IOException var4) {
         return CompletableFuture.completedFuture((Object)null);
      }

      if (attributes.size() == 0L) {
         LOGGER.at(Level.WARNING).log("Error loading backup file %s, file was found to be entirely empty", file);
         return CompletableFuture.completedFuture((Object)null);
      } else {
         LOGGER.at(Level.WARNING).log("Loading %s backup file for %s!", file, fileOrig);
         return CompletableFuture.supplyAsync(SneakyThrow.sneakySupplier((ThrowableSupplier)(() -> Files.readString(file)))).thenApply(BsonDocument::parse);
      }
   }

   public static BsonValue translateJsonToBson(@Nonnull JsonElement element) {
      return (BsonValue)(element.isJsonObject() ? BsonDocument.parse(element.toString()) : new BsonString(element.getAsString()));
   }

   public static JsonElement translateBsonToJson(BsonDocument value) {
      try {
         StringWriter writer = new StringWriter();

         JsonElement var2;
         try {
            codec.encode(new JsonWriter(writer, SETTINGS), value, encoderContext);
            var2 = JsonParser.parseString(writer.toString());
         } catch (Throwable var5) {
            try {
               writer.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         writer.close();
         return var2;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static String toJson(BsonDocument document) {
      StringWriter writer = new StringWriter();
      BSON_DOCUMENT_CODEC.encode(new JsonWriter(writer, SETTINGS), document, encoderContext);
      return writer.toString();
   }

   public static <T> void writeSync(@Nonnull Path path, @Nonnull Codec<T> codec, T value, @Nonnull HytaleLogger logger) throws IOException {
      Path parent = PathUtil.getParent(path);
      if (!Files.exists(parent, new LinkOption[0])) {
         Files.createDirectories(parent);
      }

      ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
      BsonValue bsonValue = codec.encode(value, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(logger);
      BsonDocument document = bsonValue.asDocument();
      Path tmpPath = path.resolveSibling(String.valueOf(path.getFileName()) + ".tmp");
      Path bakPath = path.resolveSibling(String.valueOf(path.getFileName()) + ".bak");
      BufferedWriter writer = Files.newBufferedWriter(tmpPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      try {
         BSON_DOCUMENT_CODEC.encode(new JsonWriter(writer, SETTINGS), document, encoderContext);
      } catch (Throwable var14) {
         if (writer != null) {
            try {
               writer.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }
         }

         throw var14;
      }

      if (writer != null) {
         writer.close();
      }

      if (Files.isRegularFile(path, new LinkOption[0])) {
         FileUtil.atomicMove(path, bakPath);
      }

      FileUtil.atomicMove(tmpPath, path);
   }

   static {
      SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.STRICT).indent(true).newLineCharacters("\n").int64Converter((value, writer) -> writer.writeNumber(Long.toString(value))).build();
      codec = new BsonDocumentCodec();
      decoderContext = DecoderContext.builder().build();
      encoderContext = EncoderContext.builder().build();
      BSON_DOCUMENT_CODEC = new BsonDocumentCodec();
   }
}
