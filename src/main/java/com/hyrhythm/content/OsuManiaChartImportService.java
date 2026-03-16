package com.hyrhythm.content;

import com.hyrhythm.content.interfaces.RhythmChartImportService;
import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmChartMetadata;
import com.hyrhythm.content.model.RhythmNote;
import com.hyrhythm.content.model.RhythmTimingPoint;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class OsuManiaChartImportService implements RhythmChartImportService, RhythmLoggingAccess {
    @Override
    public RhythmChart importOsu(String sourceName, InputStream inputStream) {
        Objects.requireNonNull(sourceName, "sourceName");
        Objects.requireNonNull(inputStream, "inputStream");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return parseOsu(sourceName, reader.lines().toList());
        } catch (IOException exception) {
            logRhythmError("import", "osu_read_failed", Map.of("source", sourceName), exception);
            throw new IllegalStateException("Failed to read osu chart " + sourceName, exception);
        }
    }

    @Override
    public List<RhythmChart> importOsz(Path archivePath) {
        Objects.requireNonNull(archivePath, "archivePath");
        validateArchivePath(archivePath);

        List<RhythmChart> charts = new ArrayList<>();
        try (InputStream inputStream = Files.newInputStream(archivePath);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().toLowerCase(Locale.ROOT).endsWith(".osu")) {
                    zipInputStream.closeEntry();
                    continue;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                zipInputStream.transferTo(buffer);
                RhythmChart chart = importOsu(entry.getName(), new ByteArrayInputStream(buffer.toByteArray()));
                charts.add(chart);
                zipInputStream.closeEntry();
            }
        } catch (IOException exception) {
            logRhythmError("import", "osz_read_failed", Map.of("archive", archivePath), exception);
            throw new IllegalStateException("Failed to read osz archive " + archivePath, exception);
        }

        if (charts.isEmpty()) {
            throw new IllegalArgumentException("Archive " + archivePath + " does not contain any .osu difficulties.");
        }

        logRhythmInfo(
            "import",
            "osz_archive_loaded",
            new LinkedHashMap<>() {{
                put("archive", archivePath);
                put("chartCount", charts.size());
            }}
        );
        return List.copyOf(charts);
    }

    private static void validateArchivePath(Path archivePath) {
        Path fileName = archivePath.getFileName();
        String normalizedName = fileName == null ? archivePath.toString().toLowerCase(Locale.ROOT) : fileName.toString().toLowerCase(Locale.ROOT);
        if (normalizedName.endsWith(".osk")) {
            throw new IllegalArgumentException(
                "Archive " + archivePath + " is an osu! skin package (.osk), not a beatmap archive. Use .osz or .osu 4K osu!mania charts instead."
            );
        }
    }

    private RhythmChart parseOsu(String sourceName, List<String> lines) {
        Map<String, String> general = new LinkedHashMap<>();
        Map<String, String> metadata = new LinkedHashMap<>();
        Map<String, String> difficulty = new LinkedHashMap<>();
        List<RhythmTimingPoint> timingPoints = new ArrayList<>();
        List<RhythmNote> notes = new ArrayList<>();

        String section = "";
        int noteIndex = 0;
        for (String rawLine : lines) {
            String line = normalizeLine(rawLine);
            if (line.isBlank() || line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line;
                continue;
            }

            switch (section) {
                case "[General]" -> readKeyValue(line, general);
                case "[Metadata]" -> readKeyValue(line, metadata);
                case "[Difficulty]" -> readKeyValue(line, difficulty);
                case "[TimingPoints]" -> timingPoints.add(parseTimingPoint(line));
                case "[HitObjects]" -> {
                    int laneCount = parseLaneCount(difficulty);
                    noteIndex++;
                    notes.add(parseHitObject(line, laneCount, noteIndex));
                }
                default -> {
                }
            }
        }

        int mode = parseInt(general.get("Mode"), -1);
        if (mode != 3) {
            logRhythmWarn("import", "unsupported_mode", Map.of("source", sourceName, "mode", mode));
            throw new IllegalArgumentException("Only osu!mania mode (Mode: 3) is supported.");
        }

        int laneCount = parseLaneCount(difficulty);
        if (laneCount != 4) {
            logRhythmWarn("import", "unsupported_lane_mode", Map.of("source", sourceName, "laneCount", laneCount));
            throw new IllegalArgumentException("Only 4K osu!mania charts are supported.");
        }
        if (timingPoints.isEmpty()) {
            throw new IllegalArgumentException("Chart " + sourceName + " does not contain timing points.");
        }
        if (notes.isEmpty()) {
            throw new IllegalArgumentException("Chart " + sourceName + " does not contain hit objects.");
        }

        RhythmChartMetadata chartMetadata = new RhythmChartMetadata(
            metadata.getOrDefault("Title", "Unknown Title"),
            metadata.get("TitleUnicode"),
            metadata.getOrDefault("Artist", "Unknown Artist"),
            metadata.get("ArtistUnicode"),
            metadata.get("Creator"),
            metadata.getOrDefault("Version", "Unknown Difficulty"),
            general.getOrDefault("AudioFilename", "missing-audio.ogg"),
            sourceName
        );
        String songId = deriveSongId(sourceName, chartMetadata);
        String chartId = deriveChartId(sourceName, songId, chartMetadata);
        RhythmChart chart = new RhythmChart(
            songId,
            chartId,
            chartMetadata,
            laneCount,
            parseDouble(difficulty.get("OverallDifficulty"), 5.0d),
            timingPoints,
            notes
        );

        logRhythmInfo(
            "import",
            "osu_chart_parsed",
            new LinkedHashMap<>() {{
                put("source", sourceName);
                put("chartId", chart.chartId());
                put("songId", chart.songId());
                put("laneCount", chart.keyMode());
                put("timingPoints", chart.timingPoints().size());
                put("hitObjects", chart.notes().size());
                put("holdNotes", chart.holdCount());
                put("audio", chart.metadata().audioFileName());
            }}
        );
        return chart;
    }

    private static RhythmTimingPoint parseTimingPoint(String line) {
        String[] parts = line.split(",");
        if (parts.length < 8) {
            throw new IllegalArgumentException("Invalid timing point: " + line);
        }
        return new RhythmTimingPoint(
            parseLong(parts[0], 0L),
            parseDouble(parts[1], 0.0d),
            parseInt(parts[2], 4),
            parseInt(parts[3], 0),
            parseInt(parts[4], 0),
            parseInt(parts[5], 100),
            parseInt(parts[6], 1) == 1,
            parseInt(parts[7], 0)
        );
    }

    private static RhythmNote parseHitObject(String line, int laneCount, int noteIndex) {
        String[] parts = line.split(",", 6);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid hit object: " + line);
        }

        int x = parseInt(parts[0], 0);
        long time = parseLong(parts[2], 0L);
        int type = parseInt(parts[3], 0);
        boolean hold = (type & 128) != 0;
        boolean circle = (type & 1) != 0;
        boolean slider = (type & 2) != 0;
        boolean spinner = (type & 8) != 0;
        if (slider || spinner || (!hold && !circle)) {
            throw new IllegalArgumentException("Unsupported mania hit object type '" + type + "' in line: " + line);
        }

        int lane = Math.clamp((int) Math.floor((double) x * laneCount / 512.0d), 0, laneCount - 1) + 1;
        long endTime = time;
        if (hold) {
            String[] holdParts = parts[5].split(":", 2);
            endTime = parseLong(holdParts[0], time);
            if (endTime < time) {
                throw new IllegalArgumentException("Hold note end time cannot be before start time: " + line);
            }
        }

        return new RhythmNote("note-" + noteIndex, lane, time, endTime, hold);
    }

    private static void readKeyValue(String line, Map<String, String> output) {
        int separator = line.indexOf(':');
        if (separator <= 0) {
            return;
        }
        String key = line.substring(0, separator).trim();
        String value = line.substring(separator + 1).trim();
        output.put(key, value);
    }

    private static String normalizeLine(String rawLine) {
        if (rawLine == null) {
            return "";
        }
        String line = rawLine.strip();
        if (line.startsWith("\uFEFF")) {
            return line.substring(1).strip();
        }
        return line;
    }

    private static int parseLaneCount(Map<String, String> difficulty) {
        return (int) Math.round(parseDouble(difficulty.get("CircleSize"), -1.0d));
    }

    private static int parseInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(rawValue.trim());
    }

    private static long parseLong(String rawValue, long defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(rawValue.trim());
    }

    private static double parseDouble(String rawValue, double defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(rawValue.trim());
    }

    private static String deriveSongId(String sourceName, RhythmChartMetadata metadata) {
        if (sourceName.toLowerCase(Locale.ROOT).contains("test-4k")) {
            return "debug-song";
        }
        return slug(metadata.artist()) + "-" + slug(metadata.title());
    }

    private static String deriveChartId(String sourceName, String songId, RhythmChartMetadata metadata) {
        if (sourceName.toLowerCase(Locale.ROOT).contains("test-4k")) {
            return "debug/test-4k";
        }
        return songId + "/" + slug(metadata.difficultyName());
    }

    private static String slug(String value) {
        String slug = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");
        if (slug.isBlank()) {
            return "unknown";
        }
        return slug;
    }
}
