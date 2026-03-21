package com.hyrhythm.ui;

import java.util.Objects;

final class RhythmGameplayPreloadedNoteRenderNode {
    final String noteId;
    final int lane;
    final RhythmGameplayLaneDirection laneDirection;
    final boolean hold;
    final int noteHeight;
    final String laneSurfaceSelector;
    final int laneSurfaceChildIndex;
    final String noteRootSelector;
    final String contentDocumentPath;

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
        String laneSurfaceSelector,
        int laneSurfaceChildIndex,
        String noteRootSelector,
        String contentDocumentPath
    ) {
        this.noteId = Objects.requireNonNull(noteId, "noteId");
        this.lane = lane;
        this.laneDirection = Objects.requireNonNull(laneDirection, "laneDirection");
        this.hold = hold;
        this.noteHeight = noteHeight;
        this.laneSurfaceSelector = Objects.requireNonNull(laneSurfaceSelector, "laneSurfaceSelector");
        this.laneSurfaceChildIndex = laneSurfaceChildIndex;
        this.noteRootSelector = Objects.requireNonNull(noteRootSelector, "noteRootSelector");
        this.contentDocumentPath = Objects.requireNonNull(contentDocumentPath, "contentDocumentPath");
    }

    void reset() {
        preloadQueued = false;
        completed = false;
        visible = false;
        lastTop = null;
    }
}
