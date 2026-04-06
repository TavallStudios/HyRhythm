package com.hypixel.hytale.server.core;

import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.server.core.io.transport.TransportType;
import com.hypixel.hytale.server.core.universe.world.ValidationOption;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

public class Options {
   public static final OptionParser PARSER = new OptionParser();
   public static final OptionSpec<Void> HELP;
   public static final OptionSpec<Void> VERSION;
   public static final OptionSpec<Void> BARE;
   public static final OptionSpec<Map.Entry<String, Level>> LOG_LEVELS;
   public static final OptionSpec<InetSocketAddress> BIND;
   public static final OptionSpec<TransportType> TRANSPORT;
   public static final OptionSpec<Void> DISABLE_CPB_BUILD;
   public static final OptionSpec<Path> PREFAB_CACHE_DIRECTORY;
   public static final OptionSpec<Path> ASSET_DIRECTORY;
   public static final OptionSpec<Path> MODS_DIRECTORIES;
   public static final OptionSpec<Void> ACCEPT_EARLY_PLUGINS;
   public static final OptionSpec<Path> EARLY_PLUGIN_DIRECTORIES;
   public static final OptionSpec<Void> VALIDATE_ASSETS;
   public static final OptionSpec<ValidationOption> VALIDATE_PREFABS;
   public static final OptionSpec<Void> VALIDATE_WORLD_GEN;
   public static final OptionSpec<Void> SHUTDOWN_AFTER_VALIDATE;
   public static final OptionSpec<Void> GENERATE_SCHEMA;
   public static final OptionSpec<Path> WORLD_GEN_DIRECTORY;
   public static final OptionSpec<Void> DISABLE_FILE_WATCHER;
   public static final OptionSpec<Void> DISABLE_SENTRY;
   public static final OptionSpec<Void> DISABLE_ASSET_COMPARE;
   public static final OptionSpec<Void> BACKUP;
   public static final OptionSpec<Integer> BACKUP_FREQUENCY_MINUTES;
   public static final OptionSpec<Path> BACKUP_DIRECTORY;
   public static final OptionSpec<Integer> BACKUP_MAX_COUNT;
   public static final OptionSpec<Integer> BACKUP_ARCHIVE_MAX_COUNT;
   public static final OptionSpec<Void> SINGLEPLAYER;
   public static final OptionSpec<String> OWNER_NAME;
   public static final OptionSpec<UUID> OWNER_UUID;
   public static final OptionSpec<Integer> CLIENT_PID;
   public static final OptionSpec<Path> UNIVERSE;
   public static final OptionSpec<Void> EVENT_DEBUG;
   public static final OptionSpec<Boolean> FORCE_NETWORK_FLUSH;
   public static final OptionSpec<Map<String, Path>> MIGRATIONS;
   public static final OptionSpec<String> MIGRATE_WORLDS;
   public static final OptionSpec<String> BOOT_COMMAND;
   public static final OptionSpec<Void> SKIP_MOD_VALIDATION;
   public static final String ALLOW_SELF_OP_COMMAND_STRING = "allow-op";
   public static final OptionSpec<Void> ALLOW_SELF_OP_COMMAND;
   public static final OptionSpec<AuthMode> AUTH_MODE;
   public static final OptionSpec<String> SESSION_TOKEN;
   public static final OptionSpec<String> IDENTITY_TOKEN;
   private static OptionSet optionSet;

   public static OptionSet getOptionSet() {
      return optionSet;
   }

   public static <T> T getOrDefault(OptionSpec<T> optionSpec, @Nonnull OptionSet optionSet, T def) {
      return (T)(!optionSet.has(optionSpec) ? def : optionSet.valueOf(optionSpec));
   }

