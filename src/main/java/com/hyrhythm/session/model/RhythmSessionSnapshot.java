package com.hyrhythm.session.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RhythmSessionSnapshot(
    String sessionId,
    UUID playerId,
    String playerName,
    RhythmSessionPhase phase,
    String chartId,
    Instant createdAt,
    Instant updatedAt
) {
    public RhythmSessionSnapshot {
        sessionId = Objects.requireNonNull(sessionId, "sessionId");
        playerId = Objects.requireNonNull(playerId, "playerId");
        playerName = Objects.requireNonNull(playerName, "playerName");
        phase = Objects.requireNonNull(phase, "phase");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static RhythmSessionSnapshot create(UUID playerId, String playerName) {
        Instant now = Instant.now();
        return new RhythmSessionSnapshot(
            UUID.randomUUID().toString(),
            playerId,
            playerName,
            RhythmSessionPhase.LOBBY,
            null,
            now,
            now
        );
    }

    public RhythmSessionSnapshot withPhase(RhythmSessionPhase newPhase) {
        return new RhythmSessionSnapshot(sessionId, playerId, playerName, newPhase, chartId, createdAt, Instant.now());
    }

    public RhythmSessionSnapshot withChartId(String newChartId) {
        return new RhythmSessionSnapshot(sessionId, playerId, playerName, phase, newChartId, createdAt, Instant.now());
    }

    public String summary() {
        return "session=" + sessionId + " phase=" + phase + " chart=" + (chartId == null ? "none" : chartId);
    }
}
