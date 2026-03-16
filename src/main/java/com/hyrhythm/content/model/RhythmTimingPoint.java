package com.hyrhythm.content.model;

public record RhythmTimingPoint(
    long timeMillis,
    double beatLength,
    int meter,
    int sampleSet,
    int sampleIndex,
    int volume,
    boolean uninherited,
    int effects
) {
}
