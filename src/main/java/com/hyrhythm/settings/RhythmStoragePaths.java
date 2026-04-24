package com.hyrhythm.settings;

import java.nio.file.Path;
import java.util.Objects;

public final class RhythmStoragePaths {
    private final Path pluginDataDirectory;
    private final Path rhythmDataDirectory;
    private final Path playerSettingsDirectory;
    private final Path songsDirectory;
    private final Path generatedAssetPackDirectory;

    public RhythmStoragePaths(Path pluginDataDirectory) {
        this.pluginDataDirectory = Objects.requireNonNull(pluginDataDirectory, "pluginDataDirectory");
        this.rhythmDataDirectory = this.pluginDataDirectory.resolve("rhythm");
        this.playerSettingsDirectory = this.rhythmDataDirectory.resolve("player-settings");
        this.songsDirectory = this.rhythmDataDirectory.resolve("songs");
        this.generatedAssetPackDirectory = this.rhythmDataDirectory.resolve("generated-asset-pack");
    }

    public Path getPluginDataDirectory() {
        return pluginDataDirectory;
    }

    public Path getRhythmDataDirectory() {
        return rhythmDataDirectory;
    }

    public Path getPlayerSettingsDirectory() {
        return playerSettingsDirectory;
    }

    public Path getSongsDirectory() {
        return songsDirectory;
    }

    public Path getGeneratedAssetPackDirectory() {
        return generatedAssetPackDirectory;
    }
}
