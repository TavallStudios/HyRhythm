package com.hyrhythm.player;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RhythmPlayerDisconnectServiceIntegrationTest {
    private static final UUID PLAYER_ID = UUID.fromString("f52b1587-355c-49eb-872f-b27ad0d416a8");

    private DependencyLoader loader;
    private RhythmSessionService sessionService;
    private RhythmGameplayService gameplayService;
    private RhythmPlayerDisconnectService disconnectService;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-player-disconnect-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        sessionService = loader.requireInstance(RhythmSessionService.class);
        gameplayService = loader.requireInstance(RhythmGameplayService.class);
        disconnectService = loader.requireInstance(RhythmPlayerDisconnectService.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void disconnectStopsGameplayAndEndsSession() {
        sessionService.joinOrCreateSession(PLAYER_ID, "UiDisconnect");
        sessionService.selectChart(PLAYER_ID, "UiDisconnect", "debug/test-4k");
        sessionService.startSession(PLAYER_ID, "UiDisconnect");

        disconnectService.handleDisconnect(PLAYER_ID, "UiDisconnect", "TimedOut");

        assertFalse(gameplayService.getActiveGameplay(PLAYER_ID).isPresent());
        assertEquals(RhythmSessionPhase.ENDED, sessionService.getSession(PLAYER_ID).orElseThrow().phase());
    }
}
