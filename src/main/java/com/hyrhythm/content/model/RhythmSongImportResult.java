package com.hyrhythm.content.model;

import java.nio.file.Path;
import java.util.Objects;

public record RhythmSongImportResult(
    Path songsDirectory,
    int discoveredSourceCount,
    int importedChartCount,
    int importedSongCount,
    int failedSourceCount
) {
    public RhythmSongImportResult {
        songsDirectory = Objects.requireNonNull(songsDirectory, "songsDirectory");
    }

    public boolean importedAnything() {
        return importedChartCount > 0;
    }
}
