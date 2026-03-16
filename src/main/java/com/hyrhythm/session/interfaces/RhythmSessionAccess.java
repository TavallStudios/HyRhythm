package com.hyrhythm.session.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;
import com.hyrhythm.session.model.RhythmSessionSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface RhythmSessionAccess extends CoreDependencyAccess {
    default RhythmSessionService getRhythmSessionService() {
        return DependencyLoaderAccess.requireInstance(RhythmSessionService.class, "RhythmSessionService");
    }

    default RhythmSessionSnapshot joinOrCreateRhythmSession(UUID playerId, String playerName) {
        return getRhythmSessionService().joinOrCreateSession(playerId, playerName);
    }

    default RhythmSessionSnapshot prepareRhythmSelfTest(UUID playerId, String playerName) {
        return getRhythmSessionService().prepareSelfTestSession(playerId, playerName);
    }

    default RhythmSessionSnapshot selectRhythmChart(UUID playerId, String playerName, String chartId) {
        return getRhythmSessionService().selectChart(playerId, playerName, chartId);
    }

    default RhythmSessionSnapshot startRhythmSession(UUID playerId, String playerName) {
        return getRhythmSessionService().startSession(playerId, playerName);
    }

    default RhythmSessionSnapshot completeRhythmSession(UUID playerId, String playerName, String reason) {
        return getRhythmSessionService().completeSession(playerId, playerName, reason);
    }

    default RhythmSessionSnapshot stopRhythmSession(UUID playerId, String playerName, String reason) {
        return getRhythmSessionService().stopSession(playerId, playerName, reason);
    }

    default Optional<RhythmSessionSnapshot> findRhythmSession(UUID playerId) {
        return getRhythmSessionService().getSession(playerId);
    }
}
