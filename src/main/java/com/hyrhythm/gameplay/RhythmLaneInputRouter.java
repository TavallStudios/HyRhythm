package com.hyrhythm.gameplay;

import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.interfaces.RhythmSettingsAccess;
import com.hyrhythm.settings.model.RhythmInputChannel;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public final class RhythmLaneInputRouter implements RhythmGameplayAccess, RhythmSettingsAccess, RhythmSessionAccess, RhythmLoggingAccess {
    public boolean handlePlayerInteraction(PlayerInteractEvent event) {
        Objects.requireNonNull(event, "event");

        Player player = Objects.requireNonNull(event.getPlayer(), "event.player");
        boolean consumed = routeInteraction(
            player.getUuid(),
            player.getDisplayName(),
            event.getActionType(),
            System.currentTimeMillis()
        );
        if (consumed) {
            event.setCancelled(true);
        }
        return consumed;
    }

    boolean routeInteraction(UUID playerId, String playerName, InteractionType interactionType, long receivedAtEpochMillis) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(interactionType, "interactionType");

        RhythmInputChannel inputChannel = RhythmInputChannel.fromInteractionType(interactionType).orElse(null);
        if (inputChannel == null) {
            return false;
        }
        if (findActiveRhythmGameplay(playerId).isEmpty()) {
            return false;
        }

        RhythmSessionSnapshot session = findRhythmSession(playerId).orElse(null);
        if (session == null || session.phase() != RhythmSessionPhase.PLAYING) {
            return false;
        }

        int lane = getOrCreateRhythmPlayerSettings(playerId, playerName).keybinds().laneForInteraction(inputChannel);
        if (lane == 0) {
            logRhythmDebug(
                "input",
                "interaction_unmapped_for_player",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("player", safePlayerName(playerName));
                    put("interactionType", interactionType);
                    put("inputChannel", inputChannel.displayName());
                }}
            );
            return false;
        }

        long songTimeMillis = Math.max(0L, receivedAtEpochMillis - session.updatedAt().toEpochMilli());
        submitRhythmLaneInput(playerId, playerName, RhythmLaneInputAction.DOWN, lane, songTimeMillis);
        logRhythmInfo(
            "input",
            "interaction_routed_to_lane",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("interactionType", interactionType);
                put("inputChannel", inputChannel.displayName());
                put("lane", lane);
                put("songTimeMs", songTimeMillis);
                put("sessionStartedAt", session.updatedAt());
                put("receivedAt", Instant.ofEpochMilli(receivedAtEpochMillis));
            }}
        );
        return true;
    }

    private static String safePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "unknown";
        }
        return playerName;
    }
}