   public static boolean parse(String[] args) throws IOException {
      optionSet = PARSER.parse(args);
      if (optionSet.has(HELP)) {
         PARSER.printHelpOn(System.out);
         return true;
      } else if (optionSet.has(VERSION)) {
         String version = ManifestUtil.getImplementationVersion();
         String patchline = ManifestUtil.getPatchline();
         String environment = "release";
         if ("release".equals(patchline)) {
            System.out.println("HytaleServer v" + version + " (" + patchline + ")");
         } else {
            System.out.println("HytaleServer v" + version + " (" + patchline + ", " + environment + ")");
         }

         return true;
      } else {
         List<?> nonOptionArguments = optionSet.nonOptionArguments();
         if (!nonOptionArguments.isEmpty()) {
            System.err.println("Unknown arguments: " + String.valueOf(nonOptionArguments));
            System.exit(1);
            return true;
         } else {
            if (optionSet.has(LOG_LEVELS)) {
               HytaleLoggerBackend.loadLevels(optionSet.valuesOf(LOG_LEVELS));
            } else if (optionSet.has(SHUTDOWN_AFTER_VALIDATE)) {
               HytaleLoggerBackend.loadLevels(List.of(Map.entry("", Level.WARNING)));
            }

            for(Path path : optionSet.valuesOf(ASSET_DIRECTORY)) {
               PathUtil.addTrustedRoot(path);
            }

            for(Path path : optionSet.valuesOf(MODS_DIRECTORIES)) {
               PathUtil.addTrustedRoot(path);
            }

            for(Path path : optionSet.valuesOf(EARLY_PLUGIN_DIRECTORIES)) {
               PathUtil.addTrustedRoot(path);
            }

            if (optionSet.has(WORLD_GEN_DIRECTORY)) {
               PathUtil.addTrustedRoot((Path)optionSet.valueOf(WORLD_GEN_DIRECTORY));
            }

            if (optionSet.has(BACKUP_DIRECTORY)) {
               PathUtil.addTrustedRoot((Path)optionSet.valueOf(BACKUP_DIRECTORY));
            }

            if (optionSet.has(UNIVERSE)) {
               PathUtil.addTrustedRoot((Path)optionSet.valueOf(UNIVERSE));
            }

            return false;
         }
      }
   }

