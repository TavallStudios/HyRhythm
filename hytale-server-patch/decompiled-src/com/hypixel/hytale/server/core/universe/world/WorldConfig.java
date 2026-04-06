package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;
import com.hypixel.hytale.codec.lookup.MapKeyMapCodec;
import com.hypixel.hytale.codec.schema.metadata.NoDefaultValue;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.codec.ShapeCodecs;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.IResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.server.core.util.BsonUtil;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class WorldConfig {
   public static final int VERSION = 4;
   public static final int INITIAL_GAME_DAY_START_HOUR = 5;
   public static final int INITIAL_GAME_DAY_START_MINS = 30;
   public static final MapKeyMapCodec<Object> PLUGIN_CODEC = new MapKeyMapCodec<Object>(false);
   public static final BuilderCodec<WorldConfig> CODEC;
   @Nonnull
   private transient AtomicBoolean hasChanged = new AtomicBoolean();
   private UUID uuid = UUID.randomUUID();
   private String displayName;
   private long seed = System.currentTimeMillis();
   @Nullable
   private ISpawnProvider spawnProvider = null;
   private IWorldGenProvider worldGenProvider;
   private IWorldMapProvider worldMapProvider;
   private IChunkStorageProvider<?> chunkStorageProvider;
   @Nonnull
   private ChunkConfig chunkConfig;
   private boolean isTicking;
   private boolean isBlockTicking;
   private boolean isPvpEnabled;
   private boolean isFallDamageEnabled;
   private boolean isGameTimePaused;
   private Instant gameTime;
   private String forcedWeather;
   private ClientEffectWorldSettings clientEffects;
   private Map<PluginIdentifier, SemverRange> requiredPlugins;
   private GameMode gameMode;
   private boolean isSpawningNPC;
   private boolean isSpawnMarkersEnabled;
   private boolean isAllNPCFrozen;
   private String gameplayConfig;
   @Nullable
   private DeathConfig deathConfigOverride;
   @Nullable
   private Integer daytimeDurationSecondsOverride;
   @Nullable
   private Integer nighttimeDurationSecondsOverride;
   private boolean isCompassUpdating;
   private boolean isSavingPlayers;
   private boolean canSaveChunks;
   private boolean saveNewChunks;
   private boolean canUnloadChunks;
   private boolean isObjectiveMarkersEnabled;
   private boolean deleteOnUniverseStart;
   private boolean deleteOnRemove;
   private IResourceStorageProvider resourceStorageProvider;
   protected MapKeyMapCodec.TypeMap<Object> pluginConfig;
   @Nullable
   private transient ISpawnProvider defaultSpawnProvider;
   private transient boolean isSavingConfig;

   public WorldConfig() {
      this.worldGenProvider = IWorldGenProvider.CODEC.getDefault();
      this.worldMapProvider = IWorldMapProvider.CODEC.getDefault();
      this.chunkStorageProvider = IChunkStorageProvider.CODEC.getDefault();
      this.chunkConfig = new ChunkConfig();
      this.isTicking = true;
      this.isBlockTicking = true;
      this.isPvpEnabled = false;
      this.isFallDamageEnabled = true;
      this.isGameTimePaused = false;
      this.gameTime = WorldTimeResource.ZERO_YEAR.plus(5L, ChronoUnit.HOURS).plus(30L, ChronoUnit.MINUTES);
      this.clientEffects = new ClientEffectWorldSettings();
      this.requiredPlugins = Collections.emptyMap();
      this.isSpawningNPC = true;
      this.isSpawnMarkersEnabled = true;
      this.isAllNPCFrozen = false;
      this.gameplayConfig = "Default";
      this.deathConfigOverride = null;
      this.daytimeDurationSecondsOverride = null;
      this.nighttimeDurationSecondsOverride = null;
      this.isCompassUpdating = true;
      this.isSavingPlayers = true;
      this.canSaveChunks = true;
      this.saveNewChunks = true;
      this.canUnloadChunks = true;
      this.isObjectiveMarkersEnabled = true;
      this.deleteOnUniverseStart = false;
      this.deleteOnRemove = false;
      this.resourceStorageProvider = IResourceStorageProvider.CODEC.getDefault();
      this.pluginConfig = new MapKeyMapCodec.TypeMap<Object>(PLUGIN_CODEC);
      this.isSavingConfig = true;
      this.markChanged();
   }

   private WorldConfig(Void dummy) {
      this.worldGenProvider = IWorldGenProvider.CODEC.getDefault();
      this.worldMapProvider = IWorldMapProvider.CODEC.getDefault();
      this.chunkStorageProvider = IChunkStorageProvider.CODEC.getDefault();
      this.chunkConfig = new ChunkConfig();
      this.isTicking = true;
      this.isBlockTicking = true;
      this.isPvpEnabled = false;
      this.isFallDamageEnabled = true;
      this.isGameTimePaused = false;
      this.gameTime = WorldTimeResource.ZERO_YEAR.plus(5L, ChronoUnit.HOURS).plus(30L, ChronoUnit.MINUTES);
      this.clientEffects = new ClientEffectWorldSettings();
      this.requiredPlugins = Collections.emptyMap();
      this.isSpawningNPC = true;
      this.isSpawnMarkersEnabled = true;
      this.isAllNPCFrozen = false;
      this.gameplayConfig = "Default";
      this.deathConfigOverride = null;
      this.daytimeDurationSecondsOverride = null;
      this.nighttimeDurationSecondsOverride = null;
      this.isCompassUpdating = true;
      this.isSavingPlayers = true;
      this.canSaveChunks = true;
      this.saveNewChunks = true;
      this.canUnloadChunks = true;
      this.isObjectiveMarkersEnabled = true;
      this.deleteOnUniverseStart = false;
      this.deleteOnRemove = false;
      this.resourceStorageProvider = IResourceStorageProvider.CODEC.getDefault();
      this.pluginConfig = new MapKeyMapCodec.TypeMap<Object>(PLUGIN_CODEC);
      this.isSavingConfig = true;
   }

   @Nonnull
   public UUID getUuid() {
      return this.uuid;
   }

   public void setUuid(UUID uuid) {
      this.uuid = uuid;
   }

   public boolean isDeleteOnUniverseStart() {
      return this.deleteOnUniverseStart;
   }

   public void setDeleteOnUniverseStart(boolean deleteOnUniverseStart) {
      this.deleteOnUniverseStart = deleteOnUniverseStart;
   }

   public boolean isDeleteOnRemove() {
      return this.deleteOnRemove;
   }

   public void setDeleteOnRemove(boolean deleteOnRemove) {
      this.deleteOnRemove = deleteOnRemove;
   }

   public boolean isSavingConfig() {
      return this.isSavingConfig;
   }

   public void setSavingConfig(boolean savingConfig) {
      this.isSavingConfig = savingConfig;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String name) {
      this.displayName = name;
   }

   @Nonnull
   public static String formatDisplayName(@Nonnull String name) {
      return name.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("([A-Za-z])([0-9])", "$1 $2").replaceAll("_", " ");
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   @Nullable
   public ISpawnProvider getSpawnProvider() {
      return this.spawnProvider != null ? this.spawnProvider : this.defaultSpawnProvider;
   }

   public void setSpawnProvider(ISpawnProvider spawnProvider) {
      this.spawnProvider = spawnProvider;
   }

   public void setDefaultSpawnProvider(@Nonnull IWorldGen generator) {
      this.defaultSpawnProvider = generator.getDefaultSpawnProvider((int)this.seed);
   }

   public IWorldGenProvider getWorldGenProvider() {
      return this.worldGenProvider;
   }

   public void setWorldGenProvider(IWorldGenProvider worldGenProvider) {
      this.worldGenProvider = worldGenProvider;
   }

   public IWorldMapProvider getWorldMapProvider() {
      return this.worldMapProvider;
   }

   public void setWorldMapProvider(IWorldMapProvider worldMapProvider) {
      this.worldMapProvider = worldMapProvider;
   }

   public IChunkStorageProvider<?> getChunkStorageProvider() {
      return this.chunkStorageProvider;
   }

   public void setChunkStorageProvider(IChunkStorageProvider<?> chunkStorageProvider) {
      this.chunkStorageProvider = chunkStorageProvider;
   }

   @Nonnull
   public ChunkConfig getChunkConfig() {
      return this.chunkConfig;
   }

   public void setChunkConfig(@Nonnull ChunkConfig chunkConfig) {
      this.chunkConfig = chunkConfig;
   }

   public boolean isTicking() {
      return this.isTicking;
   }

   public void setTicking(boolean ticking) {
      this.isTicking = ticking;
   }

   public boolean isBlockTicking() {
      return this.isBlockTicking;
   }

   public void setBlockTicking(boolean ticking) {
      this.isBlockTicking = ticking;
   }

   public boolean isPvpEnabled() {
      return this.isPvpEnabled;
   }

   public boolean isFallDamageEnabled() {
      return this.isFallDamageEnabled;
   }

   public void setPvpEnabled(boolean pvpEnabled) {
      this.isPvpEnabled = pvpEnabled;
   }

   public boolean isGameTimePaused() {
      return this.isGameTimePaused;
   }

   public void setGameTimePaused(boolean gameTimePaused) {
      this.isGameTimePaused = gameTimePaused;
   }

   public Instant getGameTime() {
      return this.gameTime;
   }

   public void setGameTime(Instant gameTime) {
      this.gameTime = gameTime;
   }

   public String getForcedWeather() {
      return this.forcedWeather;
   }

   public void setForcedWeather(String forcedWeather) {
      this.forcedWeather = forcedWeather;
   }

   public void setClientEffects(ClientEffectWorldSettings clientEffects) {
      this.clientEffects = clientEffects;
   }

   public ClientEffectWorldSettings getClientEffects() {
      return this.clientEffects;
   }

   @Nonnull
   public Map<PluginIdentifier, SemverRange> getRequiredPlugins() {
      return Collections.unmodifiableMap(this.requiredPlugins);
   }

   public void setRequiredPlugins(Map<PluginIdentifier, SemverRange> requiredPlugins) {
      this.requiredPlugins = requiredPlugins;
   }

   public GameMode getGameMode() {
      return this.gameMode != null ? this.gameMode : HytaleServer.get().getConfig().getDefaults().getGameMode();
   }

   public void setGameMode(GameMode gameMode) {
      this.gameMode = gameMode;
   }

   public boolean isSpawningNPC() {
      return this.isSpawningNPC;
   }

   public void setSpawningNPC(boolean spawningNPC) {
      this.isSpawningNPC = spawningNPC;
   }

   public boolean isSpawnMarkersEnabled() {
      return this.isSpawnMarkersEnabled;
   }

   public void setIsSpawnMarkersEnabled(boolean spawnMarkersEnabled) {
      this.isSpawnMarkersEnabled = spawnMarkersEnabled;
   }

   public boolean isAllNPCFrozen() {
      return this.isAllNPCFrozen;
   }

   public void setIsAllNPCFrozen(boolean allNPCFrozen) {
      this.isAllNPCFrozen = allNPCFrozen;
   }

   public String getGameplayConfig() {
      return this.gameplayConfig;
   }

   public void setGameplayConfig(String gameplayConfig) {
      this.gameplayConfig = gameplayConfig;
   }

   @Nullable
   public DeathConfig getDeathConfigOverride() {
      return this.deathConfigOverride;
   }

   @Nullable
   public Integer getDaytimeDurationSecondsOverride() {
      return this.daytimeDurationSecondsOverride;
   }

   @Nullable
   public Integer getNighttimeDurationSecondsOverride() {
      return this.nighttimeDurationSecondsOverride;
   }

   public boolean isCompassUpdating() {
      return this.isCompassUpdating;
   }

   public void setCompassUpdating(boolean compassUpdating) {
      this.isCompassUpdating = compassUpdating;
   }

   public boolean isSavingPlayers() {
      return this.isSavingPlayers;
   }

   public void setSavingPlayers(boolean savingPlayers) {
      this.isSavingPlayers = savingPlayers;
   }

   public boolean canUnloadChunks() {
      return this.canUnloadChunks;
   }

   public void setCanUnloadChunks(boolean unloadingChunks) {
      this.canUnloadChunks = unloadingChunks;
   }

   public boolean canSaveChunks() {
      return this.canSaveChunks;
   }

   public void setCanSaveChunks(boolean savingChunks) {
      this.canSaveChunks = savingChunks;
   }

   public boolean shouldSaveNewChunks() {
      return this.saveNewChunks;
   }

   public void setSaveNewChunks(boolean saveNewChunks) {
      this.saveNewChunks = saveNewChunks;
   }

   public boolean isObjectiveMarkersEnabled() {
      return this.isObjectiveMarkersEnabled;
   }

   public void setObjectiveMarkersEnabled(boolean objectiveMarkersEnabled) {
      this.isObjectiveMarkersEnabled = objectiveMarkersEnabled;
   }

   public IResourceStorageProvider getResourceStorageProvider() {
      return this.resourceStorageProvider;
   }

   public void setResourceStorageProvider(@Nonnull IResourceStorageProvider resourceStorageProvider) {
      this.resourceStorageProvider = resourceStorageProvider;
   }

   public MapKeyMapCodec.TypeMap<Object> getPluginConfig() {
      return this.pluginConfig;
   }

   public void markChanged() {
      this.hasChanged.set(true);
   }

   public boolean consumeHasChanged() {
      return this.hasChanged.getAndSet(false);
   }

   @Nonnull
   public static CompletableFuture<WorldConfig> load(@Nonnull Path path) {
      return CompletableFuture.supplyAsync(() -> {
         WorldConfig config = (WorldConfig)RawJsonReader.readSyncWithBak(path, CODEC, HytaleLogger.getLogger());
         return config != null ? config : new WorldConfig();
      });
   }

   @Nonnull
   public static CompletableFuture<Void> save(@Nonnull Path path, WorldConfig worldConfig) {
      BsonDocument document = CODEC.encode(worldConfig, (ExtraInfo)ExtraInfo.THREAD_LOCAL.get());
      return BsonUtil.writeDocument(path, document);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(WorldConfig.class, () -> new WorldConfig((Void)null)).versioned()).codecVersion(4)).documentation("Configuration for a single world. Settings in here only affect the world this configuration belongs to.\n\nInstances share this configuration but ignore certain parameters (e.g. *UUID*). In this case the configuration willbe cloned before loading the instance.")).append(new KeyedCodec("UUID", Codec.UUID_BINARY), (o, s) -> o.uuid = s, (o) -> o.uuid).documentation("The unique identifier for this world.\n\nInstances will ignore this and replace it with a freshly generated UUID when spawning the instance.").add()).append(new KeyedCodec("DisplayName", Codec.STRING), (o, s) -> o.displayName = s, (o) -> o.displayName).documentation("The player facing name of this world.").add()).append(new KeyedCodec("Seed", Codec.LONG), (o, i) -> o.seed = i, (o) -> o.seed).documentation("The seed of the world, used for world generation.\n\nIf a seed is not set then one will be randomly generated.").metadata(NoDefaultValue.INSTANCE).add()).append(new KeyedCodec("SpawnPoint", Transform.CODEC), (o, s) -> o.spawnProvider = new GlobalSpawnProvider(s), (o) -> null).documentation("**Deprecated**: Please use **SpawnProvider** instead.").setVersionRange(0, 1).add()).append(new KeyedCodec("SpawnProvider", ISpawnProvider.CODEC), (o, s) -> o.spawnProvider = s, (o) -> o.spawnProvider).documentation("Sets the spawn provider for the world. \n\nThis controls where new players will enter the world at. This can be provided by world generation in some cases.").add()).append(new KeyedCodec("WorldGen", IWorldGenProvider.CODEC), (o, i) -> o.worldGenProvider = i, (o) -> o.worldGenProvider).documentation("Sets the world generator that will be used by the world.").add()).append(new KeyedCodec("WorldMap", IWorldMapProvider.CODEC), (o, i) -> o.worldMapProvider = i, (o) -> o.worldMapProvider).add()).append(new KeyedCodec("ChunkStorage", IChunkStorageProvider.CODEC), (o, i) -> o.chunkStorageProvider = i, (o) -> o.chunkStorageProvider).documentation("Sets the storage system that will be used by the world to store chunks.").add()).append(new KeyedCodec("ChunkConfig", WorldConfig.ChunkConfig.CODEC), (o, i) -> o.chunkConfig = i, (o) -> o.chunkConfig).documentation("Configuration for chunk related settings").addValidator(Validators.nonNull()).add()).append(new KeyedCodec("IsTicking", Codec.BOOLEAN), (o, i) -> o.isTicking = i, (o) -> o.isTicking).documentation("Sets whether chunks in this world are ticking or not.").add()).append(new KeyedCodec("IsBlockTicking", Codec.BOOLEAN), (o, i) -> o.isBlockTicking = i, (o) -> o.isBlockTicking).documentation("Sets whether blocks in this world are ticking or not.").add()).append(new KeyedCodec("IsPvpEnabled", Codec.BOOLEAN), (o, i) -> o.isPvpEnabled = i, (o) -> o.isPvpEnabled).documentation("Sets whether PvP is allowed in this world or not.").add()).append(new KeyedCodec("IsFallDamageEnabled", Codec.BOOLEAN), (o, i) -> o.isFallDamageEnabled = i, (o) -> o.isFallDamageEnabled).documentation("Sets whether fall damage is allowed in this world or not.").add()).append(new KeyedCodec("IsGameTimePaused", Codec.BOOLEAN), (o, i) -> o.isGameTimePaused = i, (o) -> o.isGameTimePaused).documentation("Sets whether the game time is paused.\n\nThis effects things like day/night cycles and things that rely on those.").add()).append(new KeyedCodec("GameTime", Codec.INSTANT), (o, i) -> o.gameTime = i, (o) -> o.gameTime).documentation("The current time of day in the world.").add()).append(new KeyedCodec("ForcedWeather", Codec.STRING), (o, i) -> o.forcedWeather = i, (o) -> o.forcedWeather).documentation("Sets the type of weather that is being forced to be active in this world.").addValidator(Weather.VALIDATOR_CACHE.getValidator()).add()).append(new KeyedCodec("ClientEffects", ClientEffectWorldSettings.CODEC), (o, i) -> o.clientEffects = i, (o) -> o.clientEffects).documentation("Settings for the client's weather and post-processing effects in this world.").add()).append(new KeyedCodec("PregenerateRegion", ShapeCodecs.BOX), (o, i) -> o.chunkConfig.setPregenerateRegion(new Box2D(new Vector2d(i.min.x, i.min.z), new Vector2d(i.max.x, i.max.z))), (o) -> null).setVersionRange(1, 3).addValidator(Validators.deprecated()).add()).append(new KeyedCodec("RequiredPlugins", new ObjectMapCodec(SemverRange.CODEC, HashMap::new, PluginIdentifier::toString, PluginIdentifier::fromString, false)), (o, i) -> o.requiredPlugins = i, (o) -> o.requiredPlugins).documentation("Sets the plugins that are required to be enabled for this world to function.").add()).append(new KeyedCodec("GameMode", ProtocolCodecs.GAMEMODE), (o, i) -> o.gameMode = i, (o) -> o.gameMode).documentation("Sets the default gamemode for this world.").add()).append(new KeyedCodec("IsSpawningNPC", Codec.BOOLEAN), (o, i) -> o.isSpawningNPC = i, (o) -> o.isSpawningNPC).documentation("Whether NPCs can spawn in this world or not.").add()).append(new KeyedCodec("IsSpawnMarkersEnabled", Codec.BOOLEAN), (o, i) -> o.isSpawnMarkersEnabled = i, (o) -> o.isSpawnMarkersEnabled).documentation("Whether spawn markers are enabled for this world.").add()).append(new KeyedCodec("IsAllNPCFrozen", Codec.BOOLEAN), (o, i) -> o.isAllNPCFrozen = i, (o) -> o.isAllNPCFrozen).documentation("Whether all NPCs are frozen for this world").add()).append(new KeyedCodec("GameplayConfig", Codec.STRING), (o, i) -> o.gameplayConfig = i, (o) -> o.gameplayConfig).addValidator(GameplayConfig.VALIDATOR_CACHE.getValidator()).documentation("The gameplay configuration being used by this world").add()).append(new KeyedCodec("Death", DeathConfig.CODEC), (o, i) -> o.deathConfigOverride = i, (o) -> o.deathConfigOverride).documentation("Inline death configuration overrides for this world. If set, these values take precedence over the referenced GameplayConfig.").add()).append(new KeyedCodec("DaytimeDurationSeconds", Codec.INTEGER), (o, i) -> o.daytimeDurationSecondsOverride = i, (o) -> o.daytimeDurationSecondsOverride).documentation("Override for the duration of daytime in seconds. If set, takes precedence over the referenced GameplayConfig.").add()).append(new KeyedCodec("NighttimeDurationSeconds", Codec.INTEGER), (o, i) -> o.nighttimeDurationSecondsOverride = i, (o) -> o.nighttimeDurationSecondsOverride).documentation("Override for the duration of nighttime in seconds. If set, takes precedence over the referenced GameplayConfig.").add()).append(new KeyedCodec("IsCompassUpdating", Codec.BOOLEAN), (o, i) -> o.isCompassUpdating = i, (o) -> o.isCompassUpdating).documentation("Whether the compass is updating in this world").add()).append(new KeyedCodec("IsSavingPlayers", Codec.BOOLEAN), (o, i) -> o.isSavingPlayers = i, (o) -> o.isSavingPlayers).documentation("Whether the configuration for player's is being saved for players in this world.").add()).append(new KeyedCodec("IsSavingChunks", Codec.BOOLEAN), (o, i) -> o.canSaveChunks = i, (o) -> o.canSaveChunks).documentation("Whether the chunk data is allowed to be saved to the disk for this world.").add()).append(new KeyedCodec("SaveNewChunks", Codec.BOOLEAN), (o, i) -> o.saveNewChunks = i, (o) -> o.saveNewChunks).documentation("Whether newly generated chunks should be marked for saving or not.\nEnabling this can prevent random chunks from being out of place if/when worldgen changes but will increase the size of the world on disk.").add()).append(new KeyedCodec("IsUnloadingChunks", Codec.BOOLEAN), (o, i) -> o.canUnloadChunks = i, (o) -> o.canUnloadChunks).documentation("Whether the chunks should be unloaded like normally, or should be prevented from unloading at all.").add()).append(new KeyedCodec("IsObjectiveMarkersEnabled", Codec.BOOLEAN), (o, i) -> o.isObjectiveMarkersEnabled = i, (o) -> o.isObjectiveMarkersEnabled).documentation("Whether objective markers are enabled for this world.").add()).append(new KeyedCodec("DeleteOnUniverseStart", Codec.BOOLEAN), (o, i) -> o.deleteOnUniverseStart = i, (o) -> o.deleteOnUniverseStart).documentation("Whether this world should be deleted when loaded from Universe start. By default this is when going through the world folders in the universe directory.").add()).append(new KeyedCodec("DeleteOnRemove", Codec.BOOLEAN), (o, i) -> o.deleteOnRemove = i, (o) -> o.deleteOnRemove).documentation("Whether this world should be deleted once its been removed from the server").add()).append(new KeyedCodec("Instance", Codec.BSON_DOCUMENT), (o, i, e) -> o.pluginConfig.put(PLUGIN_CODEC.getKeyForId("Instance"), PLUGIN_CODEC.decodeById("Instance", i, e)), (o, e) -> null).setVersionRange(0, 2).documentation("Instance specific configuration.").addValidator(Validators.deprecated()).add()).append(new KeyedCodec("ResourceStorage", IResourceStorageProvider.CODEC), (o, i) -> o.resourceStorageProvider = i, (o) -> o.resourceStorageProvider).documentation("Sets the storage system that will be used to store resources.").add()).appendInherited(new KeyedCodec("Plugin", PLUGIN_CODEC), (o, i) -> {
         if (o.pluginConfig.isEmpty()) {
            o.pluginConfig = i;
         } else {
            MapKeyMapCodec.TypeMap<Object> temp = o.pluginConfig;
            o.pluginConfig.putAll(temp);
            o.pluginConfig.putAll(i);
         }
      }, (o) -> o.pluginConfig, (o, p) -> o.pluginConfig = p.pluginConfig).addValidator(Validators.nonNull()).add()).build();
   }

   public static class ChunkConfig {
      public static final BuilderCodec<ChunkConfig> CODEC;
      private static final Box2D DEFAULT_PREGENERATE_REGION;
      @Nullable
      private Box2D pregenerateRegion;
      @Nullable
      private Box2D keepLoadedRegion;

      @Nullable
      public Box2D getPregenerateRegion() {
         return this.pregenerateRegion;
      }

      public void setPregenerateRegion(@Nullable Box2D pregenerateRegion) {
         if (pregenerateRegion != null) {
            pregenerateRegion.normalize();
         }

         this.pregenerateRegion = pregenerateRegion;
      }

      @Nullable
      public Box2D getKeepLoadedRegion() {
         return this.keepLoadedRegion;
      }

      public void setKeepLoadedRegion(@Nullable Box2D keepLoadedRegion) {
         if (keepLoadedRegion != null) {
            keepLoadedRegion.normalize();
         }

         this.keepLoadedRegion = keepLoadedRegion;
      }

      static {
         CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ChunkConfig.class, ChunkConfig::new).appendInherited(new KeyedCodec("PregenerateRegion", Box2D.CODEC), (o, i) -> o.pregenerateRegion = i, (o) -> o.pregenerateRegion, (o, p) -> o.pregenerateRegion = p.pregenerateRegion).documentation("Sets the region that will be pregenerated for the world.\n\nIf set, the specified region will be pregenerated when the world starts.").add()).appendInherited(new KeyedCodec("KeepLoadedRegion", Box2D.CODEC), (o, i) -> o.keepLoadedRegion = i, (o) -> o.keepLoadedRegion, (o, p) -> o.keepLoadedRegion = p.keepLoadedRegion).documentation("Sets a region of chunks that will never be unloaded.").add()).afterDecode((o) -> {
            if (o.pregenerateRegion != null) {
               o.pregenerateRegion.normalize();
            }

            if (o.keepLoadedRegion != null) {
               o.keepLoadedRegion.normalize();
            }

         })).build();
         DEFAULT_PREGENERATE_REGION = new Box2D(new Vector2d(-512.0, -512.0), new Vector2d(512.0, 512.0));
      }
   }
}
