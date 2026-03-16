package com.hyrhythm.gameplay.model;

import java.util.Objects;

public record RhythmGameplayNoteView(
    String noteId,
    int lane,
    long startTimeMillis,
    long endTimeMillis,
    boolean hold,
    boolean headResolved,
    boolean tailResolved,
    boolean holdActive
) {
    public RhythmGameplayNoteView {
        noteId = Objects.requireNonNull(noteId, "noteId");
    }
}
