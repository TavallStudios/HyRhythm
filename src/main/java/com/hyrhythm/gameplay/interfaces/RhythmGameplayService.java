package com.hyrhythm.gameplay.interfaces;

import com.hyrhythm.gameplay.model.RhythmGameplaySnapshot;
import com.hyrhythm.gameplay.model.RhythmInputProcessingResult;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;

import java.util.Optional;
import java.util.UUID;

public interface RhythmGameplayService {
    RhythmGameplaySnapshot startGameplay(UUID playerId, String playerName, String sessionId, String chartId);

    RhythmGameplaySnapshot advanceGameplay(UUID playerId, String playerName, long songTimeMillis);

    RhythmInputProcessingResult submitLaneInput(
        UUID playerId,
        String playerName,
        RhythmLaneInputAction action,
        int lane,
        long songTimeMillis
    );

    RhythmGameplaySnapshot stopGameplay(UUID playerId, String playerName, String reason);

    Optional<RhythmGameplaySnapshot> getActiveGameplay(UUID playerId);

    Optional<RhythmGameplaySnapshot> getLastGameplay(UUID playerId);
}
