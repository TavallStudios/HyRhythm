package com.hyrhythm.session;

import com.hyrhythm.content.interfaces.RhythmSongLibraryAccess;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RhythmSessionManager implements
    RhythmSessionService,
    RhythmLoggingAccess,
    RhythmSongLibraryAccess,
    RhythmGameplayAccess {
    private final ConcurrentMap<UUID, RhythmSessionSnapshot> sessionsByPlayerId = new ConcurrentHashMap<>();

    @Override
    public RhythmSessionSnapshot joinOrCreateSession(UUID playerId, String playerName) {
        validatePlayer(playerId, playerName);

        RhythmSessionSnapshot existing = sessionsByPlayerId.get(playerId);
        if (existing != null && existing.phase() != RhythmSessionPhase.ENDED) {
            logRhythmInfo(
                "session",
                "player_joined_existing_session",
                sessionFields(existing, "player", playerName)
            );
            return existing;
        }

        RhythmSessionSnapshot created = RhythmSessionSnapshot.create(playerId, playerName);
        sessionsByPlayerId.put(playerId, created);
        logRhythmInfo(
            "session",
            "match_created",
            sessionFields(created, "player", playerName)
        );
        return created;
    }

    @Override
    public RhythmSessionSnapshot prepareSelfTestSession(UUID playerId, String playerName) {
        String debugChartId = requireDebugRhythmChart().chartId();
        RhythmSessionSnapshot session = joinOrCreateSession(playerId, playerName)
            .withChartId(debugChartId)
            .withPhase(RhythmSessionPhase.READY);
        sessionsByPlayerId.put(playerId, session);
        logRhythmInfo(
            "session",
            "debug_chart_selected",
            sessionFields(session, "chartId", debugChartId)
        );
        return session;
    }

    @Override
    public RhythmSessionSnapshot selectChart(UUID playerId, String playerName, String chartId) {
        RhythmSessionSnapshot session = requireSession(playerId, playerName);
        String normalizedChartId = chartId == null ? "" : chartId.trim();
        if (normalizedChartId.isEmpty()) {
            throw new IllegalArgumentException("chartId");
        }

        var chart = findRhythmChartById(normalizedChartId)
            .orElseThrow(() -> new IllegalStateException("Chart '" + normalizedChartId + "' is not registered in the song library."));
        RhythmSessionSnapshot updatedSession = session
            .withChartId(chart.chartId())
            .withPhase(RhythmSessionPhase.READY);
        sessionsByPlayerId.put(playerId, updatedSession);
        logRhythmInfo(
            "session",
            "chart_selected",
            new LinkedHashMap<>() {{
                put("sessionId", updatedSession.sessionId());
                put("playerId", updatedSession.playerId());
                put("player", updatedSession.playerName());
                put("phase", updatedSession.phase());
                put("chartId", chart.chartId());
                put("songId", chart.songId());
                put("keyMode", chart.keyMode());
                put("difficulty", chart.metadata().difficultyName());
            }}
        );
        return updatedSession;
    }

    @Override
    public RhythmSessionSnapshot startSession(UUID playerId, String playerName) {
        RhythmSessionSnapshot session = requireSession(playerId, playerName);
        if (session.phase() == RhythmSessionPhase.PLAYING) {
            throw new IllegalStateException("Session is already playing.");
        }
        if (session.phase() == RhythmSessionPhase.ENDED) {
            throw new IllegalStateException("Session has already ended. Use /rhythm join or /rhythm test to create a new run.");
        }
        if (session.chartId() == null) {
            throw new IllegalStateException("No chart selected. Use /rhythm ui or /rhythm test first.");
        }
        var chart = findRhythmChartById(session.chartId())
            .orElseThrow(() -> new IllegalStateException("Selected chart '" + session.chartId() + "' is not registered in the song library."));

        startRhythmGameplay(playerId, playerName, session.sessionId(), chart.chartId());

        RhythmSessionSnapshot started = session.withPhase(RhythmSessionPhase.PLAYING);
        sessionsByPlayerId.put(playerId, started);
        logRhythmInfo(
            "session",
            "gameplay_transition_completed",
            new LinkedHashMap<>() {{
                put("sessionId", started.sessionId());
                put("playerId", started.playerId());
                put("player", started.playerName());
                put("phase", started.phase());
                put("chartId", chart.chartId());
                put("songId", chart.songId());
                put("difficulty", chart.metadata().difficultyName());
            }}
        );
        return started;
    }

    @Override
    public RhythmSessionSnapshot completeSession(UUID playerId, String playerName, String reason) {
        RhythmSessionSnapshot session = requireSession(playerId, playerName);
        RhythmSessionSnapshot ended = session.withPhase(RhythmSessionPhase.ENDED);
        sessionsByPlayerId.put(playerId, ended);
        logRhythmInfo(
            "session",
            "session_completed",
            sessionFields(ended, "reason", reason)
        );
        return ended;
    }

    @Override
    public RhythmSessionSnapshot stopSession(UUID playerId, String playerName, String reason) {
        RhythmSessionSnapshot session = requireSession(playerId, playerName);
        if (findActiveRhythmGameplay(playerId).isPresent()) {
            stopRhythmGameplay(playerId, playerName, reason);
        }
        RhythmSessionSnapshot ended = session.withPhase(RhythmSessionPhase.ENDED);
        sessionsByPlayerId.put(playerId, ended);
        logRhythmInfo(
            "session",
            "session_stopped",
            sessionFields(ended, "reason", reason)
        );
        return ended;
    }

    @Override
    public java.util.Optional<RhythmSessionSnapshot> getSession(UUID playerId) {
        return java.util.Optional.ofNullable(sessionsByPlayerId.get(playerId));
    }

    private RhythmSessionSnapshot requireSession(UUID playerId, String playerName) {
        validatePlayer(playerId, playerName);

        RhythmSessionSnapshot session = sessionsByPlayerId.get(playerId);
        if (session == null) {
            throw new IllegalStateException("No active rhythm session. Use /rhythm join first.");
        }
        return session;
    }

    private static void validatePlayer(UUID playerId, String playerName) {
        Objects.requireNonNull(playerId, "playerId");
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("playerName");
        }
    }

    private static LinkedHashMap<String, Object> sessionFields(
        RhythmSessionSnapshot session,
        String extraField,
        Object extraValue
    ) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("sessionId", session.sessionId());
        fields.put("playerId", session.playerId());
        fields.put("player", session.playerName());
        fields.put("phase", session.phase());
        fields.put(extraField, extraValue);
        return fields;
    }
}
