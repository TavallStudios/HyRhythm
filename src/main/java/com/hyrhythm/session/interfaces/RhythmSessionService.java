package com.hyrhythm.session.interfaces;

import com.hyrhythm.session.model.RhythmSessionSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface RhythmSessionService {
    RhythmSessionSnapshot joinOrCreateSession(UUID playerId, String playerName);

    RhythmSessionSnapshot prepareSelfTestSession(UUID playerId, String playerName);

    RhythmSessionSnapshot selectChart(UUID playerId, String playerName, String chartId);

    RhythmSessionSnapshot startSession(UUID playerId, String playerName);

    RhythmSessionSnapshot completeSession(UUID playerId, String playerName, String reason);

    RhythmSessionSnapshot stopSession(UUID playerId, String playerName, String reason);

    Optional<RhythmSessionSnapshot> getSession(UUID playerId);
}
