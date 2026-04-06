package com.hypixel.hytale.plugin.early;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EarlyPluginLoader {
   @Nonnull
   public static final Path EARLY_PLUGINS_PATH = Path.of("earlyplugins");
   @Nonnull
   private static final List<ClassTransformer> transformers = new ObjectArrayList();
   @Nullable
   private static URLClassLoader pluginClassLoader;

   private EarlyPluginLoader() {
   }

   public static void loadEarlyPlugins(@Nonnull String[] args) {
      ObjectArrayList<URL> urls = new ObjectArrayList();
      collectPluginJars(EARLY_PLUGINS_PATH, urls);

      for(Path path : parseEarlyPluginPaths(args)) {
         collectPluginJars(path, urls);
      }

      if (!urls.isEmpty()) {
         pluginClassLoader = new URLClassLoader((URL[])urls.toArray((x$0) -> new URL[x$0]), EarlyPluginLoader.class.getClassLoader());

         for(ClassTransformer transformer : ServiceLoader.load(ClassTransformer.class, pluginClassLoader)) {
            PrintStream var10000 = System.out;
            String var10001 = transformer.getClass().getName();
            var10000.println("[EarlyPlugin] Loading transformer: " + var10001 + " (priority=" + transformer.priority() + ")");
            transformers.add(transformer);
         }

         transformers.sort(Comparator.comparingInt(ClassTransformer::priority).reversed());
         if (!transformers.isEmpty()) {
            System.err.printf("===============================================================================================\n                              Loaded %d class transformer(s)!!\n===============================================================================================\n                       This is unsupported and may cause stability issues.\n                                     Use at your own risk!!\n===============================================================================================\n", transformers.size());
            boolean isSingleplayer = hasFlag(args, "--singleplayer");
            boolean acceptEarlyPlugins = hasFlag(args, "--accept-early-plugins");
            if (!isSingleplayer && !acceptEarlyPlugins) {
               if (System.console() == null) {
                  System.err.println("ERROR: Early plugins require interactive confirmation, but no console is available.");
                  System.err.println("Pass --accept-early-plugins to accept the risk and continue.");
                  System.exit(1);
               }

               System.err.print("Press ENTER to accept and continue...");
               System.console().readLine();
            }
         }

      }
   }

   private static List<Path> parseEarlyPluginPaths(@Nonnull String[] args) {
      ObjectArrayList<Path> paths = new ObjectArrayList();

      for(int i = 0; i < args.length; ++i) {
         if (args[i].equals("--early-plugins") && i + 1 < args.length) {
            for(String pathStr : args[i + 1].split(",")) {
               paths.add(Path.of(pathStr.trim()));
            }
         } else if (args[i].startsWith("--early-plugins=")) {
            String value = args[i].substring("--early-plugins=".length());

            for(String pathStr : value.split(",")) {
               paths.add(Path.of(pathStr.trim()));
            }
         }
      }

      return paths;
   }

   private static boolean hasFlag(String[] args, String flag) {
      for(String arg : args) {
         if (arg.equals(flag)) {
            return true;
         }
      }

      return false;
   }

   private static void collectPluginJars(Path path, List<URL> urls) {
      if (Files.isDirectory(path, new LinkOption[0])) {
         try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.jar");

            try {
               for(Path file : stream) {
                  if (Files.isRegularFile(file, new LinkOption[0])) {
                     urls.add(file.toUri().toURL());
                     System.out.println("[EarlyPlugin] Found: " + String.valueOf(file.getFileName()));
                  }
               }
            } catch (Throwable var6) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException e) {
            PrintStream var10000 = System.err;
            String var10001 = String.valueOf(path);
            var10000.println("[EarlyPlugin] Failed to scan directory " + var10001 + ": " + e.getMessage());
         }

      }
   }

   public static boolean hasTransformers() {
      return !transformers.isEmpty();
   }

   public static List<ClassTransformer> getTransformers() {
      return Collections.unmodifiableList(transformers);
   }

   @Nullable
   public static URLClassLoader getPluginClassLoader() {
      return pluginClassLoader;
   }
}
