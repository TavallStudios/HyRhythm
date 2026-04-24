package com.hyrhythm.content;

import com.hyrhythm.content.interfaces.RhythmChartImportAccess;
import com.hyrhythm.content.interfaces.RhythmChartImportService;
import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.logging.RhythmRuntimeLogger;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmOsuChartImportServiceTest {
    private final RhythmChartImportHarness importHarness = new RhythmChartImportHarness();

    @BeforeEach
    void setUp() {
        DependencyLoader loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();

        RhythmDebugState debugState = new RhythmDebugState();
        loader.registerImportantInstance(RhythmDebugState.class, debugState);
        loader.registerImportantInstance(RhythmLoggingService.class, new RhythmRuntimeLogger(null, debugState));
        loader.registerImportantInstance(RhythmChartImportService.class, new OsuManiaChartImportService());
    }

    @Test
    void importsBuiltInDebugOsuChartIntoInternalModel() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("content/charts/debug/test-4k.osu")) {
            RhythmChart chart = importHarness.importRhythmOsu("content/charts/debug/test-4k.osu", inputStream);

            assertEquals("debug/test-4k", chart.chartId());
            assertEquals(4, chart.keyMode());
            assertEquals("HyRhythm Debug Track", chart.metadata().title());
            assertEquals("debug-track.ogg", chart.metadata().audioFileName());
            assertEquals(2, chart.timingPoints().size());
            assertEquals(23, chart.notes().size());
            assertEquals(1L, chart.holdCount());
            assertEquals(3, chart.notes().get(2).lane());
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Test
    void importsOszArchivesContainingOsuDifficulties() throws Exception {
        Path archivePath = Files.createTempFile("hyrhythm-", ".osz");
        byte[] audioBytes = UUID.randomUUID().toString().getBytes();

        try (InputStream osuStream = getClass().getClassLoader().getResourceAsStream("content/charts/debug/test-4k.osu");
             ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            zipOutputStream.putNextEntry(new ZipEntry("test-4k.osu"));
            zipOutputStream.write(osuStream.readAllBytes());
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("debug-track.ogg"));
            zipOutputStream.write(audioBytes);
            zipOutputStream.closeEntry();
        }

        List<RhythmChart> charts = importHarness.importRhythmOsz(archivePath);
        assertEquals(1, charts.size());
        assertEquals("debug/test-4k", charts.getFirst().chartId());
    }

    @Test
    void rejectsUnsupportedModes() {
        String unsupportedChart = """
            osu file format v14

            [General]
            AudioFilename: debug-track.ogg
            Mode: 0

            [Metadata]
            Title: Unsupported
            Artist: HyRhythm
            Creator: HyRhythm
            Version: osu

            [Difficulty]
            CircleSize: 4
            OverallDifficulty: 5

            [TimingPoints]
            0,500,4,1,0,100,1,0

            [HitObjects]
            64,192,1000,1,0,0:0:0:0:
            """;

        assertThrows(
            IllegalArgumentException.class,
            () -> importHarness.importRhythmOsu("unsupported.osu", new java.io.ByteArrayInputStream(unsupportedChart.getBytes()))
        );
    }

    @Test
    void rejectsOsuSkinArchivesWithActionableMessage() throws Exception {
        Path archivePath = Files.createTempFile("hyrhythm-skin-", ".osk");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            zipOutputStream.putNextEntry(new ZipEntry("skin.ini"));
            zipOutputStream.write("[General]\nName=Test Skin".getBytes());
            zipOutputStream.closeEntry();
        }

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> importHarness.importRhythmOsz(archivePath)
        );

        assertTrue(exception.getMessage().contains("skin package (.osk)"));
    }

    private static final class RhythmChartImportHarness implements RhythmChartImportAccess {
    }
}
