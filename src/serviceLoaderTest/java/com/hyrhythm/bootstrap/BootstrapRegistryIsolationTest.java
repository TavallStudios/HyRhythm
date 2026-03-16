package com.hyrhythm.bootstrap;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.logging.RhythmRuntimeLogger;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BootstrapRegistryIsolationTest {
    @BeforeEach
    void setUp() {
        DependencyLoader.getFallbackDependencyLoader().resetInstances();
    }

    @Test
    void coreRegistryRegistersBootstrapServicesIntoLoader() {
        DependencyLoader loader = DependencyLoader.getFallbackDependencyLoader();

        new CoreBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        assertFalse(loader.isInstanceRegistered(com.hyrhythm.HyRhythmPlugin.class));
        assertNotNull(loader.requireInstance(RhythmDebugState.class));
        assertNotNull(loader.requireInstance(RhythmRuntimeLogger.class));
        assertNotNull(loader.requireInstance(RhythmLoggingService.class));
    }
}
