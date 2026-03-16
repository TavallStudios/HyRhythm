package com.hyrhythm.settings;

import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.settings.model.RhythmKeybinds;
import com.hyrhythm.settings.model.RhythmLaneKeys;
import com.hyrhythm.settings.model.RhythmPlayerSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public final class RhythmPlayerSettingsStore implements RhythmLoggingAccess {
    private final RhythmStoragePaths storagePaths;

    public RhythmPlayerSettingsStore(RhythmStoragePaths storagePaths) {
        this.storagePaths = storagePaths;
    }

    public Optional<RhythmPlayerSettings> load(UUID playerId) {
        Path path = settingsPath(playerId);
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            RhythmPlayerSettings settings = new RhythmPlayerSettings(
                RhythmKeybinds.fromStoredValues(
                    properties.getProperty("lane1"),
                    properties.getProperty("lane2"),
                    properties.getProperty("lane3"),
                    properties.getProperty("lane4")
                ),
                RhythmLaneKeys.fromStoredValues(
                    properties.getProperty("lane1Key"),
                    properties.getProperty("lane2Key"),
                    properties.getProperty("lane3Key"),
                    properties.getProperty("lane4Key")
                ),
                parseInt(properties.getProperty("globalOffsetMs"), 0),
                parseDouble(properties.getProperty("scrollSpeed"), 1.0d)
            );
            logRhythmDebug(
                "settings",
                "settings_loaded_from_disk",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("path", path);
                }}
            );
            return Optional.of(settings);
        } catch (IOException exception) {
            logRhythmError(
                "settings",
                "settings_load_failed",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("path", path);
                }},
                exception
            );
            throw new IllegalStateException("Failed to load settings for " + playerId, exception);
        }
    }

    public RhythmPlayerSettings save(UUID playerId, RhythmPlayerSettings settings) {
        ensureDirectories();
        Path path = settingsPath(playerId);
        Properties properties = new Properties();
        properties.setProperty("lane1", settings.keybinds().lane1Input().displayName());
        properties.setProperty("lane2", settings.keybinds().lane2Input().displayName());
        properties.setProperty("lane3", settings.keybinds().lane3Input().displayName());
        properties.setProperty("lane4", settings.keybinds().lane4Input().displayName());
        properties.setProperty("lane1Key", settings.laneKeys().lane1Key());
        properties.setProperty("lane2Key", settings.laneKeys().lane2Key());
        properties.setProperty("lane3Key", settings.laneKeys().lane3Key());
        properties.setProperty("lane4Key", settings.laneKeys().lane4Key());
        properties.setProperty("globalOffsetMs", Integer.toString(settings.globalOffsetMs()));
        properties.setProperty("scrollSpeed", Double.toString(settings.scrollSpeed()));

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, "HyRhythm player settings");
            logRhythmInfo(
                "settings",
                "settings_persisted",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("path", path);
                    put("keybinds", settings.keybinds().toDisplayString());
                    put("laneKeys", settings.laneKeys().toDisplayString());
                }}
            );
            return settings;
        } catch (IOException exception) {
            logRhythmError(
                "settings",
                "settings_persist_failed",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("path", path);
                }},
                exception
            );
            throw new IllegalStateException("Failed to persist settings for " + playerId, exception);
        }
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(storagePaths.getPlayerSettingsDirectory());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create settings directory " + storagePaths.getPlayerSettingsDirectory(), exception);
        }
    }

    private Path settingsPath(UUID playerId) {
        return storagePaths.getPlayerSettingsDirectory().resolve(playerId + ".properties");
    }

    private static int parseInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(rawValue.trim());
    }

    private static double parseDouble(String rawValue, double defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(rawValue.trim());
    }
}
