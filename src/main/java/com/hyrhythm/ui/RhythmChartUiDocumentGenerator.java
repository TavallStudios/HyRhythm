package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmNote;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RhythmChartUiDocumentGenerator {
    private static final int TRACK_HEIGHT_PX = 388;
    private static final int LANE_TRACK_WIDTH_PX = 250;
    private static final int LANE_TRACK_STEP_PX = 276;
    private static final int NOTE_ICON_SIZE_PX = 28;
    private static final int NOTE_ICON_LEFT_PX = 6;
    private static final int NOTE_ICON_TOP_PX = 2;
    private static final int NOTE_LABEL_LEFT_PADDING_PX = 40;
    private static final int NOTE_LABEL_RIGHT_PADDING_PX = 8;
    private static final int NOTE_BASE_HEIGHT_PX = 32;

    public String generateDocument(RhythmChart chart) {
        Objects.requireNonNull(chart, "chart");

        Map<Integer, List<RhythmNote>> notesByLane = groupNotesByLane(chart);
        StringBuilder documentBuilder = new StringBuilder(8192);
        documentBuilder.append("$C = \"../Common.ui\";\n\n");
        documentBuilder.append("Group #GameplayChartRoot {\n");
        documentBuilder.append("  Anchor: (Full: 0);\n\n");
        for (int lane = 1; lane <= chart.keyMode(); lane++) {
            appendLaneGroup(documentBuilder, lane, notesByLane.getOrDefault(lane, List.of()));
            if (lane < chart.keyMode()) {
                documentBuilder.append('\n');
            }
        }
        documentBuilder.append("}\n");
        return documentBuilder.toString();
    }

    private static Map<Integer, List<RhythmNote>> groupNotesByLane(RhythmChart chart) {
        LinkedHashMap<Integer, List<RhythmNote>> notesByLane = new LinkedHashMap<>();
        for (int lane = 1; lane <= chart.keyMode(); lane++) {
            notesByLane.put(lane, new ArrayList<>());
        }
        for (RhythmNote note : chart.notes()) {
            notesByLane.computeIfAbsent(note.lane(), ignored -> new ArrayList<>()).add(note);
        }
        return notesByLane;
    }

    private static void appendLaneGroup(StringBuilder documentBuilder, int lane, List<RhythmNote> notes) {
        RhythmGameplayLaneDirection laneDirection = RhythmGameplayLaneDirection.fromLane(lane);
        documentBuilder.append("  Group #GameplayChartLane").append(lane).append(" {\n");
        documentBuilder.append("    Anchor: (Left: ").append(laneLeft(lane)).append(", Top: 0, Width: ")
            .append(LANE_TRACK_WIDTH_PX).append(", Height: ").append(TRACK_HEIGHT_PX).append(");\n");
        documentBuilder.append('\n');
        for (RhythmNote note : notes) {
            appendNote(documentBuilder, laneDirection, note);
            documentBuilder.append('\n');
        }
        documentBuilder.append("  }\n");
    }

    private static void appendNote(StringBuilder documentBuilder, RhythmGameplayLaneDirection laneDirection, RhythmNote note) {
        String noteRootId = RhythmGameplayUiSelectors.gameplayNoteRootId(laneDirection, note.noteId());
        documentBuilder.append("    Group #").append(noteRootId).append(" {\n");
        documentBuilder.append("      Visible: false;\n");
        documentBuilder.append("      Anchor: (Left: 0, Right: 0, Top: 0, Height: ").append(noteHeight(note)).append(");\n");
        documentBuilder.append("      Background: (Color: ").append(note.hold() ? "#f2c14a(0.35)" : "#ffffff(0.18)").append(");\n\n");
        documentBuilder.append("      Sprite {\n");
        documentBuilder.append("        Anchor: (Width: ").append(NOTE_ICON_SIZE_PX).append(", Height: ").append(NOTE_ICON_SIZE_PX)
            .append(", Left: ").append(NOTE_ICON_LEFT_PX).append(", Top: ").append(NOTE_ICON_TOP_PX).append(");\n");
        documentBuilder.append("        TexturePath: \"").append(laneDirection.generatedChartTexturePath()).append("\";\n");
        documentBuilder.append("      }\n\n");
        documentBuilder.append("      Label {\n");
        documentBuilder.append("        Text: \"").append(note.hold() ? "Hold" : "Tap").append("\";\n");
        documentBuilder.append("        Style: (FontSize: 11, TextColor: #d9e4ef, VerticalAlignment: Center, HorizontalAlignment: End);\n");
        documentBuilder.append("        Padding: (Right: ").append(NOTE_LABEL_RIGHT_PADDING_PX).append(", Left: ")
            .append(NOTE_LABEL_LEFT_PADDING_PX).append(");\n");
        documentBuilder.append("      }\n");
        documentBuilder.append("    }\n");
    }

    private static int noteHeight(RhythmNote note) {
        if (!note.hold()) {
            return NOTE_BASE_HEIGHT_PX;
        }
        long durationMillis = Math.max(120L, note.durationMillis());
        return NOTE_BASE_HEIGHT_PX + (int) Math.min(168L, durationMillis / 8L);
    }

    private static int laneLeft(int lane) {
        return (lane - 1) * LANE_TRACK_STEP_PX;
    }
}
