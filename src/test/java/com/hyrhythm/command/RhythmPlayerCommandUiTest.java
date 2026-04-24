package com.hyrhythm.command;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.ui.RhythmGameplayPage;
import com.hyrhythm.ui.RhythmKeybindsPage;
import com.hyrhythm.ui.RhythmSongSelectionPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RhythmPlayerCommandUiTest {
    private static final UUID PLAYER_ID = UUID.fromString("0a48f64b-6657-49b5-90ae-34e6d96c6c62");

    private DependencyLoader loader;
    private RhythmCommandRouter router;
    private RhythmUiCommand uiCommand;
    private RhythmKeybindsCommand keybindsCommand;
    private RhythmStartCommand startCommand;
    private RhythmStateCommand stateCommand;
    private RhythmSessionService sessionService;
    private RhythmGameplayService gameplayService;
    private RhythmSongLibraryService songLibraryService;

    private Player player;
    private final List<String> messages = new ArrayList<>();
    private final List<CustomUIPage> openedPages = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-player-command-ui-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        router = loader.requireInstance(RhythmCommandRouter.class);
        uiCommand = loader.requireInstance(RhythmUiCommand.class);
        keybindsCommand = loader.requireInstance(RhythmKeybindsCommand.class);
        startCommand = loader.requireInstance(RhythmStartCommand.class);
        stateCommand = loader.requireInstance(RhythmStateCommand.class);
        sessionService = loader.requireInstance(RhythmSessionService.class);
        gameplayService = loader.requireInstance(RhythmGameplayService.class);
        songLibraryService = loader.requireInstance(RhythmSongLibraryService.class);
        player = createInteractivePlayer();
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        messages.clear();
        openedPages.clear();
        loader.resetInstances();
    }

    @Test
    void playerCommandFlowOpensUiStartsGameplayAndEndsThroughRealPages() {
        uiCommand.executeAsync(new CommandContext(uiCommand, player, "ui")).join();

        assertEquals(RhythmSessionPhase.LOBBY, sessionService.getSession(PLAYER_ID).orElseThrow().phase());
        assertEquals(1, openedPages.size());
        assertInstanceOf(RhythmSongSelectionPage.class, openedPages.getFirst());

        var song = songLibraryService.listSongs().getFirst();
        var chart = song.charts().getFirst();
        RhythmSongSelectionPage selectionPage = (RhythmSongSelectionPage) openedPages.getFirst();
        selectionPage.build(null, new UICommandBuilder(), new UIEventBuilder(), null);
        selectionPage.handleDataEvent(null, null, selectionEvent("songId", song.songId()));
        selectionPage.handleDataEvent(null, null, selectionEvent("chartId", chart.chartId()));
        selectionPage.handleDataEvent(null, null, selectionEvent("action", "Confirm"));

        assertEquals(RhythmSessionPhase.READY, sessionService.getSession(PLAYER_ID).orElseThrow().phase());

        startCommand.executeAsync(new CommandContext(startCommand, player, "start")).join();

        assertTrue(messages.stream().anyMatch(message -> message.contains("Opening gameplay UI.")));
        assertEquals(RhythmSessionPhase.PLAYING, sessionService.getSession(PLAYER_ID).orElseThrow().phase());
        assertEquals(2, openedPages.size());
        assertInstanceOf(RhythmGameplayPage.class, openedPages.get(1));

        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 1000L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 1500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 3, 2000L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.UP, 3, 2600L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 3000L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 3500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 4500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 5500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 3, 6500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 7500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 8500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 9000L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 3, 9500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 10500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 11500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 12500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 3, 13500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 14500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 15500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 16500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 3, 17500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 2, 18500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 4, 19500L);
        gameplayService.submitLaneInput(PLAYER_ID, "UiCommandPlayer", RhythmLaneInputAction.DOWN, 1, 20000L);

        assertEquals(RhythmSessionPhase.ENDED, sessionService.getSession(PLAYER_ID).orElseThrow().phase());

        stateCommand.executeAsync(new CommandContext(stateCommand, player, "state")).join();

        assertTrue(messages.stream().anyMatch(message -> message.contains("phase=ENDED chart=debug/test-4k")));
        assertTrue(messages.stream().anyMatch(message -> message.contains("finish=chart_complete")));
    }

    @Test
    void playerKeybindsCommandOpensRebindPage() {
        keybindsCommand.executeAsync(new CommandContext(keybindsCommand, player, "keybinds")).join();

        assertEquals(1, openedPages.size());
        assertInstanceOf(RhythmKeybindsPage.class, openedPages.getFirst());
        assertTrue(messages.stream().anyMatch(message -> message.contains("Opening keybind rebind menu")));
    }

    @SuppressWarnings("unchecked")
    private Player createInteractivePlayer() {
        Player mockedPlayer = mock(Player.class);
        World world = mock(World.class);
        PageManager pageManager = mock(PageManager.class);
        EntityStore entityStore = mock(EntityStore.class);
        Store<EntityStore> store = mock(Store.class);
        Ref<EntityStore> playerReference = mock(Ref.class);

        when(mockedPlayer.getUuid()).thenReturn(PLAYER_ID);
        when(mockedPlayer.getDisplayName()).thenReturn("UiCommandPlayer");
        when(mockedPlayer.getWorld()).thenReturn(world);
        when(mockedPlayer.getPageManager()).thenReturn(pageManager);
        when(mockedPlayer.getReference()).thenReturn(playerReference);
        when(mockedPlayer.getPlayerRef()).thenReturn(null);
        when(mockedPlayer.hasPermission(any(String.class))).thenReturn(true);
        when(mockedPlayer.hasPermission(any(String.class), anyBoolean())).thenReturn(true);

        when(world.isAlive()).thenReturn(true);
        when(world.getEntityStore()).thenReturn(entityStore);
        when(entityStore.getStore()).thenReturn(store);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(world).execute(any(Runnable.class));

        doAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            messages.add(message.getRawText());
            return null;
        }).when(mockedPlayer).sendMessage(any(Message.class));

        doAnswer(invocation -> {
            openedPages.add(invocation.getArgument(2));
            return null;
        }).when(pageManager).openCustomPage(any(), any(), any(CustomUIPage.class));

        return mockedPlayer;
    }

    private static RhythmSongSelectionPage.SelectionEventData selectionEvent(String fieldName, String value) {
        try {
            RhythmSongSelectionPage.SelectionEventData eventData = new RhythmSongSelectionPage.SelectionEventData();
            Field field = RhythmSongSelectionPage.SelectionEventData.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(eventData, value);
            return eventData;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to build selection event for field " + fieldName, exception);
        }
    }
}
