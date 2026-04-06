package com.hypixel.hytale.builtin.asseteditor.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class AssetPathUtil {
   public static final String UNIX_FILE_SEPARATOR = "/";
   public static final String FILE_EXTENSION_JSON = ".json";
   public static final String DIR_SERVER = "Server";
   public static final String DIR_COMMON = "Common";
   public static final Path PATH_DIR_COMMON = Paths.get("Common");
   public static final Path PATH_DIR_SERVER = Paths.get("Server");
   public static final Path EMPTY_PATH = Path.of("");
   private static final Pattern INVALID_FILENAME_CHAR_REGEX = Pattern.compile("[<>:\"|?*/\\\\]");
   private static final String[] RESERVED_NAMES = new String[]{"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

   public static boolean isInvalidFileName(@Nonnull Path param0) {
      // $FF: Couldn't be decompiled
   }

   public static String removeInvalidFileNameChars(String name) {
      return INVALID_FILENAME_CHAR_REGEX.matcher(name).replaceAll("");
   }

   @Nonnull
   private static String getIdFromPath(@Nonnull Path path) {
      String fileName = path.getFileName().toString();
      int extensionIndex = fileName.lastIndexOf(46);
      return extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
   }
}