   static {
      HELP = PARSER.accepts("help", "Print's this message.").forHelp();
      VERSION = PARSER.accepts("version", "Prints version information.");
      BARE = PARSER.accepts("bare", "Runs the server bare. For example without loading worlds, binding to ports or creating directories. (Note: Plugins will still be loaded which may not respect this flag)");
      LOG_LEVELS = PARSER.accepts("log", "Sets the logger level.").withRequiredArg().withValuesSeparatedBy(',').withValuesConvertedBy(new LevelValueConverter());
      BIND = PARSER.acceptsAll(List.of("b", "bind"), "Port to listen on").withRequiredArg().withValuesSeparatedBy(',').withValuesConvertedBy(new SocketAddressValueConverter()).defaultsTo(new InetSocketAddress(5520), new InetSocketAddress[0]);
      TRANSPORT = PARSER.acceptsAll(List.of("t", "transport"), "Transport type").withRequiredArg().ofType(TransportType.class).defaultsTo(TransportType.QUIC, new TransportType[0]);
      DISABLE_CPB_BUILD = PARSER.accepts("disable-cpb-build", "Disables building of compact prefab buffers");
      PREFAB_CACHE_DIRECTORY = PARSER.accepts("prefab-cache", "Prefab cache directory for immutable assets").withRequiredArg().withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.ANY));
      ASSET_DIRECTORY = PARSER.acceptsAll(List.of("assets"), "Asset directory").withRequiredArg().withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR_OR_ZIP)).defaultsTo(Paths.get("../HytaleAssets"), new Path[0]);
      MODS_DIRECTORIES = PARSER.acceptsAll(List.of("mods"), "Additional mods directories").withRequiredArg().withValuesSeparatedBy(',').withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR));
      ACCEPT_EARLY_PLUGINS = PARSER.accepts("accept-early-plugins", "You acknowledge that loading early plugins is unsupported and may cause stability issues.");
      EARLY_PLUGIN_DIRECTORIES = PARSER.accepts("early-plugins", "Additional early plugin directories to load from").withRequiredArg().withValuesSeparatedBy(',').withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR));
      VALIDATE_ASSETS = PARSER.accepts("validate-assets", "Causes the server to exit with an error code if any assets are invalid.");
      VALIDATE_PREFABS = PARSER.accepts("validate-prefabs", "Causes the server to exit with an error code if any prefabs are invalid.").withOptionalArg().withValuesSeparatedBy(',').ofType(ValidationOption.class);
      VALIDATE_WORLD_GEN = PARSER.accepts("validate-world-gen", "Causes the server to exit with an error code if default world gen is invalid.");
      SHUTDOWN_AFTER_VALIDATE = PARSER.accepts("shutdown-after-validate", "Automatically shutdown the server after asset and/or prefab validation.");
      GENERATE_SCHEMA = PARSER.accepts("generate-schema", "Causes the server generate schema, save it into the assets directory and then exit");
      WORLD_GEN_DIRECTORY = PARSER.accepts("world-gen", "World gen directory").withRequiredArg().withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR));
      DISABLE_FILE_WATCHER = PARSER.accepts("disable-file-watcher");
      DISABLE_SENTRY = PARSER.accepts("disable-sentry");
      DISABLE_ASSET_COMPARE = PARSER.accepts("disable-asset-compare");
      BACKUP = PARSER.accepts("backup");
      BACKUP_FREQUENCY_MINUTES = PARSER.accepts("backup-frequency").withRequiredArg().ofType(Integer.class).defaultsTo(30, new Integer[0]);
      BACKUP_DIRECTORY = PARSER.accepts("backup-dir").requiredIf(BACKUP, new OptionSpec[0]).withRequiredArg().withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR));
      BACKUP_MAX_COUNT = PARSER.accepts("backup-max-count").withRequiredArg().ofType(Integer.class).defaultsTo(5, new Integer[0]);
      BACKUP_ARCHIVE_MAX_COUNT = PARSER.accepts("backup-archive-max-count").withRequiredArg().ofType(Integer.class).defaultsTo(5, new Integer[0]);
      SINGLEPLAYER = PARSER.accepts("singleplayer");
      OWNER_NAME = PARSER.accepts("owner-name").withRequiredArg();
      OWNER_UUID = PARSER.accepts("owner-uuid").withRequiredArg().withValuesConvertedBy(new UUIDConverter());
      CLIENT_PID = PARSER.accepts("client-pid").withRequiredArg().ofType(Integer.class);
      UNIVERSE = PARSER.accepts("universe").withRequiredArg().withValuesConvertedBy(new PathConverter(Options.PathConverter.PathType.DIR));
      EVENT_DEBUG = PARSER.accepts("event-debug");
      FORCE_NETWORK_FLUSH = PARSER.accepts("force-network-flush").withRequiredArg().ofType(Boolean.class).defaultsTo(true, new Boolean[0]);
      MIGRATIONS = PARSER.accepts("migrations", "The migrations to run").withRequiredArg().withValuesConvertedBy(new StringToPathMapConverter());
      MIGRATE_WORLDS = PARSER.accepts("migrate-worlds", "Worlds to migrate").availableIf(MIGRATIONS, new OptionSpec[0]).withRequiredArg().withValuesSeparatedBy(',');
      BOOT_COMMAND = PARSER.accepts("boot-command", "Runs command on boot. If multiple commands are provided they are executed synchronously in order.").withRequiredArg().withValuesSeparatedBy(',');
      SKIP_MOD_VALIDATION = PARSER.accepts("skip-mod-validation", "Skips mod validation, attempting to allow the server to boot even if one fails to load");
      ALLOW_SELF_OP_COMMAND = PARSER.accepts("allow-op");
      AUTH_MODE = PARSER.accepts("auth-mode", "Authentication mode").withRequiredArg().withValuesConvertedBy(new AuthModeConverter()).defaultsTo(Options.AuthMode.AUTHENTICATED, new AuthMode[0]);
      SESSION_TOKEN = PARSER.accepts("session-token", "Session token for Session Service API").withRequiredArg().ofType(String.class);
      IDENTITY_TOKEN = PARSER.accepts("identity-token", "Identity token (JWT)").withRequiredArg().ofType(String.class);
   }

   public static enum AuthMode {
      AUTHENTICATED,
      OFFLINE,
      INSECURE;
   }

   private static class AuthModeConverter implements ValueConverter<AuthMode> {
      public AuthMode convert(String value) {
         return Options.AuthMode.valueOf(value.toUpperCase());
      }

      public Class<? extends AuthMode> valueType() {
         return AuthMode.class;
      }

      public String valuePattern() {
         return "authenticated|offline|insecure";
      }
   }

   public static class UUIDConverter implements ValueConverter<UUID> {
      @Nonnull
      public UUID convert(@Nonnull String s) {
         return UUID.fromString(s);
      }

      @Nonnull
      public Class<? extends UUID> valueType() {
         return UUID.class;
      }

      @Nullable
      public String valuePattern() {
         return null;
      }
   }

   public static class LevelValueConverter implements ValueConverter<Map.Entry<String, Level>> {
      private static final Map.Entry<String, Level> ENTRY;

      @Nonnull
      public Map.Entry<String, Level> convert(@Nonnull String value) {
         if (!value.contains(":")) {
            return Map.entry("", Level.parse(value.toUpperCase()));
         } else {
            String[] split = value.split(":");
            return Map.entry(split[0], Level.parse(split[1].toUpperCase()));
         }
      }

      @Nonnull
      public Class<Map.Entry<String, Level>> valueType() {
         return ENTRY.getClass();
      }

      @Nullable
      public String valuePattern() {
         return null;
      }

      static {
         ENTRY = Map.entry("", Level.ALL);
      }
   }

   public static class PathConverter implements ValueConverter<Path> {
      private final PathType pathType;

      public PathConverter(PathType pathType) {
         this.pathType = pathType;
      }

      @Nonnull
      public Path convert(@Nonnull String s) {
         try {
            Path path = PathUtil.get(s);
            if (Files.exists(path, new LinkOption[0])) {
               switch (this.pathType.ordinal()) {
                  case 0:
                     if (!Files.isRegularFile(path, new LinkOption[0])) {
                        throw new ValueConversionException("Path must be a file!");
                     }
                     break;
                  case 1:
                     if (!Files.isDirectory(path, new LinkOption[0])) {
                        throw new ValueConversionException("Path must be a directory!");
                     }
                     break;
                  case 2:
                     if (!Files.isDirectory(path, new LinkOption[0]) && (!Files.exists(path, new LinkOption[0]) || !path.getFileName().toString().endsWith(".zip"))) {
                        throw new ValueConversionException("Path must be a directory or zip!");
                     }
               }
            }

            return path;
         } catch (InvalidPathException e) {
            throw new ValueConversionException("Failed to parse '" + s + "' to path!", e);
         }
      }

      @Nonnull
      public Class<? extends Path> valueType() {
         return Path.class;
      }

      @Nullable
      public String valuePattern() {
         return null;
      }

      public static enum PathType {
         FILE,
         DIR,
         DIR_OR_ZIP,
         ANY;
      }
   }

   public static class SocketAddressValueConverter implements ValueConverter<InetSocketAddress> {
      @Nonnull
      public InetSocketAddress convert(@Nonnull String value) {
         if (value.contains(":")) {
            String[] split = value.split(":");
            return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
         } else {
            try {
               return new InetSocketAddress(Integer.parseInt(value));
            } catch (NumberFormatException var3) {
               return new InetSocketAddress(value, 5520);
            }
         }
      }

      @Nonnull
      public Class<? extends InetSocketAddress> valueType() {
         return InetSocketAddress.class;
      }

      @Nullable
      public String valuePattern() {
         return null;
      }
   }

   public static class StringToPathMapConverter implements ValueConverter<Map<String, Path>> {
      private static final Map<String, Level> MAP = new Object2ObjectOpenHashMap();

      @Nonnull
      public Map<String, Path> convert(@Nonnull String value) {
         HashMap<String, Path> map = new HashMap();
         String[] strings = value.split(",");

         for(String string : strings) {
            String[] split = string.split("=");
            if (split.length == 2) {
               if (map.containsKey(split[0])) {
                  throw new ValueConversionException("String '" + split[0] + "' has already been specified!");
               }

               Path path = PathUtil.get(split[1]);
               if (!Files.exists(path, new LinkOption[0])) {
                  throw new ValueConversionException("No file found for '" + split[1] + "'!");
               }

               map.put(split[0], path);
            }
         }

         return map;
      }

      @Nonnull
      public Class<Map<String, Path>> valueType() {
         return MAP.getClass();
      }

      @Nullable
      public String valuePattern() {
         return null;
      }
   }
}
