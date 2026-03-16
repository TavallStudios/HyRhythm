package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Objects;
import java.util.UUID;

public final class RhythmGameplayPageFactory {
    private final RhythmGameplayService gameplayService;
    private final RhythmSessionService sessionService;
    private final RhythmGameplayUiScheduler uiScheduler;
    private final RhythmLoggingService loggingService;

    public RhythmGameplayPageFactory(
        RhythmGameplayService gameplayService,
        RhythmSessionService sessionService,
        RhythmGameplayUiScheduler uiScheduler,
        RhythmLoggingService loggingService
    ) {
        this.gameplayService = Objects.requireNonNull(gameplayService, "gameplayService");
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.uiScheduler = Objects.requireNonNull(uiScheduler, "uiScheduler");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
    }

    public RhythmGameplayPage create(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmSessionSnapshot session,
        RhythmChart chart,
        RhythmPlayerSettings settings,
        String soundEventId
    ) {
        return new RhythmGameplayPage(
            playerRef,
            playerId,
            playerName,
            session,
            chart,
            settings,
            gameplayService,
            sessionService,
            uiScheduler,
            loggingService,
            soundEventId
        );
    }
}
