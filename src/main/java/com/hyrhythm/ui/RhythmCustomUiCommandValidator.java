package com.hyrhythm.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.Locale;
import java.util.Objects;

final class RhythmCustomUiCommandValidator {
    private static final String TEXTURE_PATH_SUFFIX = ".TEXTUREPATH";
    private static final String RESERVED_CLOSE_BUTTON_SELECTOR = "#CLOSEBUTTON";
    private static final String GAMEPLAY_NOTE_SELECTOR_PREFIX = "#GAMEPLAYNOTE_";
    private static final String GAMEPLAY_NOTE_ROOT_SELECTOR = "#GAMEPLAYNOTEROOT";
    private static final String LANE_BUTTON_SELECTOR = "#LANEBUTTON";
    private static final String LANE_INTERACTION_SURFACE_SELECTOR = "#LANEINTERACTIONSURFACE";
    private static final String LANE_CONTROL_ROW_SELECTOR = "CONTROLROW";
    private static final String LANE_TRACK_SURFACE_SELECTOR = "TRACKSURFACE";
    private static final String KEYBOARD_CAPTURE_SELECTOR = "#KEYBOARDCAPTURE";
    private static final String GAMEPLAY_ROOT_SELECTOR = "#GAMEPLAYROOT";

    private RhythmCustomUiCommandValidator() {
    }

    static void validate(UICommandBuilder uiCommandBuilder) {
        Objects.requireNonNull(uiCommandBuilder, "uiCommandBuilder");
        validate(uiCommandBuilder.getCommands());
    }

    static void validate(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        Objects.requireNonNull(uiCommandBuilder, "uiCommandBuilder");
        Objects.requireNonNull(uiEventBuilder, "uiEventBuilder");
        validate(uiCommandBuilder.getCommands());
        validate(uiEventBuilder.getEvents());
    }

    static void validate(CustomUICommand[] commands) {
        Objects.requireNonNull(commands, "commands");
        for (CustomUICommand command : commands) {
            if (command == null) {
                continue;
            }
            validateSelector(command.selector);
            validateCommandCompatibility(command.type, command.selector, command.text);
            if (command.type != CustomUICommandType.Set || command.selector == null) {
                continue;
            }
            if (command.selector.toUpperCase(Locale.ROOT).endsWith(TEXTURE_PATH_SUFFIX)) {
                throw new IllegalStateException(
                    "CustomUI does not allow runtime Set mutations for selector '" + command.selector + "'."
                );
            }
        }
    }

    static void validate(CustomUIEventBinding[] eventBindings) {
        Objects.requireNonNull(eventBindings, "eventBindings");
        for (CustomUIEventBinding eventBinding : eventBindings) {
            if (eventBinding == null) {
                continue;
            }
            validateSelector(eventBinding.selector);
            validateSelectorCompatibility(eventBinding.type, eventBinding.selector);
        }
    }

    private static void validateSelector(String selector) {
        if (selector == null) {
            return;
        }
        String normalizedSelector = selector.toUpperCase(Locale.ROOT);
        if (normalizedSelector.contains(RESERVED_CLOSE_BUTTON_SELECTOR)) {
            throw new IllegalStateException(
                "CustomUI selector '" + selector + "' uses reserved framework selector '#CloseButton'."
            );
        }
    }

    private static void validateCommandCompatibility(CustomUICommandType commandType, String selector, String commandText) {
        if (commandType == null || selector == null) {
            return;
        }

        String normalizedSelector = selector.toUpperCase(Locale.ROOT);
        if (commandType == CustomUICommandType.AppendInline && normalizedSelector.contains(LANE_TRACK_SURFACE_SELECTOR)) {
            validateTrackSurfaceInlineHost(selector, commandText);
        }
        if (commandType == CustomUICommandType.Set
            && normalizedSelector.contains("TRACKSURFACE[")
            && (normalizedSelector.contains(GAMEPLAY_NOTE_ROOT_SELECTOR) || normalizedSelector.contains(GAMEPLAY_NOTE_SELECTOR_PREFIX))) {
            throw new IllegalStateException(
                "HyRhythm gameplay note updates must target stable note selectors, not indexed track-surface chains, for '"
                    + selector
                    + "'."
            );
        }
    }

    private static void validateTrackSurfaceInlineHost(String selector, String commandText) {
        String normalizedDocument = commandText == null ? "" : commandText.toUpperCase(Locale.ROOT);
        if (normalizedDocument.contains(GAMEPLAY_NOTE_SELECTOR_PREFIX)) {
            return;
        }
        throw new IllegalStateException(
            "HyRhythm gameplay track-surface appendInline hosts must declare an explicit stable gameplay note id for '"
                + selector
                + "'."
        );
    }

    private static void validateSelectorCompatibility(CustomUIEventBindingType eventType, String selector) {
        if (eventType == null || selector == null) {
            return;
        }

        String normalizedSelector = selector.toUpperCase(Locale.ROOT);
        if (eventType == CustomUIEventBindingType.MouseButtonReleased
            && (normalizedSelector.contains(LANE_BUTTON_SELECTOR)
                || normalizedSelector.contains(LANE_INTERACTION_SURFACE_SELECTOR)
                || (normalizedSelector.contains("#LANE") && normalizedSelector.contains(LANE_CONTROL_ROW_SELECTOR)))) {
            throw new IllegalStateException(
                "HyRhythm gameplay lanes are HUD-only. Do not bind MouseButtonReleased on '"
                    + selector
                    + "'."
            );
        }
        if (eventType == CustomUIEventBindingType.MouseButtonReleased && normalizedSelector.contains(LANE_INTERACTION_SURFACE_SELECTOR)) {
            throw new IllegalStateException(
                "CustomUI MouseButtonReleased binding must not target the legacy lane interaction surface '"
                    + selector
                    + "'."
            );
        }
        if (eventType == CustomUIEventBindingType.Activating && normalizedSelector.contains(LANE_INTERACTION_SURFACE_SELECTOR)) {
            throw new IllegalStateException(
                "CustomUI Activating binding should target the button control, not interaction surface '" + selector + "'."
            );
        }
        if (eventType == CustomUIEventBindingType.ValueChanged && normalizedSelector.contains(KEYBOARD_CAPTURE_SELECTOR)) {
            throw new IllegalStateException(
                "HyRhythm gameplay input must not depend on hidden CustomUI keyboard capture selector '" + selector + "'."
            );
        }
        if (eventType == CustomUIEventBindingType.KeyDown && normalizedSelector.contains(GAMEPLAY_ROOT_SELECTOR)) {
            throw new IllegalStateException(
                "HyRhythm gameplay page must not bind KeyDown on layout root selector '" + selector + "'."
            );
        }
    }

}
