package com.hyrhythm.gameplay;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hypixel.hytale.protocol.InteractionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmLaneInputRouterTest {
    private static final UUID PLAYER_ID = UUID.fromString("5d664cd0-3f1b-45aa-a4f9-9c4dd7c031df");
    private static final InteractionType[] DEFAULT_INTERACTIONS = {
        InteractionType.Ability1,
        InteractionType.Ability2,
        InteractionType.Ability3,
        InteractionType.Use,
        InteractionType.Ability1,
        InteractionType.Ability2,
        InteractionType.Use,
        InteractionType.Ability3,
        InteractionType.Ability1,
        InteractionType.Use,
        InteractionType.Ability2,
        InteractionType.Ability3,
        InteractionType.Ability2,
        InteractionType.Use,
        InteractionType.Ability1,
        InteractionType.Ability3,
        InteractionType.Ability2,
        InteractionType.Use,
        InteractionType.Ability1,
        InteractionType.Ability3,
        InteractionType.Ability2,
        InteractionType.Use,
        InteractionType.Ability1
    };
    private static final long[] INPUT_TIMES = {
        1000L, 1500L, 2000L, 3000L, 3500L, 4500L, 5500L, 6500L, 7500L, 8500L, 9000L, 9500L,
        10500L, 11500L, 12500L, 13500L, 14500L, 15500L, 16500L, 17500L, 18500L, 19500L, 20000L
    };

    private DependencyLoader loader;
    private RhythmSessionService sessionService;
    private RhythmGameplayService gameplayService;
    private RhythmSettingsService settingsService;
    private RhythmLaneInputRouter laneInputRouter;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-lane-router-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        sessionService = loader.requireInstance(RhythmSessionService.class);
        gameplayService = loader.requireInstance(RhythmGameplayService.class);
        settingsService = loader.requireInstance(RhythmSettingsService.class);
        laneInputRouter = loader.requireInstance(RhythmLaneInputRouter.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void defaultInteractionMappingCompletesDebugChart() {
        RhythmSessionSnapshot session = startDebugSession();
        long startedAtMillis = session.updatedAt().toEpochMilli();

        for (int index = 0; index < INPUT_TIMES.length; index++) {
            assertTrue(laneInputRouter.routeInteraction(PLAYER_ID, "RouterPlayer", DEFAULT_INTERACTIONS[index], startedAtMillis + INPUT_TIMES[index]));
        }

        var finalSnapshot = gameplayService.getLastGameplay(PLAYER_ID).orElseThrow();

        assertTrue(finalSnapshot.completed());
        assertEquals(24, finalSnapshot.combo());
        assertEquals(24, finalSnapshot.hitCount());
        assertTrue(finalSnapshot.heldLanes().isEmpty());
        assertEquals(RhythmSessionPhase.ENDED, sessionService.getSession(PLAYER_ID).orElseThrow().phase());
    }

    @Test
    void interactionRouterRespectsSwappedLaneBindings() {
        settingsService.updateLaneBinding(PLAYER_ID, "RouterPlayer", 1, "Use");
        RhythmSessionSnapshot session = startDebugSession();
        long startedAtMillis = session.updatedAt().toEpochMilli();

        InteractionType[] swappedInteractions = {
            InteractionType.Use,
            InteractionType.Ability2,
            InteractionType.Ability3,
            InteractionType.Ability1,
            InteractionType.Use,
            InteractionType.Ability2,
            InteractionType.Ability1,
            InteractionType.Ability3,
            InteractionType.Use,
            InteractionType.Ability1,
            InteractionType.Ability2,
            InteractionType.Ability3,
            InteractionType.Ability2,
            InteractionType.Ability1,
            InteractionType.Use,
            InteractionType.Ability3,
            InteractionType.Ability2,
            InteractionType.Ability1,
            InteractionType.Use,
            InteractionType.Ability3,
            InteractionType.Ability2,
            InteractionType.Ability1,
            InteractionType.Use
        };
        for (int index = 0; index < INPUT_TIMES.length; index++) {
            assertTrue(laneInputRouter.routeInteraction(PLAYER_ID, "RouterPlayer", swappedInteractions[index], startedAtMillis + INPUT_TIMES[index]));
        }

        var finalSnapshot = gameplayService.getLastGameplay(PLAYER_ID).orElseThrow();

        assertTrue(finalSnapshot.completed());
        assertEquals(24, finalSnapshot.combo());
        assertEquals("1=Use 2=Ability2 3=Ability3 4=Ability1", settingsService.getOrCreateSettings(PLAYER_ID, "RouterPlayer").keybinds().toDisplayString());
    }

    @Test
    void interactionRouterIgnoresInputsWithoutActiveGameplay() {
        assertFalse(laneInputRouter.routeInteraction(PLAYER_ID, "RouterPlayer", InteractionType.Ability1, System.currentTimeMillis()));
    }

    private RhythmSessionSnapshot startDebugSession() {
        sessionService.joinOrCreateSession(PLAYER_ID, "RouterPlayer");
        sessionService.selectChart(PLAYER_ID, "RouterPlayer", "debug/test-4k");
        return sessionService.startSession(PLAYER_ID, "RouterPlayer");
    }
}
