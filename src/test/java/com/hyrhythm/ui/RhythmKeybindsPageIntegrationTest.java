package com.hyrhythm.ui;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmKeybindsPageIntegrationTest {
    private static final UUID PLAYER_ID = UUID.fromString("7a7dddc3-c697-4bc0-ab4f-21b543fb7dc0");
    private static final Pattern COMMAND_VALUE_PATTERN = Pattern.compile("\"0\"\\s*:\\s*\"([^\"]*)\"");

    private DependencyLoader loader;
    private RhythmSettingsService settingsService;
    private RhythmKeybindsPageFactory pageFactory;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-keybinds-page-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        settingsService = loader.requireInstance(RhythmSettingsService.class);
        pageFactory = loader.requireInstance(RhythmKeybindsPageFactory.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void keybindsPageBuildShowsDefaultLaneKeysAndCaptureBindings() {
        RhythmKeybindsPage page = createPage();
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals("Lane 1: D", commandValue(uiCommandBuilder.getCommands(), "#Lane1Value.Text"));
        assertEquals("Lane 2: F", commandValue(uiCommandBuilder.getCommands(), "#Lane2Value.Text"));
        assertEquals("Lane 3: J", commandValue(uiCommandBuilder.getCommands(), "#Lane3Value.Text"));
        assertEquals("Lane 4: K", commandValue(uiCommandBuilder.getCommands(), "#Lane4Value.Text"));
        assertEquals("Awaiting input: none", commandValue(uiCommandBuilder.getCommands(), "#AwaitingValue.Text"));
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ChangeLane1Button");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ChangeLane4Button");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.ValueChanged, "#CaptureFieldHost[0] #CaptureInput");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ResetKeybindsButton");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ExitKeybindsButton");
    }

    @Test
    void keybindsPageCaptureFlowUpdatesSavedLaneKey() {
        RhythmKeybindsPage page = createPage();

        page.handleDataEvent(null, null, "{\"Action\":\"Await\",\"Lane\":\"1\"}");
        page.handleDataEvent(null, null, "{\"Action\":\"Capture\",\"@CaptureValue\":\"x\"}");

        assertEquals("X", settingsService.getOrCreateSettings(PLAYER_ID, "UiPlayer").laneKeys().lane1Key());

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals("Lane 1: X", commandValue(uiCommandBuilder.getCommands(), "#Lane1Value.Text"));
        assertEquals("Awaiting input: none", commandValue(uiCommandBuilder.getCommands(), "#AwaitingValue.Text"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#CaptureStatus.Text").contains("Change button"));
    }

    private RhythmKeybindsPage createPage() {
        return pageFactory.create(
            null,
            PLAYER_ID,
            "UiPlayer",
            settingsService.getOrCreateSettings(PLAYER_ID, "UiPlayer")
        );
    }

    private static String commandValue(CustomUICommand[] commands, String selector) {
        CustomUICommand command = findCommand(commands, selector);
        assertNotNull(command, "Missing UI command for selector " + selector);
        if (command.text != null && !command.text.isBlank()) {
            return command.text;
        }
        if (command.data != null) {
            Matcher matcher = COMMAND_VALUE_PATTERN.matcher(command.data);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return command.data;
        }
        return null;
    }

    private static CustomUICommand findCommand(CustomUICommand[] commands, String selector) {
        for (CustomUICommand command : commands) {
            if (command != null && command.type == CustomUICommandType.Set && selector.equals(command.selector)) {
                return command;
            }
        }
        return null;
    }

    private static void assertBinding(CustomUIEventBinding[] eventBindings, CustomUIEventBindingType type, String selector) {
        for (CustomUIEventBinding eventBinding : eventBindings) {
            if (eventBinding == null || eventBinding.type != type) {
                continue;
            }
            if (!selector.equals(eventBinding.selector)) {
                continue;
            }
            assertNotNull(eventBinding.data);
            return;
        }
        throw new AssertionError("Missing event binding " + type + " for selector " + selector);
    }
}
