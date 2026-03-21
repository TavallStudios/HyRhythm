package com.hyrhythm.ui;

import java.util.Objects;

final class RhythmGameplayPreloadedNoteRenderNode {
    final String noteId;
    final int lane;
    final RhythmGameplayLaneDirection laneDirection;
    final boolean hold;
    final int noteHeight;
    final String hostSelector;
    final String rootId;
    final String rootSelector;
    final String hostDocument;
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
        String hostSelector,
        String rootId,
        String hostDocument,
        String contentDocumentPath
    ) {
        this.noteId = Objects.requireNonNull(noteId, "noteId");
        this.lane = lane;
        this.laneDirection = Objects.requireNonNull(laneDirection, "laneDirection");
        this.hold = hold;
        this.noteHeight = noteHeight;
        this.hostSelector = Objects.requireNonNull(hostSelector, "hostSelector");
        this.rootId = Objects.requireNonNull(rootId, "rootId");
        this.rootSelector = "#" + this.rootId;
        this.hostDocument = Objects.requireNonNull(hostDocument, "hostDocument");
        this.contentDocumentPath = Objects.requireNonNull(contentDocumentPath, "contentDocumentPath");
    }

    void reset() {
        preloadQueued = false;
        completed = false;
        visible = false;
        lastTop = null;
    }
}
