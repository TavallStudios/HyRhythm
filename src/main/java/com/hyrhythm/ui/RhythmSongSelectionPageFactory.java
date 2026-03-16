package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RhythmSongSelectionPageFactory {
    private final RhythmSessionService sessionService;
    private final RhythmLoggingService loggingService;

    public RhythmSongSelectionPageFactory(
        RhythmSessionService sessionService,
        RhythmLoggingService loggingService
    ) {
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
    }

    public RhythmSongSelectionPage create(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmSessionSnapshot session,
        List<RhythmSong> songs
    ) {
        return new RhythmSongSelectionPage(
            playerRef,
            playerId,
            playerName,
            session,
            songs,
            sessionService,
            loggingService
        );
    }
}
