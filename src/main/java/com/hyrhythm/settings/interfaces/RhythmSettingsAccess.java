package com.hyrhythm.settings.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;
import com.hyrhythm.settings.model.RhythmPlayerSettings;

import java.util.UUID;

public interface RhythmSettingsAccess extends CoreDependencyAccess {
    default RhythmSettingsService getRhythmSettingsService() {
        return DependencyLoaderAccess.requireInstance(RhythmSettingsService.class, "RhythmSettingsService");
    }

    default RhythmPlayerSettings getOrCreateRhythmPlayerSettings(UUID playerId, String playerName) {
        return getRhythmSettingsService().getOrCreateSettings(playerId, playerName);
    }

    default RhythmPlayerSettings updateRhythmLaneBinding(UUID playerId, String playerName, int lane, String key) {
        return getRhythmSettingsService().updateLaneBinding(playerId, playerName, lane, key);
    }

    default RhythmPlayerSettings updateRhythmLaneKey(UUID playerId, String playerName, int lane, String key) {
        return getRhythmSettingsService().updateLaneKey(playerId, playerName, lane, key);
    }

    default RhythmPlayerSettings resetRhythmKeybinds(UUID playerId, String playerName) {
        return getRhythmSettingsService().resetKeybinds(playerId, playerName);
    }

    default RhythmPlayerSettings resetRhythmLaneKeys(UUID playerId, String playerName) {
        return getRhythmSettingsService().resetLaneKeys(playerId, playerName);
    }

    default String describeRhythmSettings(UUID playerId, String playerName) {
        return getRhythmSettingsService().describeSettings(playerId, playerName);
    }
}
