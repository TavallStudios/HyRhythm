package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.config.ModConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.commands.PluginCommand;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.plugin.pending.PendingLoadJavaPlugin;
import com.hypixel.hytale.server.core.plugin.pending.PendingLoadPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginManager {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final Path MODS_PATH = Path.of("mods");
   @Nonnull
   public static final MetricsRegistry<PluginManager> METRICS_REGISTRY;
   private static PluginManager instance;
   @Nonnull
   private final PluginClassLoader corePluginClassLoader = new PluginClassLoader(this, (PluginIdentifier)null, true, new URL[0]);
   @Nonnull
   private final List<PendingLoadPlugin> corePlugins = new ObjectArrayList();
   private final PluginBridgeClassLoader bridgeClassLoader = new PluginBridgeClassLoader(this, PluginManager.class.getClassLoader());
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final Map<PluginIdentifier, PluginBase> plugins = new Object2ObjectLinkedOpenHashMap();
   private final Map<Path, PluginClassLoader> classLoaders = new ConcurrentHashMap();
   private boolean hasOutdatedPlugins = false;
   private final boolean loadExternalPlugins = true;
   @Nonnull
   private PluginState state;
   @Nullable
   private List<PendingLoadPlugin> loadOrder;
   @Nullable
   private Map<PluginIdentifier, PluginBase> loading;
   @Nonnull
   private final Map<PluginIdentifier, PluginManifest> availablePlugins;
   public PluginListPageManager pluginListPageManager;
   private ComponentType<EntityStore, PluginListPageManager.SessionSettings> sessionSettingsComponentType;

   public static PluginManager get() {
      return instance;
   }

   public PluginManager() {
      this.state = PluginState.NONE;
      this.availablePlugins = new Object2ObjectLinkedOpenHashMap();
      instance = this;
      this.pluginListPageManager = new PluginListPageManager();
   }

   public void registerCorePlugin(@Nonnull PluginManifest builder) {
      this.corePlugins.add(new PendingLoadJavaPlugin((Path)null, builder, this.corePluginClassLoader));
   }

   private boolean canLoadOnBoot(@Nonnull PendingLoadPlugin plugin) {
      PluginIdentifier identifier = plugin.getIdentifier();
      PluginManifest manifest = plugin.getManifest();
      ModConfig modConfig = (ModConfig)HytaleServer.get().getConfig().getModConfig().get(identifier);
      boolean enabled;
      if (modConfig != null && modConfig.getEnabled() != null) {
         enabled = modConfig.getEnabled();
      } else {
         HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
         enabled = !manifest.isDisabledByDefault() && (plugin.isInServerClassPath() || serverConfig.getDefaultModsEnabled());
      }

      if (enabled) {
         return true;
      } else {
         LOGGER.at(Level.WARNING).log("Skipping mod %s (Disabled by server config)", identifier);
         return false;
      }
   }

   public void setup() {
      if (this.state != PluginState.NONE) {
         throw new IllegalStateException("Expected PluginState.NONE but found " + String.valueOf(this.state));
      } else {
         this.state = PluginState.SETUP;
         CommandManager.get().registerSystemCommand(new PluginCommand());
         this.sessionSettingsComponentType = EntityStore.REGISTRY.registerComponent(PluginListPageManager.SessionSettings.class, PluginListPageManager.SessionSettings::new);
         HashMap<PluginIdentifier, PendingLoadPlugin> pending = new HashMap();
         this.availablePlugins.clear();
         LOGGER.at(Level.INFO).log("Loading pending core plugins!");

         for(int i = 0; i < this.corePlugins.size(); ++i) {
            PendingLoadPlugin plugin = (PendingLoadPlugin)this.corePlugins.get(i);
            LOGGER.at(Level.INFO).log("- %s", plugin.getIdentifier());
            if (this.canLoadOnBoot(plugin)) {
               loadPendingPlugin(pending, plugin);
            } else {
               this.availablePlugins.put(plugin.getIdentifier(), plugin.getManifest());
            }
         }

         Path self;
         try {
            self = Paths.get(PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         } catch (URISyntaxException e) {
            throw new RuntimeException(e);
         }

         this.loadPluginsFromDirectory(pending, self.getParent().resolve("builtin"), false, this.availablePlugins);
         this.loadPluginsInClasspath(pending, this.availablePlugins);
         this.loadPluginsFromDirectory(pending, MODS_PATH, !Options.getOptionSet().has(Options.BARE), this.availablePlugins);

         for(Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
            this.loadPluginsFromDirectory(pending, modsPath, false, this.availablePlugins);
         }

         this.lock.readLock().lock();

         try {
            this.plugins.keySet().forEach((key) -> {
               pending.remove(key);
               LOGGER.at(Level.WARNING).log("Skipping loading of %s because it is already loaded!", key);
            });
            Iterator<PendingLoadPlugin> iterator = pending.values().iterator();

            while(iterator.hasNext()) {
               PendingLoadPlugin pendingLoadPlugin = (PendingLoadPlugin)iterator.next();

               try {
                  this.validatePluginDeps(pendingLoadPlugin, pending);
               } catch (MissingPluginDependencyException e) {
                  LOGGER.at(Level.SEVERE).log(e.getMessage());
                  iterator.remove();
               }
            }
         } finally {
            this.lock.readLock().unlock();
         }

         if (this.hasOutdatedPlugins && System.getProperty("hytale.allow_outdated_mods") == null) {
            LOGGER.at(Level.SEVERE).log("One or more plugins are targeting a different server version. It is recommended to update these plugins to ensure compatibility.");
            HytaleServer.get().getEventBus().registerGlobal(AddPlayerToWorldEvent.class, (event) -> {
               PlayerRef playerRef = (PlayerRef)event.getHolder().getComponent(PlayerRef.getComponentType());
               Player player = (Player)event.getHolder().getComponent(Player.getComponentType());
               if (playerRef != null && player != null) {
                  if (player.hasPermission("hytale.mods.outdated.notify")) {
                     playerRef.sendMessage(Message.translation("server.pluginManager.outOfDatePlugins").color(Color.RED));
                  }
               }
            });
         }

         this.loadOrder = PendingLoadPlugin.calculateLoadOrder(pending);
         this.loading = new Object2ObjectOpenHashMap();
         pending.forEach((identifier, pendingLoad) -> this.availablePlugins.put(identifier, pendingLoad.getManifest()));
         ObjectArrayList<CompletableFuture<Void>> preLoadFutures = new ObjectArrayList();
         ObjectArrayList<PluginIdentifier> failedBootPlugins = new ObjectArrayList();
         this.lock.writeLock().lock();

         try {
            LOGGER.at(Level.FINE).log("Loading plugins!");

            for(PendingLoadPlugin pendingLoadPlugin : this.loadOrder) {
               LOGGER.at(Level.FINE).log("- %s", pendingLoadPlugin.getIdentifier());

               try {
                  PluginBase plugin = pendingLoadPlugin.load();
                  this.plugins.put(plugin.getIdentifier(), plugin);
                  this.loading.put(plugin.getIdentifier(), plugin);
                  CompletableFuture<Void> future = plugin.preLoad();
                  if (future != null) {
                     preLoadFutures.add(future);
                  }
               } catch (ClassNotFoundException e) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s. Failed to find main class!", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               } catch (NoSuchMethodException e) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s. Requires default constructor!", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               } catch (Throwable e) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               }
            }
         } finally {
            this.lock.writeLock().unlock();
         }

         if (!failedBootPlugins.isEmpty() && !Constants.shouldSkipModValidation()) {
            StringBuilder sb = new StringBuilder("Failed to boot the following plugins:\n");
            ObjectListIterator var41 = failedBootPlugins.iterator();

            while(var41.hasNext()) {
               PluginIdentifier failed = (PluginIdentifier)var41.next();
               sb.append(" - ").append(failed).append('\n');
            }

            HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
         } else {
            CompletableFuture.allOf((CompletableFuture[])preLoadFutures.toArray((x$0) -> new CompletableFuture[x$0])).join();
            boolean hasFailed = false;

            for(PendingLoadPlugin pendingPlugin : this.loadOrder) {
               PluginBase plugin = (PluginBase)this.loading.get(pendingPlugin.getIdentifier());
               if (plugin != null && !this.setup(plugin)) {
                  hasFailed = true;
               }
            }

            if (!Constants.shouldSkipModValidation() && hasFailed) {
               StringBuilder sb = new StringBuilder("Failed to setup the following plugins:\n");
               this.collectFailedPlugins(sb);
               HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
            } else {
               this.loading.values().removeIf((v) -> v.getState().isInactive());
            }
         }
      }
   }

   public void start() {
      if (this.state != PluginState.SETUP) {
         throw new IllegalStateException("Expected PluginState.SETUP but found " + String.valueOf(this.state));
      } else {
         this.state = PluginState.START;
         boolean hasFailed = false;

         for(PendingLoadPlugin pendingPlugin : this.loadOrder) {
            PluginBase plugin = (PluginBase)this.loading.get(pendingPlugin.getIdentifier());
            if (plugin != null && !this.start(plugin)) {
               hasFailed = true;
            }
         }

         StringBuilder sb = new StringBuilder();

         for(Map.Entry<PluginIdentifier, ModConfig> entry : HytaleServer.get().getConfig().getModConfig().entrySet()) {
            PluginIdentifier identifier = (PluginIdentifier)entry.getKey();
            ModConfig modConfig = (ModConfig)entry.getValue();
            SemverRange requiredVersion = modConfig.getRequiredVersion();
            if (requiredVersion != null && !this.hasPlugin(identifier, requiredVersion)) {
               sb.append(String.format("%s, Version: %s\n", identifier, modConfig));
            }
         }

         if (!sb.isEmpty()) {
            String msg = "Failed to start server! Missing Mods:\n" + String.valueOf(sb);
            LOGGER.at(Level.SEVERE).log(msg);
            HytaleServer.get().shutdownServer(ShutdownReason.MISSING_REQUIRED_PLUGIN.withMessage(msg));
         } else if (hasFailed && !Constants.shouldSkipModValidation()) {
            sb = new StringBuilder("Failed to start the following plugins:\n");
            this.collectFailedPlugins(sb);
            HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
         } else {
            this.loadOrder = null;
            this.loading = null;
         }
      }
   }

   private void collectFailedPlugins(StringBuilder sb) {
      if (this.loading != null) {
         for(Map.Entry<PluginIdentifier, PluginBase> failed : this.loading.entrySet()) {
            if (((PluginBase)failed.getValue()).getState() == PluginState.FAILED) {
               Throwable reasonThrowable = ((PluginBase)failed.getValue()).getFailureCause();
               String reason = reasonThrowable != null ? reasonThrowable.toString() : "Unknown";
               sb.append(" - ").append(failed.getKey()).append(": ").append(reason).append('\n');
            }
         }

      }
   }

   public void shutdown() {
      this.state = PluginState.SHUTDOWN;
      LOGGER.at(Level.INFO).log("Saving plugins config...");
      this.lock.writeLock().lock();

      try {
         List<PluginBase> list = new ObjectArrayList(this.plugins.values());

         for(int i = list.size() - 1; i >= 0; --i) {
            PluginBase plugin = (PluginBase)list.get(i);
            if (plugin.getState() == PluginState.ENABLED) {
               LOGGER.at(Level.FINE).log("Shutting down %s %s", plugin.getType().getDisplayName(), plugin.getIdentifier());
               plugin.shutdown0(true);
               HytaleServer.get().doneStop(plugin);
               LOGGER.at(Level.INFO).log("Shut down plugin %s", plugin.getIdentifier());
            }
         }

         this.plugins.clear();
      } finally {
         this.lock.writeLock().unlock();
      }

   }

   @Nonnull
   public PluginState getState() {
      return this.state;
   }

   @Nonnull
   public PluginBridgeClassLoader getBridgeClassLoader() {
      return this.bridgeClassLoader;
   }

   private void validatePluginDeps(@Nonnull PendingLoadPlugin pendingLoadPlugin, @Nullable Map<PluginIdentifier, PendingLoadPlugin> pending) {
      String serverVersion = ManifestUtil.getVersion();
      if (!pendingLoadPlugin.getManifest().getGroup().equals("Hytale")) {
         String targetServerVersion = pendingLoadPlugin.getManifest().getServerVersion();
         if (targetServerVersion == null || serverVersion != null && !targetServerVersion.equals(serverVersion)) {
            if (targetServerVersion != null && !"*".equals(targetServerVersion)) {
               LOGGER.at(Level.WARNING).log("Plugin '%s' targets a different server version %s. You may encounter issues, please check for plugin updates.", pendingLoadPlugin.getIdentifier(), serverVersion);
            } else {
               LOGGER.at(Level.WARNING).log("Plugin '%s' does not specify a target server version. You may encounter issues, please check for plugin updates. This will be a hard error in the future", pendingLoadPlugin.getIdentifier());
            }

            this.hasOutdatedPlugins = true;
         }
      }

      for(Map.Entry<PluginIdentifier, SemverRange> entry : pendingLoadPlugin.getManifest().getDependencies().entrySet()) {
         PluginIdentifier identifier = (PluginIdentifier)entry.getKey();
         PluginManifest dependency = null;
         if (pending != null) {
            PendingLoadPlugin pendingDependency = (PendingLoadPlugin)pending.get(identifier);
            if (pendingDependency != null) {
               dependency = pendingDependency.getManifest();
            }
         }

         if (dependency == null) {
            PluginBase loadedBase = (PluginBase)this.plugins.get(identifier);
            if (loadedBase != null) {
               dependency = loadedBase.getManifest();
            }
         }

         if (dependency == null) {
            throw new MissingPluginDependencyException(String.format("Failed to load '%s' because the dependency '%s' could not be found!", pendingLoadPlugin.getIdentifier(), identifier));
         }

         SemverRange expectedVersion = (SemverRange)entry.getValue();
         if (!dependency.getVersion().satisfies(expectedVersion)) {
            throw new MissingPluginDependencyException(String.format("Failed to load '%s' because version of dependency '%s'(%s) does not satisfy '%s'!", pendingLoadPlugin.getIdentifier(), identifier, dependency.getVersion(), expectedVersion));
         }
      }

   }

   private void loadPluginsFromDirectory(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending, @Nonnull Path path, boolean create, @Nonnull Map<PluginIdentifier, PluginManifest> bootRejectMap) {
      if (!Files.isDirectory(path, new LinkOption[0])) {
         if (create) {
            try {
               Files.createDirectories(path);
            } catch (IOException e) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to create directory: %s", path);
            }
         }

      } else {
         LOGGER.at(Level.INFO).log("Loading pending plugins from directory: " + String.valueOf(path));

         try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);

            try {
               for(Path file : stream) {
                  if (Files.isRegularFile(file, new LinkOption[0]) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                     PendingLoadJavaPlugin plugin = this.loadPendingJavaPlugin(file);
                     if (plugin != null) {
                        assert plugin.getPath() != null;

                        LOGGER.at(Level.INFO).log("- %s from path %s", plugin.getIdentifier(), path.relativize(plugin.getPath()));
                        if (this.canLoadOnBoot(plugin)) {
                           loadPendingPlugin(pending, plugin);
                        } else {
                           bootRejectMap.put(plugin.getIdentifier(), plugin.getManifest());
                        }
                     }
                  }
               }
            } catch (Throwable var11) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to find pending plugins from: %s", path);
         }

      }
   }

   @Nullable
   private PendingLoadJavaPlugin loadPendingJavaPlugin(@Nonnull Path file) {
      try {
         FileSystem fs = FileSystems.newFileSystem(file);

         Object var19;
         label121: {
            Object var10;
            label122: {
               PendingLoadJavaPlugin var22;
               try {
                  InputStream stream;
                  label128: {
                     Path resource = fs.getPath("manifest.json");
                     if (!Files.exists(resource, new LinkOption[0])) {
                        LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to load manifest file!", file.toString());
                        var19 = null;
                        break label121;
                     }

                     stream = Files.newInputStream(resource);

                     try {
                        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                        label102: {
                           try {
                              char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                              RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                              ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                              manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                              if (manifest == null) {
                                 LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to decode manifest file!", file.toString());
                                 var10 = null;
                                 break label102;
                              }

                              extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                           } catch (Throwable var14) {
                              try {
                                 reader.close();
                              } catch (Throwable var13) {
                                 var14.addSuppressed(var13);
                              }

                              throw var14;
                           }

                           reader.close();
                           break label128;
                        }

                        reader.close();
                     } catch (Throwable var15) {
                        if (stream != null) {
                           try {
                              stream.close();
                           } catch (Throwable var12) {
                              var15.addSuppressed(var12);
                           }
                        }

                        throw var15;
                     }

                     if (stream != null) {
                        stream.close();
                     }
                     break label122;
                  }

                  if (stream != null) {
                     stream.close();
                  }

                  URL url = file.toUri().toURL();
                  PluginClassLoader pluginClassLoader = (PluginClassLoader)this.classLoaders.computeIfAbsent(file, (path) -> new PluginClassLoader(this, new PluginIdentifier(manifest), false, new URL[]{url}));
                  var22 = new PendingLoadJavaPlugin(file, manifest, pluginClassLoader);
               } catch (Throwable var16) {
                  if (fs != null) {
                     try {
                        fs.close();
                     } catch (Throwable var11) {
                        var16.addSuppressed(var11);
                     }
                  }

                  throw var16;
               }

               if (fs != null) {
                  fs.close();
               }

               return var22;
            }

            if (fs != null) {
               fs.close();
            }

            return (PendingLoadJavaPlugin)var10;
         }

         if (fs != null) {
            fs.close();
         }

         return (PendingLoadJavaPlugin)var19;
      } catch (MalformedURLException e) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load pending plugin from '%s'. Failed to create URLClassLoader!", file.toString());
      } catch (IOException e) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load pending plugin %s. Failed to load manifest file!", file.toString());
      }

      return null;
   }

   private void loadPluginsInClasspath(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending, @Nonnull Map<PluginIdentifier, PluginManifest> rejectedBootList) {
      LOGGER.at(Level.INFO).log("Loading pending classpath plugins!");

      try {
         URI uri = PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
         ClassLoader classLoader = PluginManager.class.getClassLoader();

         try {
            for(URL manifestUrl : new HashSet(Collections.list(classLoader.getResources("manifest.json")))) {
               URLConnection connection = manifestUrl.openConnection();
               InputStream stream = connection.getInputStream();

               label129: {
                  try {
                     InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                     label125: {
                        try {
                           char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                           RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                           ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                           PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                           extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                           if (manifest != null) {
                              PendingLoadJavaPlugin plugin;
                              if (connection instanceof JarURLConnection) {
                                 JarURLConnection jarURLConnection = (JarURLConnection)connection;
                                 URL classpathUrl = jarURLConnection.getJarFileURL();
                                 Path path = Path.of(classpathUrl.toURI());
                                 PluginClassLoader pluginClassLoader = (PluginClassLoader)this.classLoaders.computeIfAbsent(path, (f) -> new PluginClassLoader(this, new PluginIdentifier(manifest), true, new URL[]{classpathUrl}));
                                 plugin = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                              } else {
                                 URI pluginUri = manifestUrl.toURI().resolve(".");
                                 Path path = Paths.get(pluginUri);
                                 URL classpathUrl = pluginUri.toURL();
                                 PluginClassLoader pluginClassLoader = (PluginClassLoader)this.classLoaders.computeIfAbsent(path, (f) -> new PluginClassLoader(this, new PluginIdentifier(manifest), true, new URL[]{classpathUrl}));
                                 plugin = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                              }

                              LOGGER.at(Level.INFO).log("- %s", plugin.getIdentifier());
                              if (this.canLoadOnBoot(plugin)) {
                                 loadPendingPlugin(pending, plugin);
                              } else {
                                 rejectedBootList.put(plugin.getIdentifier(), plugin.getManifest());
                              }
                              break label125;
                           }

                           LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to decode manifest file!", manifestUrl);
                        } catch (Throwable var27) {
                           try {
                              reader.close();
                           } catch (Throwable var24) {
                              var27.addSuppressed(var24);
                           }

                           throw var27;
                        }

                        reader.close();
                        break label129;
                     }

                     reader.close();
                  } catch (Throwable var28) {
                     if (stream != null) {
                        try {
                           stream.close();
                        } catch (Throwable var23) {
                           var28.addSuppressed(var23);
                        }
                     }

                     throw var28;
                  }

                  if (stream != null) {
                     stream.close();
                  }
                  continue;
               }

               if (stream != null) {
                  stream.close();
               }

               return;
            }

            URL manifestsUrl = classLoader.getResource("manifests.json");
            if (manifestsUrl != null) {
               InputStream stream = manifestsUrl.openStream();

               try {
                  InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                  try {
                     char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                     RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                     ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                     PluginManifest[] manifests = PluginManifest.ARRAY_CODEC.decodeJson(rawJsonReader, extraInfo);
                     extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                     URL url = uri.toURL();
                     Path path = Paths.get(uri);
                     PluginClassLoader pluginClassLoader = (PluginClassLoader)this.classLoaders.computeIfAbsent(path, (f) -> new PluginClassLoader(this, (PluginIdentifier)null, true, new URL[]{url}));

                     for(PluginManifest manifest : manifests) {
                        PendingLoadJavaPlugin plugin = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                        LOGGER.at(Level.INFO).log("- %s", plugin.getIdentifier());
                        if (this.canLoadOnBoot(plugin)) {
                           loadPendingPlugin(pending, plugin);
                        } else {
                           rejectedBootList.put(plugin.getIdentifier(), plugin.getManifest());
                        }
                     }
                  } catch (Throwable var25) {
                     try {
                        reader.close();
                     } catch (Throwable var22) {
                        var25.addSuppressed(var22);
                     }

                     throw var25;
                  }

                  reader.close();
               } catch (Throwable var26) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var21) {
                        var26.addSuppressed(var21);
                     }
                  }

                  throw var26;
               }

               if (stream != null) {
                  stream.close();
               }
            }
         } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load pending classpath plugin from '%s'. Failed to load manifest file!", uri.toString());
         }
      } catch (URISyntaxException e) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to get jar path!");
      }

   }

   @Nonnull
   public List<PluginBase> getPlugins() {
      this.lock.readLock().lock();

      ObjectArrayList var1;
      try {
         var1 = new ObjectArrayList(this.plugins.values());
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }

   @Nullable
   public PluginBase getPlugin(PluginIdentifier identifier) {
      this.lock.readLock().lock();

      PluginBase var2;
      try {
         var2 = (PluginBase)this.plugins.get(identifier);
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   public boolean hasPlugin(PluginIdentifier identifier, @Nonnull SemverRange range) {
      PluginBase plugin = this.getPlugin(identifier);
      return plugin != null && plugin.getManifest().getVersion().satisfies(range);
   }

   public boolean reload(@Nonnull PluginIdentifier identifier) {
      boolean result = this.unload(identifier) && this.load(identifier);
      this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
      return result;
   }

   public boolean unload(@Nonnull PluginIdentifier identifier) {
      this.lock.writeLock().lock();
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      boolean var7;
      try {
         PluginBase plugin = (PluginBase)this.plugins.get(identifier);
         if (plugin.getState() != PluginState.ENABLED) {
            this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
            var7 = false;
            return var7;
         }

         plugin.shutdown0(false);
         HytaleServer.get().doneStop(plugin);
         this.plugins.remove(identifier);
         if (plugin instanceof JavaPlugin javaPlugin) {
            this.unloadJavaPlugin(javaPlugin);
         }

         this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
         var7 = true;
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
         this.lock.writeLock().unlock();
      }

      return var7;
   }

   protected void unloadJavaPlugin(JavaPlugin plugin) {
      Path path = plugin.getFile();
      PluginClassLoader classLoader = (PluginClassLoader)this.classLoaders.remove(path);
      if (classLoader != null) {
         try {
            classLoader.close();
         } catch (IOException var5) {
            LOGGER.at(Level.SEVERE).log("Failed to close Class Loader for JavaPlugin %s", plugin.getIdentifier());
         }
      }

   }

   public boolean load(@Nonnull PluginIdentifier identifier) {
      this.lock.readLock().lock();

      label38: {
         boolean var3;
         try {
            PluginBase plugin = (PluginBase)this.plugins.get(identifier);
            if (plugin == null) {
               break label38;
            }

            this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
            var3 = false;
         } finally {
            this.lock.readLock().unlock();
         }

         return var3;
      }

      boolean result = this.findAndLoadPlugin(identifier);
      this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
      return result;
   }

   private boolean findAndLoadPlugin(PluginIdentifier identifier) {
      for(PendingLoadPlugin plugin : this.corePlugins) {
         if (plugin.getIdentifier().equals(identifier)) {
            return this.load(plugin);
         }
      }

      try {
         URI uri = PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
         ClassLoader classLoader = PluginManager.class.getClassLoader();

         for(URL manifestUrl : new HashSet(Collections.list(classLoader.getResources("manifest.json")))) {
            InputStream stream = manifestUrl.openStream();

            boolean var15;
            label164: {
               try {
                  InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                  label160: {
                     try {
                        char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                        RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                        ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                        PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                        extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                        if (!(new PluginIdentifier(manifest)).equals(identifier)) {
                           break label160;
                        }

                        PluginClassLoader pluginClassLoader = new PluginClassLoader(this, identifier, true, new URL[]{uri.toURL()});
                        PendingLoadJavaPlugin plugin = new PendingLoadJavaPlugin(Paths.get(uri), manifest, pluginClassLoader);
                        var15 = this.load((PendingLoadPlugin)plugin);
                     } catch (Throwable var24) {
                        try {
                           reader.close();
                        } catch (Throwable var22) {
                           var24.addSuppressed(var22);
                        }

                        throw var24;
                     }

                     reader.close();
                     break label164;
                  }

                  reader.close();
               } catch (Throwable var25) {
                  if (stream != null) {
                     try {
                        stream.close();
                     } catch (Throwable var21) {
                        var25.addSuppressed(var21);
                     }
                  }

                  throw var25;
               }

               if (stream != null) {
                  stream.close();
               }
               continue;
            }

            if (stream != null) {
               stream.close();
            }

            return var15;
         }

         URL manifestsUrl = classLoader.getResource("manifests.json");
         if (manifestsUrl != null) {
            label233: {
               InputStream stream = manifestsUrl.openStream();

               boolean var18;
               label191: {
                  try {
                     InputStreamReader reader;
                     label229: {
                        reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                        try {
                           char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                           RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                           ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                           PluginManifest[] manifests = PluginManifest.ARRAY_CODEC.decodeJson(rawJsonReader, extraInfo);
                           extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                           PluginManifest[] var50 = manifests;
                           int var52 = manifests.length;
                           int var53 = 0;

                           while(true) {
                              if (var53 >= var52) {
                                 break label229;
                              }

                              PluginManifest manifest = var50[var53];
                              if ((new PluginIdentifier(manifest)).equals(identifier)) {
                                 PluginClassLoader pluginClassLoader = new PluginClassLoader(this, identifier, true, new URL[]{uri.toURL()});
                                 PendingLoadJavaPlugin plugin = new PendingLoadJavaPlugin(Paths.get(uri), manifest, pluginClassLoader);
                                 var18 = this.load((PendingLoadPlugin)plugin);
                                 break;
                              }

                              ++var53;
                           }
                        } catch (Throwable var26) {
                           try {
                              reader.close();
                           } catch (Throwable var20) {
                              var26.addSuppressed(var20);
                           }

                           throw var26;
                        }

                        reader.close();
                        break label191;
                     }

                     reader.close();
                  } catch (Throwable var27) {
                     if (stream != null) {
                        try {
                           stream.close();
                        } catch (Throwable var19) {
                           var27.addSuppressed(var19);
                        }
                     }

                     throw var27;
                  }

                  if (stream != null) {
                     stream.close();
                  }
                  break label233;
               }

               if (stream != null) {
                  stream.close();
               }

               return var18;
            }
         }

         Path path = Paths.get(uri).getParent().resolve("builtin");
         if (Files.exists(path, new LinkOption[0])) {
            try {
               label232: {
                  DirectoryStream<Path> stream = Files.newDirectoryStream(path);

                  boolean var51;
                  label210: {
                     try {
                        for(Path file : stream) {
                           if (Files.isRegularFile(file, new LinkOption[0]) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                              PluginManifest manifest = loadManifest(file);
                              if (manifest != null && (new PluginIdentifier(manifest)).equals(identifier)) {
                                 PendingLoadJavaPlugin pendingLoadJavaPlugin = this.loadPendingJavaPlugin(file);
                                 if (pendingLoadJavaPlugin != null) {
                                    var51 = this.load((PendingLoadPlugin)pendingLoadJavaPlugin);
                                    break label210;
                                 }
                                 break;
                              }
                           }
                        }
                     } catch (Throwable var28) {
                        if (stream != null) {
                           try {
                              stream.close();
                           } catch (Throwable var23) {
                              var28.addSuppressed(var23);
                           }
                        }

                        throw var28;
                     }

                     if (stream != null) {
                        stream.close();
                     }
                     break label232;
                  }

                  if (stream != null) {
                     stream.close();
                  }

                  return var51;
               }
            } catch (IOException e) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to find plugins!");
            }
         }
      } catch (URISyntaxException | IOException e) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load pending classpath plugin. Failed to load manifest file!");
      }

      Boolean result = this.findPluginInDirectory(identifier, MODS_PATH);
      if (result != null) {
         return result;
      } else {
         for(Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
            result = this.findPluginInDirectory(identifier, modsPath);
            if (result != null) {
               return result;
            }
         }

         return false;
      }
   }

   @Nullable
   private Boolean findPluginInDirectory(@Nonnull PluginIdentifier identifier, @Nonnull Path modsPath) {
      if (!Files.isDirectory(modsPath, new LinkOption[0])) {
         return null;
      } else {
         try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(modsPath);

            Boolean var8;
            label79: {
               label85: {
                  try {
                     for(Path file : stream) {
                        if (Files.isRegularFile(file, new LinkOption[0]) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                           PluginManifest manifest = loadManifest(file);
                           if (manifest != null && (new PluginIdentifier(manifest)).equals(identifier)) {
                              PendingLoadJavaPlugin pendingLoadJavaPlugin = this.loadPendingJavaPlugin(file);
                              if (pendingLoadJavaPlugin != null) {
                                 var8 = this.load((PendingLoadPlugin)pendingLoadJavaPlugin);
                                 break label85;
                              }

                              var8 = false;
                              break label79;
                           }
                        }
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

                  return null;
               }

               if (stream != null) {
                  stream.close();
               }

               return var8;
            }

            if (stream != null) {
               stream.close();
            }

            return var8;
         } catch (IOException e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to find plugins in %s!", modsPath);
            return null;
         }
      }
   }

   @Nullable
   private static PluginManifest loadManifest(@Nonnull Path file) {
      try {
         URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toUri().toURL()}, PluginManager.class.getClassLoader());

         PluginManifest var8;
         try {
            InputStream stream = urlClassLoader.findResource("manifest.json").openStream();

            try {
               InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

               try {
                  char[] buffer = (char[])RawJsonReader.READ_BUFFER.get();
                  RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                  ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
                  PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                  var8 = manifest;
               } catch (Throwable var12) {
                  try {
                     reader.close();
                  } catch (Throwable var11) {
                     var12.addSuppressed(var11);
                  }

                  throw var12;
               }

               reader.close();
            } catch (Throwable var13) {
               if (stream != null) {
                  try {
                     stream.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (stream != null) {
               stream.close();
            }
         } catch (Throwable var14) {
            try {
               urlClassLoader.close();
            } catch (Throwable var9) {
               var14.addSuppressed(var9);
            }

            throw var14;
         }

         urlClassLoader.close();
         return var8;
      } catch (IOException e) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load manifest %s.", file);
         return null;
      }
   }

   private boolean load(@Nullable PendingLoadPlugin pendingLoadPlugin) {
      if (pendingLoadPlugin == null) {
         return false;
      } else {
         try {
            this.validatePluginDeps(pendingLoadPlugin, (Map)null);
            PluginBase plugin = pendingLoadPlugin.load();
            this.lock.writeLock().lock();

            try {
               this.plugins.put(plugin.getIdentifier(), plugin);
            } finally {
               this.lock.writeLock().unlock();
            }

            CompletableFuture<Void> preload = plugin.preLoad();
            if (preload == null) {
               boolean result = this.setup(plugin) && this.start(plugin);
               this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               return result;
            }

            preload.thenAccept((v) -> {
               if (!this.setup(plugin)) {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               } else if (!this.start(plugin)) {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               } else {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               }
            });
            this.pluginListPageManager.notifyPluginChange(this.plugins, pendingLoadPlugin.getIdentifier());
         } catch (ClassNotFoundException e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s. Failed to find main class!", pendingLoadPlugin.getPath());
         } catch (NoSuchMethodException e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s. Requires default constructor!", pendingLoadPlugin.getPath());
         } catch (Throwable e) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(e)).log("Failed to load plugin %s", pendingLoadPlugin.getPath());
         }

         return false;
      }
   }

   private boolean setup(@Nonnull PluginBase plugin) {
      if (plugin.getState() == PluginState.NONE && this.dependenciesMatchState(plugin, PluginState.SETUP, PluginState.SETUP)) {
         LOGGER.at(Level.FINE).log("Setting up plugin %s", plugin.getIdentifier());
         boolean prev = AssetStore.DISABLE_DYNAMIC_DEPENDENCIES;
         AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = false;

         try {
            plugin.setup0();
         } finally {
            AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = prev;
         }

         AssetModule.get().initPendingStores();
         HytaleServer.get().doneSetup(plugin);
         if (!plugin.getState().isInactive()) {
            IEventDispatcher<PluginSetupEvent, PluginSetupEvent> dispatch = HytaleServer.get().getEventBus().dispatchFor(PluginSetupEvent.class, plugin.getClass());
            if (dispatch.hasListener()) {
               dispatch.dispatch(new PluginSetupEvent(plugin));
            }

            return true;
         }

         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      } else {
         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      }

      return false;
   }

   private boolean start(@Nonnull PluginBase plugin) {
      if (plugin.getState() == PluginState.SETUP && this.dependenciesMatchState(plugin, PluginState.ENABLED, PluginState.START)) {
         LOGGER.at(Level.FINE).log("Starting plugin %s", plugin.getIdentifier());
         plugin.start0();
         HytaleServer.get().doneStart(plugin);
         if (!plugin.getState().isInactive()) {
            LOGGER.at(Level.INFO).log("Enabled plugin %s", plugin.getIdentifier());
            return true;
         }

         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      } else {
         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      }

      return false;
   }

   private boolean dependenciesMatchState(PluginBase plugin, PluginState requiredState, PluginState stage) {
      for(PluginIdentifier dependencyOnManifest : plugin.getManifest().getDependencies().keySet()) {
         PluginBase dependency = (PluginBase)this.plugins.get(dependencyOnManifest);
         if (dependency == null || dependency.getState() != requiredState) {
            HytaleLogger.Api var10000 = LOGGER.at(Level.SEVERE);
            String var10001 = plugin.getName();
            var10000.log(var10001 + " is lacking dependency " + dependencyOnManifest.getName() + " at stage " + String.valueOf(stage));
            LOGGER.at(Level.SEVERE).log(plugin.getName() + " DISABLED!");
            plugin.setFailureCause(new Exception("Missing dependency " + dependencyOnManifest.getName()));
            return false;
         }
      }

      return true;
   }

   private static void loadPendingPlugin(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending, @Nonnull PendingLoadPlugin plugin) {
      if (pending.putIfAbsent(plugin.getIdentifier(), plugin) != null) {
         throw new IllegalArgumentException("Tried to load duplicate plugin: " + String.valueOf(plugin.getIdentifier()));
      } else {
         for(PendingLoadPlugin subPlugin : plugin.createSubPendingLoadPlugins()) {
            loadPendingPlugin(pending, subPlugin);
         }

      }
   }

   @Nonnull
   public Map<PluginIdentifier, PluginManifest> getAvailablePlugins() {
      return this.availablePlugins;
   }

   public ComponentType<EntityStore, PluginListPageManager.SessionSettings> getSessionSettingsComponentType() {
      return this.sessionSettingsComponentType;
   }

   static {
      METRICS_REGISTRY = (new MetricsRegistry()).register("Plugins", (pluginManager) -> (PluginBase[])pluginManager.getPlugins().toArray((x$0) -> new PluginBase[x$0]), new ArrayCodec(PluginBase.METRICS_REGISTRY, (x$0) -> new PluginBase[x$0]));
   }

   public static class PluginBridgeClassLoader extends ClassLoader {
      private final PluginManager pluginManager;

      public PluginBridgeClassLoader(PluginManager pluginManager, ClassLoader parent) {
         super(parent);
         this.pluginManager = pluginManager;
      }

      @Nonnull
      protected Class<?> loadClass(@Nonnull String name, boolean resolve) throws ClassNotFoundException {
         return this.loadClass0(name, (PluginClassLoader)null);
      }

      @Nonnull
      public Class<?> loadClass0(@Nonnull String name, PluginClassLoader pluginClassLoader) throws ClassNotFoundException {
         this.pluginManager.lock.readLock().lock();

         Class var7;
         try {
            Iterator var3 = this.pluginManager.plugins.entrySet().iterator();

            Class<?> loadClass;
            do {
               if (!var3.hasNext()) {
                  throw new ClassNotFoundException();
               }

               Map.Entry<PluginIdentifier, PluginBase> entry = (Map.Entry)var3.next();
               PluginBase pluginBase = (PluginBase)entry.getValue();
               loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
            } while(loadClass == null);

            var7 = loadClass;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return var7;
      }

      @Nonnull
      public Class<?> loadClass0(@Nonnull String name, PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) throws ClassNotFoundException {
         this.pluginManager.lock.readLock().lock();

         Class var8;
         try {
            Iterator var4 = manifest.getDependencies().keySet().iterator();

            Class<?> loadClass;
            do {
               if (!var4.hasNext()) {
                  for(PluginIdentifier pluginIdentifier : manifest.getOptionalDependencies().keySet()) {
                     if (!manifest.getDependencies().containsKey(pluginIdentifier)) {
                        PluginBase pluginBase = (PluginBase)this.pluginManager.plugins.get(pluginIdentifier);
                        if (pluginBase != null) {
                           loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
                           if (loadClass != null) {
                              var8 = loadClass;
                              return var8;
                           }
                        }
                     }
                  }

                  for(Map.Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
                     if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                        PluginBase pluginBase = (PluginBase)entry.getValue();
                        loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
                        if (loadClass != null) {
                           var8 = loadClass;
                           return var8;
                        }
                     }
                  }

                  throw new ClassNotFoundException();
               }

               PluginIdentifier pluginIdentifier = (PluginIdentifier)var4.next();
               PluginBase pluginBase = (PluginBase)this.pluginManager.plugins.get(pluginIdentifier);
               loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
            } while(loadClass == null);

            var8 = loadClass;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return var8;
      }

      public static Class<?> tryGetClass(@Nonnull String name, PluginClassLoader pluginClassLoader, PluginBase pluginBase) {
         if (!(pluginBase instanceof JavaPlugin)) {
            return null;
         } else {
            try {
               PluginClassLoader classLoader = ((JavaPlugin)pluginBase).getClassLoader();
               if (classLoader != pluginClassLoader) {
                  Class<?> loadClass = classLoader.loadLocalClass(name);
                  if (loadClass != null) {
                     return loadClass;
                  }
               }
            } catch (ClassNotFoundException var5) {
            }

            return null;
         }
      }

      @Nullable
      public URL getResource0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader) {
         this.pluginManager.lock.readLock().lock();

         URL var6;
         try {
            Iterator var3 = this.pluginManager.plugins.entrySet().iterator();

            URL resource;
            do {
               if (!var3.hasNext()) {
                  return null;
               }

               Map.Entry<PluginIdentifier, PluginBase> entry = (Map.Entry)var3.next();
               resource = tryGetResource(name, pluginClassLoader, (PluginBase)entry.getValue());
            } while(resource == null);

            var6 = resource;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return var6;
      }

      @Nullable
      public URL getResource0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) {
         this.pluginManager.lock.readLock().lock();

         URL resource;
         try {
            Iterator var4 = manifest.getDependencies().keySet().iterator();

            URL resource;
            do {
               if (!var4.hasNext()) {
                  for(PluginIdentifier pluginIdentifier : manifest.getOptionalDependencies().keySet()) {
                     if (!manifest.getDependencies().containsKey(pluginIdentifier)) {
                        PluginBase pluginBase = (PluginBase)this.pluginManager.plugins.get(pluginIdentifier);
                        if (pluginBase != null) {
                           resource = tryGetResource(name, pluginClassLoader, pluginBase);
                           if (resource != null) {
                              URL var8 = resource;
                              return var8;
                           }
                        }
                     }
                  }

                  for(Map.Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
                     if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                        resource = tryGetResource(name, pluginClassLoader, (PluginBase)entry.getValue());
                        if (resource != null) {
                           resource = resource;
                           return resource;
                        }
                     }
                  }

                  return null;
               }

               PluginIdentifier pluginIdentifier = (PluginIdentifier)var4.next();
               resource = tryGetResource(name, pluginClassLoader, (PluginBase)this.pluginManager.plugins.get(pluginIdentifier));
            } while(resource == null);

            resource = resource;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return resource;
      }

      @Nonnull
      public Enumeration<URL> getResources0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader) {
         ObjectArrayList<URL> results = new ObjectArrayList();
         this.pluginManager.lock.readLock().lock();

         try {
            for(Map.Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               URL resource = tryGetResource(name, pluginClassLoader, (PluginBase)entry.getValue());
               if (resource != null) {
                  results.add(resource);
               }
            }
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return Collections.enumeration(results);
      }

      @Nonnull
      public Enumeration<URL> getResources0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) {
         ObjectArrayList<URL> results = new ObjectArrayList();
         this.pluginManager.lock.readLock().lock();

         try {
            for(PluginIdentifier pluginIdentifier : manifest.getDependencies().keySet()) {
               URL resource = tryGetResource(name, pluginClassLoader, (PluginBase)this.pluginManager.plugins.get(pluginIdentifier));
               if (resource != null) {
                  results.add(resource);
               }
            }

            for(PluginIdentifier pluginIdentifier : manifest.getOptionalDependencies().keySet()) {
               if (!manifest.getDependencies().containsKey(pluginIdentifier)) {
                  PluginBase pluginBase = (PluginBase)this.pluginManager.plugins.get(pluginIdentifier);
                  if (pluginBase != null) {
                     URL resource = tryGetResource(name, pluginClassLoader, pluginBase);
                     if (resource != null) {
                        results.add(resource);
                     }
                  }
               }
            }

            for(Map.Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                  URL resource = tryGetResource(name, pluginClassLoader, (PluginBase)entry.getValue());
                  if (resource != null) {
                     results.add(resource);
                  }
               }
            }
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return Collections.enumeration(results);
      }

      @Nullable
      private static URL tryGetResource(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nullable PluginBase pluginBase) {
         if (pluginBase instanceof JavaPlugin javaPlugin) {
            PluginClassLoader classLoader = javaPlugin.getClassLoader();
            return classLoader != pluginClassLoader ? classLoader.findResource(name) : null;
         } else {
            return null;
         }
      }

      static {
         registerAsParallelCapable();
      }
   }
}
