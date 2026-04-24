package com.hyrhythm.content.model;

public record RhythmNote(
    String noteId,
    int lane,
    long startTimeMillis,
    long endTimeMillis,
    boolean hold
) {
    public long durationMillis() {
        return Math.max(0L, endTimeMillis - startTimeMillis);
    }
}
