package com.hyrhythm.player;

import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.player.interfaces.RhythmPlayerTargetService;
import com.hyrhythm.player.model.RhythmPlayerTarget;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RhythmOnlinePlayerDirectory implements RhythmPlayerTargetService, RhythmLoggingAccess {
    @Override
    public Optional<RhythmPlayerTarget> findOnlinePlayer(String lookup) {
        String normalizedLookup = lookup == null ? "" : lookup.trim();
        if (normalizedLookup.isEmpty()) {
            return Optional.empty();
        }

        Universe universe = Universe.get();
        if (universe == null) {
            logRhythmDebug("player", "online_player_lookup_skipped", fields("lookup", normalizedLookup, "reason", "universe_unavailable"));
            return Optional.empty();
        }

        PlayerRef playerRef = universe.getPlayer(normalizedLookup, NameMatching.STARTS_WITH_IGNORE_CASE);
        if (playerRef == null) {
            logRhythmDebug("player", "online_player_not_found", fields("lookup", normalizedLookup));
            return Optional.empty();
        }

        RhythmPlayerTarget target = toTarget(playerRef);
        logRhythmDebug(
            "player",
            "online_player_resolved",
            fields("lookup", normalizedLookup, "playerId", target.playerId(), "player", target.playerName())
        );
        return Optional.of(target);
    }

    @Override
    public List<RhythmPlayerTarget> listOnlinePlayers() {
        Universe universe = Universe.get();
        if (universe == null) {
            return List.of();
        }

        return universe.getPlayers().stream()
            .filter(Objects::nonNull)
            .map(RhythmOnlinePlayerDirectory::toTarget)
            .sorted(Comparator.comparing(RhythmPlayerTarget::playerName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private static RhythmPlayerTarget toTarget(PlayerRef playerRef) {
        Player player = playerRef.getComponent(Player.getComponentType());
        String playerName = player == null ? playerRef.getUsername() : player.getDisplayName();
        return new RhythmPlayerTarget(playerRef.getUuid(), playerName, player);
    }

    private static LinkedHashMap<String, Object> fields(Object... entries) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        for (int index = 0; index + 1 < entries.length; index += 2) {
            fields.put(String.valueOf(entries[index]), entries[index + 1]);
        }
        return fields;
    }
}
