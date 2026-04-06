package com.hypixel.hytale.server.core.plugin.pending;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginClassLoader;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PendingLoadJavaPlugin extends PendingLoadPlugin {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final PluginClassLoader urlClassLoader;

   public PendingLoadJavaPlugin(@Nullable Path path, @Nonnull PluginManifest manifest, @Nonnull PluginClassLoader urlClassLoader) {
      super(path, manifest);
      this.urlClassLoader = urlClassLoader;
   }

   @Nonnull
   public PendingLoadPlugin createSubPendingLoadPlugin(@Nonnull PluginManifest manifest) {
      return new PendingLoadJavaPlugin(this.getPath(), manifest, this.urlClassLoader);
   }

   public boolean isInServerClassPath() {
      return this.urlClassLoader.isInServerClassPath();
   }

   @Nonnull
   public JavaPlugin load() throws Exception {
      PluginManifest manifest = this.getManifest();
      Class<?> mainClass = this.urlClassLoader.loadLocalClass(manifest.getMain());
      if (JavaPlugin.class.isAssignableFrom(mainClass)) {
         Constructor<?> constructor = mainClass.getConstructor(JavaPluginInit.class);
         Path var10000 = PluginManager.MODS_PATH;
         String var10001 = manifest.getGroup();
         Path dataDirectory = var10000.resolve(var10001 + "_" + manifest.getName());
         JavaPluginInit init = new JavaPluginInit(manifest, dataDirectory, this.getPath(), this.urlClassLoader);
         return (JavaPlugin)constructor.newInstance(init);
      } else {
         throw new ClassCastException(manifest.getMain() + " does not extend JavaPlugin");
      }
   }

   @Nonnull
   public String toString() {
      return "PendingLoadJavaPlugin{" + super.toString() + "}";
   }
}
