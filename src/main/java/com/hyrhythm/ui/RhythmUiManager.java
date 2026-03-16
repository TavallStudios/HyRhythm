package com.hyrhythm.ui;

import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import com.hyrhythm.ui.interfaces.RhythmUiService;
import com.hypixel.hytale.server.core.entity.entities.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public final class RhythmUiManager implements RhythmUiService {
    private final RhythmSongLibraryService songLibraryService;
    private final RhythmSessionService sessionService;
    private final RhythmSettingsService settingsService;
    private final RhythmSongSelectionPageFactory songSelectionPageFactory;
    private final RhythmGameplayPageFactory gameplayPageFactory;
    private final RhythmKeybindsPageFactory keybindsPageFactory;
    private final RhythmLoggingService loggingService;

    public RhythmUiManager(
        RhythmSongLibraryService songLibraryService,
        RhythmSessionService sessionService,
        RhythmSettingsService settingsService,
        RhythmSongSelectionPageFactory songSelectionPageFactory,
        RhythmGameplayPageFactory gameplayPageFactory,
        RhythmKeybindsPageFactory keybindsPageFactory,
        RhythmLoggingService loggingService
    ) {
        this.songLibraryService = Objects.requireNonNull(songLibraryService, "songLibraryService");
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.settingsService = Objects.requireNonNull(settingsService, "settingsService");
        this.songSelectionPageFactory = Objects.requireNonNull(songSelectionPageFactory, "songSelectionPageFactory");
        this.gameplayPageFactory = Objects.requireNonNull(gameplayPageFactory, "gameplayPageFactory");
        this.keybindsPageFactory = Objects.requireNonNull(keybindsPageFactory, "keybindsPageFactory");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
    }

    @Override
    public void openSongSelection(Player player) {
        Objects.requireNonNull(player, "player");

        List<RhythmSong> songs = songLibraryService.listSongs();
        if (songs.isEmpty()) {
            throw new IllegalStateException("No rhythm songs are registered.");
        }

        var world = player.getWorld();
        if (world == null) {
            throw new IllegalStateException("Player is not attached to a world, so the rhythm UI cannot open.");
        }

        RhythmSessionSnapshot session = sessionService.joinOrCreateSession(player.getUuid(), player.getDisplayName());
        loggingService.info(
            "ui",
            "song_selection_ui_requested",
            new LinkedHashMap<>() {{
                put("sessionId", session.sessionId());
                put("playerId", player.getUuid());
                put("player", player.getDisplayName());
                put("songCount", songs.size());
            }}
        );

        world.execute(() -> {
            if (!world.isAlive()) {
                loggingService.warn(
                    "ui",
                    "song_selection_ui_open_failed",
                    new LinkedHashMap<>() {{
                        put("sessionId", session.sessionId());
                        put("playerId", player.getUuid());
                        put("reason", "world_not_alive");
                    }}
                );
                return;
            }

            player.getPageManager().openCustomPage(
                player.getReference(),
                world.getEntityStore().getStore(),
                songSelectionPageFactory.create(player.getPlayerRef(), player.getUuid(), player.getDisplayName(), session, songs)
            );
            loggingService.info(
                "ui",
                "song_selection_ui_opened",
                new LinkedHashMap<>() {{
                    put("sessionId", session.sessionId());
                    put("playerId", player.getUuid());
                    put("player", player.getDisplayName());
                    put("chartId", session.chartId() == null ? "none" : session.chartId());
                }}
            );
        });
    }

    @Override
    public void openGameplay(Player player) {
        Objects.requireNonNull(player, "player");

        var world = player.getWorld();
        if (world == null) {
            throw new IllegalStateException("Player is not attached to a world, so the rhythm gameplay UI cannot open.");
        }

        RhythmSessionSnapshot session = sessionService.getSession(player.getUuid())
            .orElseThrow(() -> new IllegalStateException("No rhythm session exists. Use /rhythm join or /rhythm ui first."));
        if (session.chartId() == null || session.chartId().isBlank()) {
            throw new IllegalStateException("No chart is selected for the active rhythm session.");
        }

        var chart = songLibraryService.findChartById(session.chartId())
            .orElseThrow(() -> new IllegalStateException("Selected chart '" + session.chartId() + "' is no longer registered."));
        RhythmPlayerSettings settings = settingsService.getOrCreateSettings(player.getUuid(), player.getDisplayName());
        String soundEventId = songLibraryService.findSoundEventIdBySongId(chart.songId()).orElse(null);

        loggingService.info(
            "ui",
            "gameplay_ui_requested",
            new LinkedHashMap<>() {{
                put("sessionId", session.sessionId());
                put("playerId", player.getUuid());
                put("player", player.getDisplayName());
                put("chartId", session.chartId());
                put("phase", session.phase());
                put("binds", settings.keybinds().toDisplayString());
            }}
        );

        world.execute(() -> {
            if (!world.isAlive()) {
                loggingService.warn(
                    "ui",
                    "gameplay_ui_open_failed",
                    new LinkedHashMap<>() {{
                        put("sessionId", session.sessionId());
                        put("playerId", player.getUuid());
                        put("reason", "world_not_alive");
                    }}
                );
                return;
            }

            player.getPageManager().openCustomPage(
                player.getReference(),
                world.getEntityStore().getStore(),
                gameplayPageFactory.create(
                    player.getPlayerRef(),
                    player.getUuid(),
                    player.getDisplayName(),
                    session,
                    chart,
                    settings,
                    soundEventId
                )
            );
            loggingService.info(
                "ui",
                "gameplay_ui_opened",
                new LinkedHashMap<>() {{
                    put("sessionId", session.sessionId());
                    put("playerId", player.getUuid());
                    put("player", player.getDisplayName());
                    put("chartId", session.chartId());
                }}
            );
        });
    }

    @Override
    public void openKeybinds(Player player) {
        Objects.requireNonNull(player, "player");

        var world = player.getWorld();
        if (world == null) {
            throw new IllegalStateException("Player is not attached to a world, so the rhythm keybind UI cannot open.");
        }

        RhythmPlayerSettings settings = settingsService.getOrCreateSettings(player.getUuid(), player.getDisplayName());
        loggingService.info(
            "ui",
            "keybinds_ui_requested",
            new LinkedHashMap<>() {{
                put("playerId", player.getUuid());
                put("player", player.getDisplayName());
                put("laneKeys", settings.laneKeys().toDisplayString());
            }}
        );

        world.execute(() -> {
            if (!world.isAlive()) {
                loggingService.warn(
                    "ui",
                    "keybinds_ui_open_failed",
                    new LinkedHashMap<>() {{
                        put("playerId", player.getUuid());
                        put("reason", "world_not_alive");
                    }}
                );
                return;
            }

            player.getPageManager().openCustomPage(
                player.getReference(),
                world.getEntityStore().getStore(),
                keybindsPageFactory.create(
                    player.getPlayerRef(),
                    player.getUuid(),
                    player.getDisplayName(),
                    settings
                )
            );
            loggingService.info(
                "ui",
                "keybinds_ui_opened",
                new LinkedHashMap<>() {{
                    put("playerId", player.getUuid());
                    put("player", player.getDisplayName());
                    put("laneKeys", settings.laneKeys().toDisplayString());
                }}
            );
        });
    }
}
