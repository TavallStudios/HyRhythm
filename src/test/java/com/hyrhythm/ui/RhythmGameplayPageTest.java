package com.hyrhythm.ui;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.gameplay.model.RhythmGameplaySnapshot;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RhythmGameplayPageTest {
    private static final UUID PLAYER_ID = UUID.fromString("cb54298d-bcbb-4cd7-94ed-f62374f80553");
    private static final Pattern COMMAND_VALUE_PATTERN = Pattern.compile("\"0\"\\s*:\\s*\"([^\"]*)\"");

    private DependencyLoader loader;
    private RhythmSessionService sessionService;
    private RhythmGameplayService gameplayService;
    private RhythmSongLibraryService songLibraryService;
    private RhythmSettingsService settingsService;
    private RhythmGameplayPageFactory pageFactory;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-gameplay-page-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        sessionService = loader.requireInstance(RhythmSessionService.class);
        gameplayService = loader.requireInstance(RhythmGameplayService.class);
        songLibraryService = loader.requireInstance(RhythmSongLibraryService.class);
        settingsService = loader.requireInstance(RhythmSettingsService.class);
        pageFactory = loader.requireInstance(RhythmGameplayPageFactory.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void gameplayPageIgnoresGameplayInputEvents() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);

        page.handleDataEvent(null, null, "{\"Lane\":\"1\",\"Action\":\"Tap\",\"SongTimeMs\":\"1000\"}");
        page.handleDataEvent(null, null, "{\"Key\":\"Ability1\",\"Action\":\"Tap\",\"SongTimeMs\":\"1000\"}");

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(0, snapshot.combo());
        assertEquals(0, snapshot.hitCount());
        assertTrue(snapshot.heldLanes().isEmpty());
    }

    @Test
    void gameplayPageBuildDoesNotEmitForbiddenTexturePathMutations() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);

        assertDoesNotThrow(() -> page.build(null, new UICommandBuilder(), new UIEventBuilder(), null));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageBuildUsesProjectOwnedCloseSelectorOnly() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertTrue(containsSelector(uiEventBuilder.getEvents(), "#ExitGameplayButton"));
        assertTrue(lacksSelector(uiCommandBuilder.getCommands(), "#CloseButton"));
        assertTrue(lacksSelector(uiEventBuilder.getEvents(), "#CloseButton"));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageBuildUsesLaneButtonBindings() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#Lane1ControlRow[0] #LaneButton");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#Lane4ControlRow[0] #LaneButton");
        assertFalse(containsBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.KeyDown, "#GameplayRoot"));
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#StopButton");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ExitGameplayButton");
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageBuildRendersGameplayMetadataAndInputLabels() {
        settingsService.updateLaneKey(PLAYER_ID, "UiPlayer", 1, "L");
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmChart chart = chart();
        RhythmGameplayPage page = createPage(startedSession, chart);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals(chart.metadata().title(), commandValue(uiCommandBuilder.getCommands(), "#SongTitle.Text"));
        assertEquals(chart.metadata().artist() + " | " + chart.metadata().difficultyName() + " | " + chart.keyMode() + "K", commandValue(uiCommandBuilder.getCommands(), "#SongMeta.Text"));
        assertEquals("Keys: 1=L 2=F 3=J 4=K", commandValue(uiCommandBuilder.getCommands(), "#BindSummary.Text"));
        assertEquals("0ms", commandValue(uiCommandBuilder.getCommands(), "#ClockValue.Text"));
        assertEquals("0", commandValue(uiCommandBuilder.getCommands(), "#ScoreValue.Text"));
        assertEquals("0", commandValue(uiCommandBuilder.getCommands(), "#ComboValue.Text"));
        assertEquals("100.00%", commandValue(uiCommandBuilder.getCommands(), "#AccuracyValue.Text"));
        assertEquals("Stop Match", commandValue(uiCommandBuilder.getCommands(), "#StopButton.Text"));
        assertEquals("Lane 1  [L]", commandValue(uiCommandBuilder.getCommands(), "#Lane1ControlRow[0] #LaneButton.Text"));
        assertEquals("Lane 4  [K]", commandValue(uiCommandBuilder.getCommands(), "#Lane4ControlRow[0] #LaneButton.Text"));
        assertEquals("READY", commandValue(uiCommandBuilder.getCommands(), "#Lane1Status.Text"));
        assertEquals("READY", commandValue(uiCommandBuilder.getCommands(), "#Lane4Status.Text"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#Lane1Feed.Text").startsWith("Next tap "));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#Lane4Feed.Text").startsWith("Next tap "));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageRoutesSavedLaneKeyCaptureIntoGameplay() {
        settingsService.updateLaneKey(PLAYER_ID, "UiPlayer", 1, "L");
        RhythmSessionSnapshot startedSession = startDebugSession();
        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        page.handleDataEvent(null, null, capturePayload("l"));

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(1, snapshot.combo());
        assertEquals(1, snapshot.hitCount());
        assertEquals(320, snapshot.score());

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#Lane1Feed.Text").contains("Input L -> PERFECT head_hit"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#DebugText.Text").contains("Last input: L -> PERFECT head_hit"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#DebugText.Text").contains("Capture debug: down lane=1"));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageProcessesSequentialCapturedKeysWithoutLaneClicks() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        page.handleDataEvent(null, null, capturePayload("D"));

        assertEquals(1, gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow().hitCount());

        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1500L);
        page.handleDataEvent(null, null, capturePayload("F"));

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(2, snapshot.combo());
        assertEquals(2, snapshot.hitCount());
        assertEquals(640, snapshot.score());
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageRoutesLaneButtonActivationIntoGameplay() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        page.handleDataEvent(null, null, "{\"Action\":\"LaneTap\",\"Lane\":\"1\"}");

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(1, snapshot.combo());
        assertEquals(1, snapshot.hitCount());
        assertEquals(320, snapshot.score());
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageProcessesRepeatedSingleCharacterCaptureValues() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        page.handleDataEvent(null, null, capturePayload("D"));

        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 3500L);
        page.handleDataEvent(null, null, capturePayload("D"));

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(2, snapshot.hitCount());
        assertEquals(1, snapshot.combo());
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageConsumesOnlyNewestCharacterFromBackloggedCaptureValue() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        page.handleDataEvent(null, null, capturePayload("XD"));

        var snapshot = gameplayService.getActiveGameplay(PLAYER_ID).orElseThrow();

        assertEquals(1, snapshot.hitCount());
        assertEquals(1, snapshot.combo());
        assertEquals(320, snapshot.score());
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageSendsChatFeedbackForCapturedKey() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);

        PlayerRef playerRef = mock(PlayerRef.class);

        RhythmGameplayPage page = pageFactory.create(
            playerRef,
            PLAYER_ID,
            "UiPlayer",
            startedSession,
            chart(),
            settingsService.getOrCreateSettings(PLAYER_ID, "UiPlayer"),
            null
        );
        buildPage(page);

        page.handleDataEvent(null, null, capturePayload("D"));

        verify(playerRef).sendMessage(argThat((Message message) -> message.getRawText().contains("Rhythm input debug:")));
    }

    @Test
    void gameplayPageBuildReflectsHeldLaneFromGameplayState() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        gameplayService.submitLaneInput(PLAYER_ID, "UiPlayer", RhythmLaneInputAction.DOWN, 3, 2000L);
        RhythmGameplayPage page = createPage(startedSession);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals("320", commandValue(uiCommandBuilder.getCommands(), "#ScoreValue.Text"));
        assertEquals("1", commandValue(uiCommandBuilder.getCommands(), "#ComboValue.Text"));
        assertEquals("HELD", commandValue(uiCommandBuilder.getCommands(), "#Lane3Status.Text"));
        assertNotNull(findCommand(uiCommandBuilder.getCommands(), "#Lane3ControlRow[0] #LaneIdleReceptor.Visible"));
        assertNotNull(findCommand(uiCommandBuilder.getCommands(), "#Lane3ControlRow[0] #LaneHitReceptor.Visible"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#DebugText.Text").contains("held=[3]"));
        page.onDismiss(null, null);
    }

    @Test
    void deferredGameplayRuntimePreloadAppendsGeneratedChartAfterPageOpen() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmChart chart = chart();
        RhythmGameplayPage page = createPage(startedSession, chart);
        UICommandBuilder preloadCommandBuilder = new UICommandBuilder();

        page.preloadGameplayRuntimeUi(preloadCommandBuilder);

        assertEquals(0, countCommands(preloadCommandBuilder.getCommands(), CustomUICommandType.AppendInline, "TrackSurface"));
        assertEquals(0, countCommands(preloadCommandBuilder.getCommands(), CustomUICommandType.Append, "TrackSurface"));
        assertEquals(
            1,
            countAppendDocument(
                preloadCommandBuilder.getCommands(),
                "#GameplayChartHost",
                RhythmChartUiAssetPaths.documentPath(chart.chartId())
            )
        );
        assertEquals(0, countCommands(preloadCommandBuilder.getCommands(), CustomUICommandType.Clear, "#Lane"));
        assertTrue(containsSelector(preloadCommandBuilder.getCommands(), "#GameplayChartHost"));
        assertTrue(containsCommandText(preloadCommandBuilder.getCommands(), RhythmChartUiAssetPaths.documentPath(chart.chartId())));
        assertFalse(containsCommandText(preloadCommandBuilder.getCommands(), "Pages/RhythmGameplayNoteHost.ui"));
        assertFalse(containsCommandText(preloadCommandBuilder.getCommands(), "Pages/RhythmGameplayTapNote"));
        assertFalse(containsCommandText(preloadCommandBuilder.getCommands(), "Pages/RhythmGameplayHoldNote"));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageBuildDoesNotAppendGeneratedChartBeforePageOpen() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals(0, countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.AppendInline, "TrackSurface"));
        assertEquals(0, countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.Append, "TrackSurface"));
        assertEquals(0, countAppendDocument(uiCommandBuilder.getCommands(), "#GameplayChartHost", "Charts/debug_test-4k.ui"));
        String debugText = commandValue(uiCommandBuilder.getCommands(), "#DebugText.Text");
        assertTrue(debugText.contains("preload[chart=debug/test-4k"));
        assertTrue(debugText.contains("doc=Charts/debug_test-4k.ui"));
        assertTrue(debugText.contains("mode=deferred_page_open"));
        assertTrue(debugText.contains("complete=false"));
        assertTrue(debugText.contains("blocked=true"));
        page.onDismiss(null, null);
    }

    @Test
    void gameplayPageSnapshotUpdatesDoNotLiveAppendNotesDuringPlay() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);
        buildPage(page);

        RhythmGameplaySnapshot advancedSnapshot = gameplayService.advanceGameplay(PLAYER_ID, "UiPlayer", 1000L);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();

        page.applySnapshot(uiCommandBuilder, advancedSnapshot);

        assertEquals(0, countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.Append, ""));
        assertEquals(0, countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.AppendInline, ""));
        assertEquals(0, countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.Clear, ""));
        assertTrue(countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.Set, ".Visible") > 0);
        assertTrue(countCommands(uiCommandBuilder.getCommands(), CustomUICommandType.Set, ".Anchor") > 0);
        assertEquals(0, countSelectors(uiCommandBuilder.getCommands(), "TrackSurface["));
        assertTrue(countSelectors(uiCommandBuilder.getCommands(), "#GameplayNote") > 0);
        page.onDismiss(null, null);
    }

    @Test
    void stopRequestEndsSessionAndRendersCompletedGameplayState() {
        RhythmSessionSnapshot startedSession = startDebugSession();
        RhythmGameplayPage page = createPage(startedSession);

        page.handleDataEvent(null, null, "{\"Request\":\"Stop\"}");

        assertEquals(RhythmSessionPhase.ENDED, sessionService.getSession(PLAYER_ID).orElseThrow().phase());

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertEquals("Stop Closed", commandValue(uiCommandBuilder.getCommands(), "#StopButton.Text"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#StatusText.Text").contains("Finished: ui_stop"));
        assertTrue(commandValue(uiCommandBuilder.getCommands(), "#DebugText.Text").contains("finish=ui_stop"));
        page.onDismiss(null, null);
    }

    @Test
    void soundEventLookupCandidatesIncludeLegacyAndAssetKeyForms() {
        assertEquals(
            java.util.List.of(
                "hyrhythm/imported/zeratch-tp-na-ame",
                "zeratch-tp-na-ame",
                "Zeratch-tp-na-ame"
            ),
            RhythmGameplayPage.soundEventLookupCandidates("hyrhythm/imported/zeratch-tp-na-ame")
        );
        assertEquals(
            java.util.List.of("Port-avenue-odinoko-ily-frenchcore-remix"),
            RhythmGameplayPage.soundEventLookupCandidates("Port-avenue-odinoko-ily-frenchcore-remix")
        );
    }

    private static boolean containsSelector(CustomUICommand[] commands, String selectorFragment) {
        for (CustomUICommand command : commands) {
            if (command != null && command.selector != null && command.selector.contains(selectorFragment)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSelector(CustomUIEventBinding[] eventBindings, String selectorFragment) {
        for (CustomUIEventBinding eventBinding : eventBindings) {
            if (eventBinding != null && eventBinding.selector != null && eventBinding.selector.contains(selectorFragment)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lacksSelector(CustomUICommand[] commands, String selectorFragment) {
        return !containsSelector(commands, selectorFragment);
    }

    private static boolean lacksSelector(CustomUIEventBinding[] eventBindings, String selectorFragment) {
        return !containsSelector(eventBindings, selectorFragment);
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

    private static String commandData(CustomUICommand[] commands, String selector) {
        CustomUICommand command = findCommand(commands, selector);
        assertNotNull(command, "Missing UI command for selector " + selector);
        return command.data;
    }

    private static CustomUICommand findCommand(CustomUICommand[] commands, String selector) {
        CustomUICommand matchingCommand = null;
        for (CustomUICommand command : commands) {
            if (command == null) {
                continue;
            }
            if (command.type == CustomUICommandType.Set && selector.equals(command.selector)) {
                matchingCommand = command;
            }
        }
        return matchingCommand;
    }

    private static long countCommands(CustomUICommand[] commands, CustomUICommandType type, String selectorFragment) {
        long count = 0L;
        for (CustomUICommand command : commands) {
            if (command == null || command.type != type) {
                continue;
            }
            String selector = command.selector == null ? "" : command.selector;
            if (!selector.contains(selectorFragment)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static long countSelectors(CustomUICommand[] commands, String selectorFragment) {
        long count = 0L;
        for (CustomUICommand command : commands) {
            if (command == null || command.selector == null) {
                continue;
            }
            if (command.selector.contains(selectorFragment)) {
                count++;
            }
        }
        return count;
    }

    private static long countAppendDocument(CustomUICommand[] commands, String selectorFragment, String documentFragment) {
        long count = 0L;
        for (CustomUICommand command : commands) {
            if (command == null || command.type != CustomUICommandType.Append) {
                continue;
            }
            String selector = command.selector == null ? "" : command.selector;
            String document = command.text == null ? "" : command.text;
            if (!selector.contains(selectorFragment) || !document.contains(documentFragment)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private static int firstCommandIndex(
        CustomUICommand[] commands,
        CustomUICommandType type,
        String selectorFragment,
        String payloadFragment
    ) {
        for (int index = 0; index < commands.length; index++) {
            CustomUICommand command = commands[index];
            if (command == null || command.type != type) {
                continue;
            }
            String selector = command.selector == null ? "" : command.selector;
            if (!selector.contains(selectorFragment)) {
                continue;
            }
            String payload = command.text != null && !command.text.isBlank()
                ? command.text
                : command.data == null ? "" : command.data;
            if (!payload.contains(payloadFragment)) {
                continue;
            }
            return index;
        }
        return -1;
    }

    private static boolean containsCommandText(CustomUICommand[] commands, String textFragment) {
        for (CustomUICommand command : commands) {
            if (command == null) {
                continue;
            }
            if (command.text != null && command.text.contains(textFragment)) {
                return true;
            }
        }
        return false;
    }

    private static void assertBinding(CustomUIEventBinding[] eventBindings, CustomUIEventBindingType type, String selector) {
        if (containsBinding(eventBindings, type, selector)) {
            return;
        }
        throw new AssertionError("Missing event binding " + type + " for selector " + selector);
    }

    private static boolean containsBinding(CustomUIEventBinding[] eventBindings, CustomUIEventBindingType type, String selector) {
        for (CustomUIEventBinding eventBinding : eventBindings) {
            if (eventBinding == null || eventBinding.type != type) {
                continue;
            }
            if (!selector.equals(eventBinding.selector)) {
                continue;
            }
            assertNotNull(eventBinding.data);
            return true;
        }
        return false;
    }

    private RhythmSessionSnapshot startDebugSession() {
        sessionService.joinOrCreateSession(PLAYER_ID, "UiPlayer");
        sessionService.selectChart(PLAYER_ID, "UiPlayer", "debug/test-4k");
        return sessionService.startSession(PLAYER_ID, "UiPlayer");
    }

    private RhythmChart chart() {
        return songLibraryService.findChartById("debug/test-4k").orElseThrow();
    }

    private RhythmGameplayPage createPage(RhythmSessionSnapshot startedSession) {
        return createPage(startedSession, chart());
    }

    private RhythmGameplayPage createPage(RhythmSessionSnapshot startedSession, RhythmChart chart) {
        return pageFactory.create(
            null,
            PLAYER_ID,
            "UiPlayer",
            startedSession,
            chart,
            settingsService.getOrCreateSettings(PLAYER_ID, "UiPlayer"),
            null
        );
    }

    private static void buildPage(RhythmGameplayPage page) {
        page.build(null, new UICommandBuilder(), new UIEventBuilder(), null);
        page.preloadGameplayRuntimeUi(new UICommandBuilder());
    }

    private static String capturePayload(String captureValue) {
        return "{\"Action\":\"CaptureKey\",\"@CaptureValue\":\"" + captureValue + "\"}";
    }
}
