package com.hyrhythm.gameplay;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayAccess;
import com.hyrhythm.gameplay.model.RhythmJudgmentType;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionPhase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmGameplayTest implements RhythmSessionAccess, RhythmGameplayAccess {
    private static final UUID PLAYER_ID = UUID.fromString("ee0193d9-1f4a-42e2-ac80-3f31c3326301");
    private static final long[] PERFECT_TAP_TIMES = {
        1000L, 1500L, 2000L, 3000L, 3500L, 4500L, 5500L, 6500L, 7500L, 8500L, 9000L, 9500L,
        10500L, 11500L, 12500L, 13500L, 14500L, 15500L, 16500L, 17500L, 18500L, 19500L, 20000L
    };
    private static final int[] PERFECT_TAP_LANES = {
        1, 2, 3, 4, 1, 2, 4, 3, 1, 4, 2, 3,
        2, 4, 1, 3, 2, 4, 1, 3, 2, 4, 1
    };

    private DependencyLoader loader;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-gameplay-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void perfectPlayTransitionsSessionToEndedAndStoresCompletedGameplaySnapshot() {
        prepareRhythmSelfTest(PLAYER_ID, "PerfectPlayer");
        startRhythmSession(PLAYER_ID, "PerfectPlayer");

        for (int index = 0; index < 3; index++) {
            assertEquals(
                RhythmJudgmentType.PERFECT,
                submitRhythmLaneInput(
                    PLAYER_ID,
                    "PerfectPlayer",
                    RhythmLaneInputAction.DOWN,
                    PERFECT_TAP_LANES[index],
                    PERFECT_TAP_TIMES[index]
                ).judgment().type()
            );
        }

        var afterHold = advanceRhythmGameplay(PLAYER_ID, "PerfectPlayer", 2600L);
        assertEquals(RhythmJudgmentType.HOLD_OK, afterHold.lastJudgment());

        var finalInput = afterHold;
        for (int index = 3; index < PERFECT_TAP_TIMES.length; index++) {
            finalInput = submitRhythmLaneInput(
                PLAYER_ID,
                "PerfectPlayer",
                RhythmLaneInputAction.DOWN,
                PERFECT_TAP_LANES[index],
                PERFECT_TAP_TIMES[index]
            ).snapshot();
        }

        assertTrue(finalInput.completed());
        assertEquals(24, finalInput.combo());
        assertEquals(24, finalInput.hitCount());
        assertEquals(0, finalInput.missCount());
        assertFalse(findActiveRhythmGameplay(PLAYER_ID).isPresent());
        assertTrue(findLastRhythmGameplay(PLAYER_ID).orElseThrow().completed());
        assertEquals(RhythmSessionPhase.ENDED, findRhythmSession(PLAYER_ID).orElseThrow().phase());
    }

    @Test
    void earlyHoldReleaseBreaksComboAndAutoMissCompletesChart() {
        prepareRhythmSelfTest(PLAYER_ID, "ReleaseTester");
        startRhythmSession(PLAYER_ID, "ReleaseTester");

        submitRhythmLaneInput(PLAYER_ID, "ReleaseTester", RhythmLaneInputAction.DOWN, 1, 1000L);
        submitRhythmLaneInput(PLAYER_ID, "ReleaseTester", RhythmLaneInputAction.DOWN, 2, 1500L);
        submitRhythmLaneInput(PLAYER_ID, "ReleaseTester", RhythmLaneInputAction.DOWN, 3, 2000L);

        var release = submitRhythmLaneInput(PLAYER_ID, "ReleaseTester", RhythmLaneInputAction.UP, 3, 2200L);
        assertEquals(RhythmJudgmentType.EARLY_RELEASE, release.judgment().type());
        assertEquals(0, release.snapshot().combo());

        var completed = advanceRhythmGameplay(PLAYER_ID, "ReleaseTester", 20600L);
        assertTrue(completed.completed());
        assertEquals(RhythmJudgmentType.MISS, completed.lastJudgment());
        assertEquals(21, completed.missCount());
        assertEquals(RhythmSessionPhase.ENDED, findRhythmSession(PLAYER_ID).orElseThrow().phase());
    }

    @Test
    void ghostTapReturnsDebugSummaryWithCandidateState() {
        prepareRhythmSelfTest(PLAYER_ID, "DebugTester");
        startRhythmSession(PLAYER_ID, "DebugTester");

        var result = submitRhythmLaneInput(
            PLAYER_ID,
            "DebugTester",
            RhythmLaneInputAction.DOWN,
            1,
            600L
        );

        assertEquals(RhythmJudgmentType.GHOST_TAP, result.judgment().type());
        assertTrue(result.debugSummary().contains("outside_window_early"));
        assertTrue(result.debugSummary().contains("candidateBefore=note-1:head"));
    }
}
