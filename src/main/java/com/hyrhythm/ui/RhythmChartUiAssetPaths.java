package com.hyrhythm.ui;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class RhythmChartUiAssetPaths {
    private static final String GENERATED_CHART_DOCUMENT_DIRECTORY = "Charts";

    private RhythmChartUiAssetPaths() {
    }

    public static String documentPath(String chartId) {
        return GENERATED_CHART_DOCUMENT_DIRECTORY + "/" + sanitizeDocumentToken(chartId) + ".ui";
    }

    public static Path assetFilePath(Path generatedAssetPackDirectory, String chartId) {
        return Objects.requireNonNull(generatedAssetPackDirectory, "generatedAssetPackDirectory")
            .resolve("Common")
            .resolve("UI")
            .resolve("Custom")
            .resolve(GENERATED_CHART_DOCUMENT_DIRECTORY)
            .resolve(sanitizeDocumentToken(chartId) + ".ui");
    }

    private static String sanitizeDocumentToken(String chartId) {
        String sanitizedToken = Objects.requireNonNull(chartId, "chartId")
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace('\\', '_')
            .replace('/', '_')
            .replaceAll("[^a-z0-9._-]+", "_")
            .replaceAll("_{2,}", "_")
            .replaceAll("^_+|_+$", "");
        if (sanitizedToken.isBlank()) {
            return "unknown-chart";
        }
        return sanitizedToken;
    }
}
