package com.hyrhythm.bootstrap;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.command.RhythmCommandRouter;
import com.hyrhythm.content.interfaces.RhythmChartImportService;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmBootstrapRegistryIsolationTest {
    private Path tempDataDirectory;

    @BeforeEach
    void setUp() throws Exception {
        DependencyLoader.getFallbackDependencyLoader().resetInstances();
        tempDataDirectory = Files.createTempDirectory("hyrhythm-registry-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
    }

    @Test
    void rhythmRegistryRegistersCommandAndServicesIntoLoader() {
        DependencyLoader loader = DependencyLoader.getFallbackDependencyLoader();

        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        assertTrue(loader.isInstanceRegistered(RhythmSettingsService.class));
        assertTrue(loader.isInstanceRegistered(RhythmChartImportService.class));
        assertTrue(loader.isInstanceRegistered(RhythmSongLibraryService.class));
        assertTrue(loader.isInstanceRegistered(RhythmSessionService.class));
        assertTrue(loader.isInstanceRegistered(RhythmCommandRouter.class));
    }
}
