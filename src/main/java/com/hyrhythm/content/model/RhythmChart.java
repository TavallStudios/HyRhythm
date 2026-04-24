package com.hyrhythm.content.model;

import java.util.List;
import java.util.Objects;

public record RhythmChart(
    String songId,
    String chartId,
    RhythmChartMetadata metadata,
    int keyMode,
    double overallDifficulty,
    List<RhythmTimingPoint> timingPoints,
    List<RhythmNote> notes
) {
    public RhythmChart {
        songId = Objects.requireNonNull(songId, "songId");
        chartId = Objects.requireNonNull(chartId, "chartId");
        metadata = Objects.requireNonNull(metadata, "metadata");
        timingPoints = List.copyOf(timingPoints);
        notes = List.copyOf(notes);
    }

    public long holdCount() {
        return notes.stream().filter(RhythmNote::hold).count();
    }
}
