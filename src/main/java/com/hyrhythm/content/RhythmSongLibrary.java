package com.hyrhythm.content;

import com.hyrhythm.content.interfaces.RhythmChartImportService;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.content.model.RhythmSongImportResult;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.settings.RhythmStoragePaths;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.asset.AssetModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class RhythmSongLibrary implements RhythmSongLibraryService, RhythmLoggingAccess {
    private static final String BUILT_IN_DEBUG_CHART = "content/charts/debug/test-4k.osu";
    private static final String DEBUG_CHART_ID = "debug/test-4k";
    private static final String GENERATED_ASSET_PACK_ID = "HyRhythm:ImportedSongs";
    private static final String GENERATED_SOUND_EVENT_ROOT = "hyrhythm/imported";
    private static final String GENERATED_SOUND_FILE_ROOT = "Sounds/HyRhythm/Imported";
    private static final String GENERATED_SOUND_EVENT_PATH = "Server/Audio/SoundEvents";
    private static final String GENERATED_FFMPEG_ENV = "HYRHYTHM_FFMPEG_BIN";

    private final RhythmChartImportService chartImportService;
    private final RhythmStoragePaths storagePaths;
    private final Map<String, RhythmChart> chartsById = new LinkedHashMap<>();
    private final Map<String, RhythmSong> songsById = new LinkedHashMap<>();
    private final Map<String, String> soundEventIdsBySongId = new LinkedHashMap<>();

    private boolean builtInsLoaded;
    private boolean configuredSongsLoaded;

    public RhythmSongLibrary(RhythmChartImportService chartImportService, RhythmStoragePaths storagePaths) {
        this.chartImportService = Objects.requireNonNull(chartImportService, "chartImportService");
        this.storagePaths = Objects.requireNonNull(storagePaths, "storagePaths");
    }

    @Override
    public synchronized void loadBuiltInSongs() {
        ensureBuiltInSongsLoaded();
        if (!configuredSongsLoaded) {
            configuredSongsLoaded = true;
            importConfiguredSongsInternal();
        }
    }

    @Override
    public synchronized List<RhythmSong> listSongs() {
        loadBuiltInSongs();
        return List.copyOf(songsById.values());
    }

    @Override
    public synchronized RhythmSongImportResult importSongsFromConfiguredDirectory() {
        ensureBuiltInSongsLoaded();
        configuredSongsLoaded = true;
        return importConfiguredSongsInternal();
    }

    @Override
    public Path songsDirectory() {
        return storagePaths.getSongsDirectory();
    }

    @Override
    public synchronized Optional<RhythmChart> findChartById(String chartId) {
        loadBuiltInSongs();
        return Optional.ofNullable(chartsById.get(chartId));
    }

    @Override
    public synchronized Optional<String> findSoundEventIdBySongId(String songId) {
        loadBuiltInSongs();
        if (songId == null || songId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(soundEventIdsBySongId.get(songId.trim()));
    }

    @Override
    public synchronized RhythmChart requireDebugChart() {
        loadBuiltInSongs();
        RhythmChart chart = chartsById.get(DEBUG_CHART_ID);
        if (chart == null) {
            throw new IllegalStateException("Built-in debug chart is not available.");
        }
        return chart;
    }

    private void ensureBuiltInSongsLoaded() {
        if (builtInsLoaded) {
            return;
        }

        try (InputStream inputStream = RhythmSongLibrary.class.getClassLoader().getResourceAsStream(BUILT_IN_DEBUG_CHART)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing built-in chart resource " + BUILT_IN_DEBUG_CHART);
            }

            RhythmChart chart = chartImportService.importOsu(BUILT_IN_DEBUG_CHART, inputStream);
            registerChart(chart);
            builtInsLoaded = true;
            logRhythmInfo(
                "content",
                "built_in_chart_registered",
                new LinkedHashMap<>() {{
                    put("chartId", chart.chartId());
                    put("songId", chart.songId());
                    put("source", chart.metadata().sourceName());
                }}
            );
        } catch (Exception exception) {
            logRhythmError("content", "built_in_chart_load_failed", Map.of("resource", BUILT_IN_DEBUG_CHART), exception);
            throw new IllegalStateException("Failed to load built-in rhythm chart.", exception);
        }
    }

    private RhythmSongImportResult importConfiguredSongsInternal() {
        Path songsDirectory = songsDirectory();
        try {
            Files.createDirectories(songsDirectory);
            Files.createDirectories(storagePaths.getGeneratedAssetPackDirectory());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare rhythm storage directories.", exception);
        }

        List<Path> sourceFiles = discoverSongSources(songsDirectory);
        int importedCharts = 0;
        int failedSources = 0;
        int songsBefore = songsById.size();
        soundEventIdsBySongId.clear();
        resetGeneratedAssetPack();

        for (Path sourceFile : sourceFiles) {
            try {
                importedCharts += importSourceFile(songsDirectory, sourceFile);
            } catch (Exception exception) {
                failedSources++;
                logRhythmWarn(
                    "content",
                    "song_import_failed",
                    Map.of("source", sourceFile, "reason", exception.getMessage())
                );
            }
        }

        registerGeneratedAssetPack();
        RhythmSongImportResult result = new RhythmSongImportResult(
            songsDirectory,
            sourceFiles.size(),
            importedCharts,
            Math.max(0, songsById.size() - songsBefore),
            failedSources
        );
        logRhythmInfo(
            "content",
            "songs_import_completed",
            new LinkedHashMap<>() {{
                put("songsDirectory", result.songsDirectory());
                put("discoveredSourceCount", result.discoveredSourceCount());
                put("importedChartCount", result.importedChartCount());
                put("importedSongCount", result.importedSongCount());
                put("failedSourceCount", result.failedSourceCount());
                put("registeredAudioCount", soundEventIdsBySongId.size());
            }}
        );
        return result;
    }

    private void registerChart(RhythmChart chart) {
        RhythmChart previousChart = chartsById.put(chart.chartId(), chart);
        if (previousChart != null) {
            rebuildSong(previousChart.songId(), previousChart.chartId(), null);
        }
        rebuildSong(chart.songId(), null, chart);
    }

    private void rebuildSong(String songId, String removedChartId, RhythmChart appendedChart) {
        RhythmSong existingSong = songsById.get(songId);
        List<RhythmChart> charts = new ArrayList<>();
        if (existingSong != null) {
            for (RhythmChart chart : existingSong.charts()) {
                if (removedChartId != null && removedChartId.equals(chart.chartId())) {
                    continue;
                }
                charts.add(chart);
            }
        }
        if (appendedChart != null) {
            charts.removeIf(chart -> chart.chartId().equals(appendedChart.chartId()));
            charts.add(appendedChart);
        }
        charts.sort(Comparator.comparing(chart -> chart.metadata().difficultyName()));
        if (charts.isEmpty()) {
            songsById.remove(songId);
            return;
        }
        RhythmChart songChart = charts.getFirst();
        songsById.put(
            songId,
            new RhythmSong(songId, songChart.metadata().title(), songChart.metadata().artist(), charts)
        );
    }

    private int importSourceFile(Path songsDirectory, Path sourceFile) throws IOException {
        String normalizedName = sourceFile.getFileName().toString().toLowerCase(Locale.ROOT);
        if (normalizedName.endsWith(".osu")) {
            try (InputStream inputStream = Files.newInputStream(sourceFile)) {
                RhythmChart chart = chartImportService.importOsu(songsDirectory.relativize(sourceFile).toString(), inputStream);
                registerChart(chart);
                registerLooseAudioAsset(sourceFile, chart);
                return 1;
            }
        }
        if (normalizedName.endsWith(".osz")) {
            List<RhythmChart> charts = chartImportService.importOsz(sourceFile);
            for (RhythmChart chart : charts) {
                registerChart(chart);
            }
            registerArchiveAudioAssets(sourceFile, charts);
            return charts.size();
        }
        return 0;
    }

    private void registerLooseAudioAsset(Path chartPath, RhythmChart chart) {
        Path siblingAudio = chartPath.getParent().resolve(chart.metadata().audioFileName());
        if (!Files.isRegularFile(siblingAudio)) {
            logRhythmWarn(
                "content",
                "song_audio_source_missing",
                Map.of("chartId", chart.chartId(), "audio", siblingAudio)
            );
            return;
        }
        registerSoundEventForSong(chart.songId(), siblingAudio);
    }

    private void registerArchiveAudioAssets(Path archivePath, Collection<RhythmChart> charts) throws IOException {
        if (charts.isEmpty()) {
            return;
        }

        try (ZipFile zipFile = new ZipFile(archivePath.toFile())) {
            for (RhythmChart chart : charts) {
                if (soundEventIdsBySongId.containsKey(chart.songId())) {
                    continue;
                }
                ZipEntry audioEntry = findArchiveEntry(zipFile, chart.metadata().audioFileName());
                if (audioEntry == null) {
                    logRhythmWarn(
                        "content",
                        "song_audio_source_missing",
                        Map.of("chartId", chart.chartId(), "audio", chart.metadata().audioFileName(), "archive", archivePath)
                    );
                    continue;
                }
                Path extractedAudio = extractArchiveAudio(archivePath, zipFile, audioEntry, chart.songId());
                if (extractedAudio != null) {
                    registerSoundEventForSong(chart.songId(), extractedAudio);
                }
            }
        }
    }

    private Path extractArchiveAudio(Path archivePath, ZipFile zipFile, ZipEntry audioEntry, String songId) throws IOException {
        Path extractedSource = storagePaths.getGeneratedAssetPackDirectory()
            .resolve(".tmp")
            .resolve(slug(songId))
            .resolve(safeFileName(Path.of(audioEntry.getName()).getFileName().toString()));
        Files.createDirectories(extractedSource.getParent());
        try (InputStream inputStream = zipFile.getInputStream(audioEntry)) {
            Files.copy(inputStream, extractedSource, StandardCopyOption.REPLACE_EXISTING);
        }
        logRhythmInfo(
            "content",
            "song_audio_extracted",
            Map.of("songId", songId, "archive", archivePath, "entry", audioEntry.getName())
        );
        return extractedSource;
    }

    private void registerSoundEventForSong(String songId, Path sourceAudio) {
        try {
            Path oggTarget = ensureOggAudio(songId, sourceAudio);
            if (oggTarget == null) {
                return;
            }
            String soundFile = GENERATED_SOUND_FILE_ROOT + "/" + slug(songId) + "/" + oggTarget.getFileName();
            writeSoundEventFile(songId, soundFile);
            soundEventIdsBySongId.put(songId, soundEventId(songId));
            logRhythmInfo(
                "content",
                "song_audio_registered",
                Map.of("songId", songId, "soundEventId", soundEventId(songId), "soundFile", soundFile)
            );
        } catch (Exception exception) {
            logRhythmWarn(
                "content",
                "song_audio_registration_failed",
                Map.of("songId", songId, "audio", sourceAudio, "reason", exception.getMessage())
            );
        }
    }

    private Path ensureOggAudio(String songId, Path sourceAudio) throws IOException, InterruptedException {
        String normalizedName = sourceAudio.getFileName().toString().toLowerCase(Locale.ROOT);
        Path oggTarget = generatedSoundDirectory(songId).resolve(slug(stripExtension(sourceAudio.getFileName().toString())) + ".ogg");
        Files.createDirectories(oggTarget.getParent());
        if (normalizedName.endsWith(".ogg")) {
            Files.copy(sourceAudio, oggTarget, StandardCopyOption.REPLACE_EXISTING);
            return oggTarget;
        }

        Path ffmpegExecutable = resolveFfmpegExecutable();
        if (ffmpegExecutable == null) {
            logRhythmWarn(
                "content",
                "song_audio_transcode_skipped",
                Map.of("songId", songId, "sourceAudio", sourceAudio, "reason", "ffmpeg_not_found")
            );
            return null;
        }

        Process process = new ProcessBuilder(
            ffmpegExecutable.toString(),
            "-y",
            "-loglevel",
            "error",
            "-i",
            sourceAudio.toString(),
            "-vn",
            "-acodec",
            "libvorbis",
            oggTarget.toString()
        ).start();
        int exitCode = process.waitFor();
        if (exitCode != 0 || !Files.isRegularFile(oggTarget)) {
            throw new IllegalStateException("ffmpeg failed with exit code " + exitCode + " for " + sourceAudio);
        }
        return oggTarget;
    }

    private void writeSoundEventFile(String songId, String soundFile) throws IOException {
        Path soundEventFile = storagePaths.getGeneratedAssetPackDirectory()
            .resolve(GENERATED_SOUND_EVENT_PATH)
            .resolve("HyRhythm")
            .resolve("Imported")
            .resolve(slug(songId) + ".json");
        Files.createDirectories(soundEventFile.getParent());
        Files.writeString(
            soundEventFile,
            """
                {
                  "AudioCategory": "AudioCat_Music",
                  "Layers": [
                    {
                      "Files": [
                        "%s"
                      ]
                    }
                  ]
                }
                """.formatted(soundFile)
        );
    }

    private void registerGeneratedAssetPack() {
        AssetModule assetModule = AssetModule.get();
        if (assetModule == null) {
            return;
        }

        Path packRoot = storagePaths.getGeneratedAssetPackDirectory();
        try {
            Files.createDirectories(packRoot);
            writeGeneratedAssetManifest(packRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare generated asset pack " + packRoot, exception);
        }

        AssetPack existingPack = assetModule.getAssetPack(GENERATED_ASSET_PACK_ID);
        if (existingPack != null) {
            assetModule.unregisterPack(GENERATED_ASSET_PACK_ID);
        }
        assetModule.registerPack(GENERATED_ASSET_PACK_ID, packRoot, generatedAssetPackManifest(), true);
    }

    private void writeGeneratedAssetManifest(Path packRoot) throws IOException {
        Files.writeString(
            packRoot.resolve("manifest.json"),
            """
                {
                  "Group": "HyRhythm",
                  "Name": "ImportedSongs",
                  "Version": "1.0.0",
                  "Description": "Generated sound assets for imported HyRhythm songs",
                  "Authors": [
                    {
                      "Name": "HyRhythm",
                      "Email": "",
                      "Url": ""
                    }
                  ],
                  "Website": "",
                  "DisabledByDefault": false,
                  "IncludesAssetPack": true,
                  "Dependencies": {},
                  "OptionalDependencies": {},
                  "ServerVersion": "*"
                }
                """
        );
    }

    private static PluginManifest generatedAssetPackManifest() {
        PluginManifest manifest = new PluginManifest();
        manifest.setGroup("HyRhythm");
        manifest.setName("ImportedSongs");
        manifest.setVersion(Semver.fromString("1.0.0"));
        manifest.setDescription("Generated sound assets for imported HyRhythm songs");
        manifest.setServerVersion("*");
        return manifest;
    }

    private void resetGeneratedAssetPack() {
        Path packRoot = storagePaths.getGeneratedAssetPackDirectory();
        if (!Files.exists(packRoot)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(packRoot)) {
            stream.sorted(Comparator.reverseOrder())
                .filter(path -> !path.equals(packRoot))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException exception) {
                        throw new IllegalStateException("Failed to reset generated asset pack path " + path, exception);
                    }
                });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to reset generated asset pack " + packRoot, exception);
        }
    }

    private Path generatedSoundDirectory(String songId) {
        return storagePaths.getGeneratedAssetPackDirectory()
            .resolve("Common")
            .resolve("Sounds")
            .resolve("HyRhythm")
            .resolve("Imported")
            .resolve(slug(songId));
    }

    private static ZipEntry findArchiveEntry(ZipFile zipFile, String entryName) {
        String normalizedTarget = normalizeArchiveName(entryName);
        return zipFile.stream()
            .filter(entry -> !entry.isDirectory())
            .filter(entry -> normalizeArchiveName(entry.getName()).equals(normalizedTarget))
            .findFirst()
            .orElse(null);
    }

    private static String normalizeArchiveName(String rawName) {
        String normalized = rawName == null ? "" : rawName.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0) {
            normalized = normalized.substring(lastSlash + 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static List<Path> discoverSongSources(Path songsDirectory) {
        if (!Files.exists(songsDirectory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(songsDirectory)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String normalizedName = path.getFileName().toString().toLowerCase(Locale.ROOT);
                    return normalizedName.endsWith(".osu") || normalizedName.endsWith(".osz");
                })
                .sorted()
                .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to scan songs directory " + songsDirectory, exception);
        }
    }

    private static String soundEventId(String songId) {
        return GENERATED_SOUND_EVENT_ROOT + "/" + slug(songId);
    }

    private static String slug(String rawValue) {
        return rawValue == null ? "unknown" : rawValue
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace('\\', '-')
            .replace('/', '-')
            .replaceAll("[^a-z0-9._-]+", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
    }

    private static String stripExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex <= 0 ? fileName : fileName.substring(0, extensionIndex);
    }

    private static String safeFileName(String fileName) {
        return fileName.replace('\\', '_').replace('/', '_');
    }

    private static Path resolveFfmpegExecutable() {
        String envOverride = System.getenv(GENERATED_FFMPEG_ENV);
        if (envOverride != null && !envOverride.isBlank()) {
            Path configuredPath = Path.of(envOverride.trim());
            if (Files.isRegularFile(configuredPath)) {
                return configuredPath;
            }
        }

        for (Path candidate : imageioFfmpegCandidates()) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static List<Path> imageioFfmpegCandidates() {
        Path userLocal = Path.of(System.getProperty("user.home"), ".local", "lib");
        if (!Files.isDirectory(userLocal)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.walk(userLocal, 6)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith("ffmpeg-"))
                .sorted()
                .toList();
        } catch (IOException exception) {
            return List.of();
        }
    }
}
