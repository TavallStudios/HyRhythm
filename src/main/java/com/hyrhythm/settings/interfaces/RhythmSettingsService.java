package com.hyrhythm.settings.interfaces;

import com.hyrhythm.settings.model.RhythmPlayerSettings;

import java.util.UUID;

public interface RhythmSettingsService {
    RhythmPlayerSettings getOrCreateSettings(UUID playerId, String playerName);

    RhythmPlayerSettings updateLaneBinding(UUID playerId, String playerName, int lane, String key);

    RhythmPlayerSettings updateLaneKey(UUID playerId, String playerName, int lane, String key);

    RhythmPlayerSettings resetKeybinds(UUID playerId, String playerName);

    RhythmPlayerSettings resetLaneKeys(UUID playerId, String playerName);

    String describeSettings(UUID playerId, String playerName);
}
