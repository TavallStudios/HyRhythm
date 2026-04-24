package com.hyrhythm.settings;

import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.logging.RhythmRuntimeLogger;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.settings.interfaces.RhythmSettingsAccess;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmPlayerSettingsServiceTest {
    private final RhythmSettingsHarness settingsHarness = new RhythmSettingsHarness();
    private Path tempDataDirectory;

    @BeforeEach
    void setUp() throws Exception {
        tempDataDirectory = Files.createTempDirectory("hyrhythm-settings-test");
        installLoader(tempDataDirectory);
    }

    @Test
    void persistsUpdatedKeybindsAcrossServiceReload() {
        UUID playerId = UUID.randomUUID();

        RhythmPlayerSettings defaults = settingsHarness.getOrCreateRhythmPlayerSettings(playerId, "Tester");
        assertEquals("1=Ability1 2=Ability2 3=Ability3 4=Use", defaults.keybinds().toDisplayString());

        RhythmPlayerSettings updated = settingsHarness.updateRhythmLaneBinding(playerId, "Tester", 1, "Use");
        assertEquals("Use", updated.keybinds().lane1Input().displayName());

        installLoader(tempDataDirectory);
        RhythmPlayerSettings reloaded = settingsHarness.getOrCreateRhythmPlayerSettings(playerId, "Tester");
        assertEquals("Use", reloaded.keybinds().lane1Input().displayName());
        assertTrue(
            Files.exists(tempDataDirectory.resolve("rhythm").resolve("player-settings").resolve(playerId + ".properties"))
        );
    }

    @Test
    void persistsUpdatedLaneKeysAcrossServiceReload() {
        UUID playerId = UUID.randomUUID();

        RhythmPlayerSettings defaults = settingsHarness.getOrCreateRhythmPlayerSettings(playerId, "Tester");
        assertEquals("1=D 2=F 3=J 4=K", defaults.laneKeys().toDisplayString());

        RhythmPlayerSettings updated = settingsHarness.updateRhythmLaneKey(playerId, "Tester", 1, "L");
        assertEquals("L", updated.laneKeys().lane1Key());

        installLoader(tempDataDirectory);
        RhythmPlayerSettings reloaded = settingsHarness.getOrCreateRhythmPlayerSettings(playerId, "Tester");
        assertEquals("L", reloaded.laneKeys().lane1Key());
    }

    private static void installLoader(Path tempDataDirectory) {
        DependencyLoader loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();

        RhythmDebugState debugState = new RhythmDebugState();
        loader.registerImportantInstance(RhythmDebugState.class, debugState);
        loader.registerImportantInstance(RhythmLoggingService.class, new RhythmRuntimeLogger(null, debugState));
        loader.registerImportantInstance(RhythmStoragePaths.class, new RhythmStoragePaths(tempDataDirectory));
        loader.registerImportantInstance(
            RhythmPlayerSettingsStore.class,
            new RhythmPlayerSettingsStore(loader.requireInstance(RhythmStoragePaths.class))
        );
        loader.registerImportantInstance(
            RhythmSettingsService.class,
            new RhythmPlayerSettingsService(loader.requireInstance(RhythmPlayerSettingsStore.class))
        );
    }

    private static final class RhythmSettingsHarness implements RhythmSettingsAccess {
    }
}
