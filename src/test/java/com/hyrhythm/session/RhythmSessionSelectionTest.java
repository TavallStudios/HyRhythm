package com.hyrhythm.session;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.session.model.RhythmSessionPhase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RhythmSessionSelectionTest implements RhythmSessionAccess {
    private static final UUID PLAYER_ID = UUID.fromString("1cad8a83-e795-4341-a2c2-aeb496df11fe");

    private DependencyLoader loader;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-session-selection-test");
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
    void selectingChartTransitionsSessionToReadyThroughAccessInterface() {
        joinOrCreateRhythmSession(PLAYER_ID, "RhythmSelector");

        var selectedSession = selectRhythmChart(PLAYER_ID, "RhythmSelector", "debug/test-4k");

        assertEquals("debug/test-4k", selectedSession.chartId());
        assertEquals(RhythmSessionPhase.READY, selectedSession.phase());
        assertEquals("debug/test-4k", findRhythmSession(PLAYER_ID).orElseThrow().chartId());
    }
}
