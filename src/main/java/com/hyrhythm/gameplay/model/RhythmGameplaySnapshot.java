package com.hyrhythm.gameplay.model;

import java.util.Locale;
import java.util.Objects;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RhythmGameplaySnapshot(
    String traceId,
    String sessionId,
    UUID playerId,
    String playerName,
    String songId,
    String chartId,
    long songTimeMillis,
    int score,
    int combo,
    int maxCombo,
    int hitCount,
    int missCount,
    int ghostTapCount,
    int judgedObjectCount,
    int totalObjectCount,
    double accuracyPercent,
    Set<Integer> heldLanes,
    RhythmJudgmentType lastJudgment,
    Long lastDeltaMillis,
    int remainingObjectCount,
    List<RhythmGameplayNoteView> remainingNotes,
    boolean active,
    boolean completed,
    String finishReason
) {
    public RhythmGameplaySnapshot {
        traceId = Objects.requireNonNull(traceId, "traceId");
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        playerName = Objects.requireNonNull(playerName, "playerName");
        songId = Objects.requireNonNull(songId, "songId");
        chartId = Objects.requireNonNull(chartId, "chartId");
        heldLanes = Set.copyOf(heldLanes);
        remainingNotes = List.copyOf(remainingNotes);
        finishReason = finishReason == null ? "" : finishReason;
    }

    public String summary() {
        String status = completed ? "ended" : active ? "active" : "idle";
        String last = lastJudgment == null ? "-" : lastJudgment.name();
        String delta = lastDeltaMillis == null ? "-" : lastDeltaMillis + "ms";
        return "gameplay=" + status
            + " time=" + songTimeMillis + "ms"
            + " score=" + score
            + " combo=" + combo
            + " maxCombo=" + maxCombo
            + " acc=" + String.format(Locale.ROOT, "%.2f", accuracyPercent) + "%"
            + " judged=" + judgedObjectCount + "/" + totalObjectCount
            + " held=" + heldLanes
            + " last=" + last
            + "(" + delta + ")"
            + (finishReason.isBlank() ? "" : " finish=" + finishReason);
    }
}
