package com.hyrhythm.ui;

import java.util.Objects;

final class RhythmGameplayUiSelectors {
    private static final String GAMEPLAY_NOTE_ROOT_ID_PREFIX = "GameplayNote_";

    private RhythmGameplayUiSelectors() {
    }

    static String gameplayNoteRootId(RhythmGameplayLaneDirection laneDirection, String noteId) {
        return GAMEPLAY_NOTE_ROOT_ID_PREFIX
            + Objects.requireNonNull(laneDirection, "laneDirection").idToken
            + "_"
            + sanitizeSelectorToken(noteId);
    }

    static String gameplayNoteRootSelector(RhythmGameplayLaneDirection laneDirection, String noteId) {
        return "#" + gameplayNoteRootId(laneDirection, noteId);
    }

    private static String sanitizeSelectorToken(String rawValue) {
        String sanitizedValue = Objects.requireNonNull(rawValue, "rawValue").replaceAll("[^A-Za-z0-9_]", "_");
        if (sanitizedValue.isBlank()) {
            return "unknown_note";
        }
        return sanitizedValue;
    }
}
