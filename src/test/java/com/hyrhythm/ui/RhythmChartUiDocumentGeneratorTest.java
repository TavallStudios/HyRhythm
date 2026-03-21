package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmChartMetadata;
import com.hyrhythm.content.model.RhythmNote;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmChartUiDocumentGeneratorTest {
    private final RhythmChartUiDocumentGenerator generator = new RhythmChartUiDocumentGenerator();

    @Test
    void generatedDocumentUsesDeterministicNoteSelectorsAndSemanticTextures() {
        RhythmChart chart = new RhythmChart(
            "test-song",
            "test-chart",
            new RhythmChartMetadata("Test Song", "Test Song", "Artist", "Artist", "Mapper", "Hard", "test.ogg", "test.osu"),
            4,
            7.0d,
            List.of(),
            List.of(
                new RhythmNote("lane-1.note", 1, 1000L, 1000L, false),
                new RhythmNote("lane-4-hold", 4, 2000L, 2800L, true)
            )
        );

        String document = generator.generateDocument(chart);

        assertTrue(document.contains("$C = \"../Common.ui\";"));
        assertTrue(document.contains("Group #GameplayChartLane1"));
        assertTrue(document.contains("Group #GameplayChartLane4"));
        assertTrue(document.contains("#" + RhythmGameplayUiSelectors.gameplayNoteRootId(RhythmGameplayLaneDirection.LEFT, "lane-1.note")));
        assertTrue(document.contains("#" + RhythmGameplayUiSelectors.gameplayNoteRootId(RhythmGameplayLaneDirection.RIGHT, "lane-4-hold")));
        assertTrue(document.contains("#GameplayNoteLeftLane1Note"));
        assertFalse(document.contains("#GameplayNote_left_note_1"));
        assertTrue(document.contains("TexturePath: \"../Pages/RhythmGameplayNoteLeft.png\";"));
        assertTrue(document.contains("TexturePath: \"../Pages/RhythmGameplayNoteRight.png\";"));
        assertTrue(document.contains("Visible: false;"));
        assertTrue(document.contains("Text: \"Tap\";"));
        assertTrue(document.contains("Text: \"Hold\";"));
    }
}
