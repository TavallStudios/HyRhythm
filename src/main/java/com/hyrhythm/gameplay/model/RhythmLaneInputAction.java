package com.hyrhythm.gameplay.model;

import java.util.Locale;

public enum RhythmLaneInputAction {
    DOWN,
    UP;

    public static RhythmLaneInputAction parse(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Input action must be provided.");
        }
        return switch (rawValue.trim().toUpperCase(Locale.ROOT)) {
            case "DOWN" -> DOWN;
            case "UP" -> UP;
            default -> throw new IllegalArgumentException("Input action must be 'down' or 'up'.");
        };
    }
}
