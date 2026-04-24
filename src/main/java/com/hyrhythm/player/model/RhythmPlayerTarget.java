package com.hyrhythm.player.model;

import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.Objects;
import java.util.UUID;

public record RhythmPlayerTarget(
    UUID playerId,
    String playerName,
    Player player
) {
    public RhythmPlayerTarget {
        playerId = Objects.requireNonNull(playerId, "playerId");
        playerName = Objects.requireNonNull(playerName, "playerName");
    }

    public boolean hasInteractivePlayer() {
        return player != null && player.getWorld() != null;
    }

    public String summary() {
        return playerName + " (" + playerId + ")";
    }
}
