package com.hyrhythm.content;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.content.interfaces.RhythmSongLibraryAccess;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.ui.RhythmChartUiAssetPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmSongLibraryIntegrationTest {
    private final RhythmSongLibraryHarness songLibraryHarness = new RhythmSongLibraryHarness();

    private DependencyLoader loader;
    private Path tempDataDirectory;

    @BeforeEach
    void setUp() throws Exception {
        tempDataDirectory = Files.createTempDirectory("hyrhythm-song-library-test");
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
    void builtInDebugChartLoadsIntoSongLibrary() {
        songLibraryHarness.loadRhythmSongLibrary();

        assertEquals(1, songLibraryHarness.listRhythmSongs().size());
        assertEquals("HyRhythm Debug Track", songLibraryHarness.listRhythmSongs().getFirst().title());
        assertEquals("debug/test-4k", songLibraryHarness.requireDebugRhythmChart().chartId());
        assertTrue(Files.isRegularFile(
            RhythmChartUiAssetPaths.assetFilePath(
                tempDataDirectory.resolve("rhythm").resolve("generated-asset-pack"),
                "debug/test-4k"
            )
        ));
    }

    @Test
    void importRegistersExternalOsuChartsFromSongsDirectory() throws Exception {
        Path songsDirectory = tempDataDirectory.resolve("rhythm").resolve("songs").resolve("tp-na-ame");
        Files.createDirectories(songsDirectory);
        Files.writeString(songsDirectory.resolve("tp-na-ame-hard.osu"), """
            osu file format v14

            [General]
            AudioFilename: tp-na-ame.ogg
            Mode: 3

            [Metadata]
            Title: tp na ame
            Artist: ZERATch
            Creator: mapper
            Version: Hard

            [Difficulty]
            CircleSize: 4
            OverallDifficulty: 7

            [TimingPoints]
            0,500,4,1,0,100,1,0

            [HitObjects]
            64,192,1000,1,0,0:0:0:0:
            192,192,1500,1,0,0:0:0:0:
            320,192,2000,1,0,0:0:0:0:
            448,192,2500,1,0,0:0:0:0:
            """);
        Files.writeString(songsDirectory.resolve("tp-na-ame.ogg"), "placeholder-audio");

        var result = songLibraryHarness.importRhythmSongsFromConfiguredDirectory();

        assertEquals(1, result.discoveredSourceCount());
        assertEquals(1, result.importedChartCount());
        assertEquals(1, result.importedSongCount());
        assertEquals(0, result.failedSourceCount());
        assertEquals(2, songLibraryHarness.listRhythmSongs().size());
        var importedChart = songLibraryHarness.findRhythmChartById("zeratch-tp-na-ame/hard").orElseThrow();
        assertEquals("tp na ame", importedChart.metadata().title());
        assertEquals("tp-na-ame.ogg", importedChart.metadata().audioFileName());
        assertTrue(songLibraryHarness.listRhythmSongs().stream().anyMatch(song -> song.songId().equals("zeratch-tp-na-ame")));
        assertEquals("Zeratch-tp-na-ame", songLibraryHarness.findRhythmSoundEventIdBySongId("zeratch-tp-na-ame").orElseThrow());
        assertTrue(Files.isRegularFile(
            tempDataDirectory.resolve("rhythm")
                .resolve("generated-asset-pack")
                .resolve("Server")
                .resolve("Audio")
                .resolve("SoundEvents")
                .resolve("HyRhythm")
                .resolve("Imported")
                .resolve("Zeratch-tp-na-ame.json")
        ));
        assertTrue(Files.isRegularFile(
            RhythmChartUiAssetPaths.assetFilePath(
                tempDataDirectory.resolve("rhythm").resolve("generated-asset-pack"),
                "zeratch-tp-na-ame/hard"
            )
        ));
    }

    private static final class RhythmSongLibraryHarness implements RhythmSongLibraryAccess {
    }
}
