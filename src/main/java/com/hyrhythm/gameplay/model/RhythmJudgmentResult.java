package com.hyrhythm.gameplay.model;

import java.util.Locale;
import java.util.Objects;

public record RhythmJudgmentResult(
    String traceId,
    String sessionId,
    String chartId,
    String noteId,
    int lane,
    RhythmLaneInputAction action,
    RhythmJudgmentType type,
    long songTimeMillis,
    long deltaMillis,
    int comboBefore,
    int comboAfter,
    int scoreBefore,
    int scoreAfter,
    double accuracyBefore,
    double accuracyAfter,
    String detail
) {
    public RhythmJudgmentResult {
        traceId = Objects.requireNonNull(traceId, "traceId");
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        chartId = Objects.requireNonNull(chartId, "chartId");
        type = Objects.requireNonNull(type, "type");
        detail = detail == null ? "" : detail;
    }

    public String summary() {
        String noteValue = noteId == null ? "-" : noteId;
        String actionValue = action == null ? "auto" : action.name().toLowerCase(Locale.ROOT);
        return actionValue
            + " lane=" + lane
            + " time=" + songTimeMillis + "ms"
            + " result=" + type.name()
            + " delta=" + deltaMillis + "ms"
            + " note=" + noteValue
            + " combo=" + comboAfter
            + " score=" + scoreAfter
            + " acc=" + String.format(Locale.ROOT, "%.2f", accuracyAfter) + "%";
    }
}
