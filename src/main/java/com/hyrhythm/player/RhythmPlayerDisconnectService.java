package com.hyrhythm.player;

import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;

public final class RhythmPlayerDisconnectService implements RhythmSessionAccess, RhythmGameplayAccess, RhythmLoggingAccess {
    public void handleDisconnect(UUID playerId, String playerName, String reason) {
        if (playerId == null) {
            throw new IllegalArgumentException("playerId");
        }
        String normalizedReason = normalizeReason(reason);
        Optional<RhythmSessionSnapshot> session = findRhythmSession(playerId);
        boolean activeGameplay = findActiveRhythmGameplay(playerId).isPresent();
        String resolvedPlayerName = resolvePlayerName(playerName, session);

        if (session.isEmpty() && !activeGameplay) {
            logRhythmDebug(
                "player",
                "disconnect_cleanup_skipped",
                fields(playerId, resolvedPlayerName, normalizedReason, "reason", "no_session_or_gameplay")
            );
            return;
        }

        if (session.isPresent() && session.get().phase() != RhythmSessionPhase.ENDED) {
            RhythmSessionSnapshot stopped = stopRhythmSession(playerId, resolvedPlayerName, normalizedReason);
            logRhythmInfo(
                "player",
                "disconnect_cleanup_completed",
                fields(playerId, resolvedPlayerName, normalizedReason, "sessionId", stopped.sessionId(), "phase", stopped.phase())
            );
            return;
        }

        if (activeGameplay) {
            stopRhythmGameplay(playerId, resolvedPlayerName, normalizedReason);
            logRhythmInfo(
                "player",
                "disconnect_gameplay_cleanup_completed",
                fields(playerId, resolvedPlayerName, normalizedReason, "phase", "gameplay_only")
            );
            return;
        }

        logRhythmDebug(
            "player",
            "disconnect_cleanup_skipped",
            fields(playerId, resolvedPlayerName, normalizedReason, "reason", "session_already_ended")
        );
    }

    private static String resolvePlayerName(String requestedName, Optional<RhythmSessionSnapshot> session) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName;
        }
        return session.map(RhythmSessionSnapshot::playerName).orElse("UnknownPlayer");
    }

    private static String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "player_disconnect";
        }
        return "disconnect_" + reason.trim().toLowerCase().replace(' ', '_');
    }

    private static LinkedHashMap<String, Object> fields(UUID playerId, String playerName, String reason, Object... extraValues) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("playerId", playerId);
        fields.put("player", playerName);
        fields.put("disconnectReason", reason);
        for (int index = 0; index + 1 < extraValues.length; index += 2) {
            fields.put(String.valueOf(extraValues[index]), extraValues[index + 1]);
        }
        return fields;
    }
}
