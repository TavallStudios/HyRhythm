package com.hyrhythm.ui;

import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Objects;
import java.util.UUID;

public final class RhythmKeybindsPageFactory {
    private final RhythmSettingsService settingsService;
    private final RhythmLoggingService loggingService;

    public RhythmKeybindsPageFactory(
        RhythmSettingsService settingsService,
        RhythmLoggingService loggingService
    ) {
        this.settingsService = Objects.requireNonNull(settingsService, "settingsService");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
    }

    public RhythmKeybindsPage create(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmPlayerSettings settings
    ) {
        return new RhythmKeybindsPage(
            playerRef,
            playerId,
            playerName,
            settings,
            settingsService,
            loggingService
        );
    }
}
