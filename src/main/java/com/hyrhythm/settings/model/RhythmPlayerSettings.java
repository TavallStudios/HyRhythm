package com.hyrhythm.settings.model;

import java.util.Objects;

public record RhythmPlayerSettings(
    RhythmKeybinds keybinds,
    RhythmLaneKeys laneKeys,
    int globalOffsetMs,
    double scrollSpeed
) {
    public RhythmPlayerSettings {
        keybinds = Objects.requireNonNull(keybinds, "keybinds");
        laneKeys = Objects.requireNonNull(laneKeys, "laneKeys");
        if (scrollSpeed <= 0.0d) {
            throw new IllegalArgumentException("scrollSpeed must be positive.");
        }
    }

    public static RhythmPlayerSettings defaults() {
        return new RhythmPlayerSettings(RhythmKeybinds.defaults(), RhythmLaneKeys.defaults(), 0, 1.0d);
    }

    public RhythmPlayerSettings withKeybinds(RhythmKeybinds keybinds) {
        return new RhythmPlayerSettings(keybinds, laneKeys, globalOffsetMs, scrollSpeed);
    }

    public RhythmPlayerSettings withLaneKeys(RhythmLaneKeys laneKeys) {
        return new RhythmPlayerSettings(keybinds, laneKeys, globalOffsetMs, scrollSpeed);
    }
}
