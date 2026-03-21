package com.hyrhythm.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RhythmCustomUiCommandValidatorTest {
    @Test
    void rejectsRuntimeTexturePathSetMutations() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#Lane1ControlRow[0] #LaneReceptor #IdleReceptor.TexturePath", "RhythmLane1Idle.png");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void allowsInlineSpritesThatDeclareTexturePathInDocumentMarkup() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.appendInline(
            "#Lane1Track",
            "Group { Sprite { TexturePath: \"RhythmGameplayNoteLeft.png\"; } }"
        );

        assertDoesNotThrow(() -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void rejectsInlineGameplayTrackSurfaceHostsEvenWithStableSelectors() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.appendInline(
            "#Lane4TrackSurface",
            "Group #GameplayNote_lane4_note_1 { Visible: false; }"
        );

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void rejectsInlineGameplayTrackSurfacePreloadsWithoutStableNoteId() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.appendInline(
            "#Lane4TrackSurface",
            "Group { Label { Text: \"probe\"; } }"
        );

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void rejectsIndexedTrackSurfaceGameplayNoteSelectors() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#Lane4TrackSurface[0] #GameplayNoteRoot.Visible", true);

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void rejectsReservedFrameworkCloseButtonSelectorsInCommands() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#CloseButton.Text", "Close");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder));
    }

    @Test
    void rejectsReservedFrameworkCloseButtonSelectorsInEventBindings() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void rejectsMouseButtonReleasedBindingsOnLaneButtonSelectors() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.MouseButtonReleased, "#Lane1ControlRow[0] #LaneButton");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void rejectsMouseButtonReleasedBindingsOnAppendedRootIds() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.MouseButtonReleased, "#Lane1ControlRow[0] #LaneInteractionSurface");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void rejectsActivatingBindingsOnLaneInteractionSurfaceSelectors() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Lane1ControlRow[0] #LaneInteractionSurface");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void allowsActivatingBindingsOnLaneButtons() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Lane1ControlRow[0] #LaneButton");

        assertDoesNotThrow(() -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void rejectsKeyboardCaptureValueChangedBindings() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#KeyboardCaptureHost[0] #KeyboardCapture");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }

    @Test
    void rejectsKeyDownBindingsOnGameplayRoot() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.KeyDown, "#GameplayRoot");

        assertThrows(IllegalStateException.class, () -> RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder));
    }
}
