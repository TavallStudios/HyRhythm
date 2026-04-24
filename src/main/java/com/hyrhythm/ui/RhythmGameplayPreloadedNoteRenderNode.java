package com.hyrhythm.ui;

import java.util.Objects;

final class RhythmGameplayPreloadedNoteRenderNode {
    final String noteId;
    final int lane;
    final RhythmGameplayLaneDirection laneDirection;
    final boolean hold;
    final int noteHeight;
    final String noteSelector;

    boolean preloadQueued;
    boolean completed;
    boolean visible;
    Integer lastTop;

    RhythmGameplayPreloadedNoteRenderNode(
        String noteId,
        int lane,
        RhythmGameplayLaneDirection laneDirection,
        boolean hold,
        int noteHeight,
        String noteSelector
    ) {
        this.noteId = Objects.requireNonNull(noteId, "noteId");
        this.lane = lane;
        this.laneDirection = Objects.requireNonNull(laneDirection, "laneDirection");
        this.hold = hold;
        this.noteHeight = noteHeight;
        this.noteSelector = Objects.requireNonNull(noteSelector, "noteSelector");
    }

    void reset() {
        preloadQueued = false;
        completed = false;
        visible = false;
        lastTop = null;
    }
}
