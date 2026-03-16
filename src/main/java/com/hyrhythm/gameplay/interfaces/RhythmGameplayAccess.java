package com.hyrhythm.gameplay.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;
import com.hyrhythm.gameplay.model.RhythmGameplaySnapshot;
import com.hyrhythm.gameplay.model.RhythmInputProcessingResult;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;

import java.util.Optional;
import java.util.UUID;

public interface RhythmGameplayAccess extends CoreDependencyAccess {
    default RhythmGameplayService getRhythmGameplayService() {
        return DependencyLoaderAccess.requireInstance(RhythmGameplayService.class, "RhythmGameplayService");
    }

    default RhythmGameplaySnapshot startRhythmGameplay(UUID playerId, String playerName, String sessionId, String chartId) {
        return getRhythmGameplayService().startGameplay(playerId, playerName, sessionId, chartId);
    }

    default RhythmGameplaySnapshot advanceRhythmGameplay(UUID playerId, String playerName, long songTimeMillis) {
        return getRhythmGameplayService().advanceGameplay(playerId, playerName, songTimeMillis);
    }

    default RhythmInputProcessingResult submitRhythmLaneInput(
        UUID playerId,
        String playerName,
        RhythmLaneInputAction action,
        int lane,
        long songTimeMillis
    ) {
        return getRhythmGameplayService().submitLaneInput(playerId, playerName, action, lane, songTimeMillis);
    }

    default RhythmGameplaySnapshot stopRhythmGameplay(UUID playerId, String playerName, String reason) {
        return getRhythmGameplayService().stopGameplay(playerId, playerName, reason);
    }

    default Optional<RhythmGameplaySnapshot> findActiveRhythmGameplay(UUID playerId) {
        return getRhythmGameplayService().getActiveGameplay(playerId);
    }

    default Optional<RhythmGameplaySnapshot> findLastRhythmGameplay(UUID playerId) {
        return getRhythmGameplayService().getLastGameplay(playerId);
    }
}
