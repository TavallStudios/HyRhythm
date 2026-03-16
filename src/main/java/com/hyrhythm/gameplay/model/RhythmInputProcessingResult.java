package com.hyrhythm.gameplay.model;

import java.util.Objects;

public record RhythmInputProcessingResult(
    RhythmGameplaySnapshot snapshot,
    RhythmJudgmentResult judgment,
    String debugSummary
) {
    public RhythmInputProcessingResult {
        snapshot = Objects.requireNonNull(snapshot, "snapshot");
        debugSummary = debugSummary == null ? "" : debugSummary;
    }
}
