package com.hyrhythm.settings;

import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.settings.model.RhythmKeybinds;
import com.hyrhythm.settings.model.RhythmLaneKeys;
import com.hyrhythm.settings.model.RhythmPlayerSettings;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RhythmPlayerSettingsService implements RhythmSettingsService, RhythmLoggingAccess {
    private final RhythmPlayerSettingsStore store;
    private final ConcurrentMap<UUID, RhythmPlayerSettings> settingsByPlayerId = new ConcurrentHashMap<>();

    public RhythmPlayerSettingsService(RhythmPlayerSettingsStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public RhythmPlayerSettings getOrCreateSettings(UUID playerId, String playerName) {
        Objects.requireNonNull(playerId, "playerId");
        return settingsByPlayerId.computeIfAbsent(playerId, ignored -> loadOrCreateDefaults(playerId, playerName));
    }

    @Override
    public RhythmPlayerSettings updateLaneBinding(UUID playerId, String playerName, int lane, String inputChannel) {
        validateLane(lane);

        RhythmPlayerSettings current = getOrCreateSettings(playerId, playerName);
        RhythmKeybinds updatedKeybinds = current.keybinds().withLaneBinding(lane, inputChannel);
        RhythmPlayerSettings updatedSettings = current.withKeybinds(updatedKeybinds);

        settingsByPlayerId.put(playerId, updatedSettings);
        store.save(playerId, updatedSettings);
        logRhythmInfo(
            "keybinds",
            "keybind_update",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("lane", lane);
                put("inputChannel", updatedKeybinds.inputForLane(lane).displayName());
                put("resultingBinds", updatedKeybinds.toDisplayString());
            }}
        );
        return updatedSettings;
    }

    @Override
    public RhythmPlayerSettings updateLaneKey(UUID playerId, String playerName, int lane, String key) {
        validateLane(lane);

        RhythmPlayerSettings current = getOrCreateSettings(playerId, playerName);
        RhythmLaneKeys updatedLaneKeys = current.laneKeys().withLaneKey(lane, key);
        RhythmPlayerSettings updatedSettings = current.withLaneKeys(updatedLaneKeys);

        settingsByPlayerId.put(playerId, updatedSettings);
        store.save(playerId, updatedSettings);
        logRhythmInfo(
            "keybinds",
            "lane_key_update",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("lane", lane);
                put("key", updatedLaneKeys.keyForLane(lane));
                put("resultingKeys", updatedLaneKeys.toDisplayString());
            }}
        );
        return updatedSettings;
    }

    @Override
    public RhythmPlayerSettings resetKeybinds(UUID playerId, String playerName) {
        RhythmPlayerSettings current = getOrCreateSettings(playerId, playerName);
        RhythmPlayerSettings resetSettings = new RhythmPlayerSettings(
            RhythmKeybinds.defaults(),
            current.laneKeys(),
            current.globalOffsetMs(),
            current.scrollSpeed()
        );
        settingsByPlayerId.put(playerId, resetSettings);
        store.save(playerId, resetSettings);
        logRhythmInfo(
            "keybinds",
            "keybind_reset",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("resultingBinds", resetSettings.keybinds().toDisplayString());
            }}
        );
        return resetSettings;
    }

    @Override
    public RhythmPlayerSettings resetLaneKeys(UUID playerId, String playerName) {
        RhythmPlayerSettings current = getOrCreateSettings(playerId, playerName);
        RhythmPlayerSettings resetSettings = new RhythmPlayerSettings(
            current.keybinds(),
            RhythmLaneKeys.defaults(),
            current.globalOffsetMs(),
            current.scrollSpeed()
        );
        settingsByPlayerId.put(playerId, resetSettings);
        store.save(playerId, resetSettings);
        logRhythmInfo(
            "keybinds",
            "lane_keys_reset",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("resultingKeys", resetSettings.laneKeys().toDisplayString());
            }}
        );
        return resetSettings;
    }

    @Override
    public String describeSettings(UUID playerId, String playerName) {
        RhythmPlayerSettings settings = getOrCreateSettings(playerId, playerName);
        return "keys=" + settings.laneKeys().toDisplayString()
            + " | inputs=" + settings.keybinds().toDisplayString()
            + " | offset=" + settings.globalOffsetMs() + "ms"
            + " | scroll=" + settings.scrollSpeed() + "x";
    }

    private RhythmPlayerSettings loadOrCreateDefaults(UUID playerId, String playerName) {
        RhythmPlayerSettings loaded = store.load(playerId).orElse(null);
        if (loaded != null) {
            logRhythmInfo(
                "keybinds",
                "current_binds_loaded",
                new LinkedHashMap<>() {{
                    put("playerId", playerId);
                    put("player", safePlayerName(playerName));
                    put("binds", loaded.keybinds().toDisplayString());
                    put("keys", loaded.laneKeys().toDisplayString());
                }}
            );
            return loaded;
        }

        RhythmPlayerSettings defaults = RhythmPlayerSettings.defaults();
        store.save(playerId, defaults);
        logRhythmInfo(
            "keybinds",
            "default_binds_applied",
            new LinkedHashMap<>() {{
                put("playerId", playerId);
                put("player", safePlayerName(playerName));
                put("binds", defaults.keybinds().toDisplayString());
                put("keys", defaults.laneKeys().toDisplayString());
            }}
        );
        return defaults;
    }

    private static void validateLane(int lane) {
        if (lane < 1 || lane > 4) {
            throw new IllegalArgumentException("Lane must be between 1 and 4.");
        }
    }

    private static String safePlayerName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return "unknown";
        }
        return playerName;
    }
}
