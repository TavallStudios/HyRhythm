package com.hyrhythm.ui;

import java.util.Objects;

final class RhythmGameplayUiSelectors {
    private static final String GAMEPLAY_NOTE_ROOT_ID_PREFIX = "GameplayNote";

    private RhythmGameplayUiSelectors() {
    }

    static String gameplayNoteRootId(RhythmGameplayLaneDirection laneDirection, String noteId) {
        return GAMEPLAY_NOTE_ROOT_ID_PREFIX
            + capitalizeToken(Objects.requireNonNull(laneDirection, "laneDirection").idToken)
            + sanitizeSelectorToken(noteId);
    }

    static String gameplayNoteRootSelector(RhythmGameplayLaneDirection laneDirection, String noteId) {
        return "#" + gameplayNoteRootId(laneDirection, noteId);
    }

    private static String capitalizeToken(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "Unknown";
        }
        String trimmedValue = rawValue.trim();
        return Character.toUpperCase(trimmedValue.charAt(0)) + trimmedValue.substring(1);
    }

    private static String sanitizeSelectorToken(String rawValue) {
        String sanitizedValue = Objects.requireNonNull(rawValue, "rawValue")
            .replaceAll("[^A-Za-z0-9]+", " ")
            .trim();
        if (sanitizedValue.isBlank()) {
            return "UnknownNote";
        }
        StringBuilder tokenBuilder = new StringBuilder(sanitizedValue.length());
        for (String part : sanitizedValue.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            tokenBuilder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                tokenBuilder.append(part.substring(1));
            }
        }
        if (tokenBuilder.isEmpty()) {
            return "UnknownNote";
        }
        return tokenBuilder.toString();
    }
}
