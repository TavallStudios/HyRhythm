package com.hyrhythm.ui;

import com.hyrhythm.bootstrap.registries.CoreBootstrapRegistry;
import com.hyrhythm.bootstrap.registries.RhythmBootstrapRegistry;
import com.hyrhythm.content.interfaces.RhythmSongLibraryService;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmSongSelectionPageTest {
    private static final UUID PLAYER_ID = UUID.fromString("d50f5b7f-53df-4453-b223-9596158efdb4");

    private DependencyLoader loader;
    private RhythmSessionService sessionService;
    private RhythmSongLibraryService songLibraryService;
    private RhythmSongSelectionPageFactory pageFactory;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDataDirectory = Files.createTempDirectory("hyrhythm-song-selection-page-test");
        System.setProperty("hyrhythm.test.dataDir", tempDataDirectory.toString());

        loader = DependencyLoader.getFallbackDependencyLoader();
        loader.resetInstances();
        new CoreBootstrapRegistry().register(loader, null);
        new RhythmBootstrapRegistry().register(loader, null);
        loader.loadQueuedDependenciesInOrder();

        sessionService = loader.requireInstance(RhythmSessionService.class);
        songLibraryService = loader.requireInstance(RhythmSongLibraryService.class);
        pageFactory = loader.requireInstance(RhythmSongSelectionPageFactory.class);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("hyrhythm.test.dataDir");
        loader.resetInstances();
    }

    @Test
    void selectionEventsReadyTheSessionThroughTheRealUiPageFlow() {
        RhythmSessionSnapshot session = sessionService.joinOrCreateSession(PLAYER_ID, "UiSelector");
        var songs = songLibraryService.listSongs();
        var song = songs.getFirst();
        var chart = song.charts().getFirst();
        RhythmSongSelectionPage page = pageFactory.create(null, PLAYER_ID, "UiSelector", session, songs);

        page.build(null, new UICommandBuilder(), new UIEventBuilder(), null);
        page.handleDataEvent(null, null, RhythmSongSelectionPage.SelectionEventData.song(song.songId()));
        page.handleDataEvent(null, null, RhythmSongSelectionPage.SelectionEventData.chart(chart.chartId()));
        page.handleDataEvent(null, null, RhythmSongSelectionPage.SelectionEventData.action("Confirm"));

        RhythmSessionSnapshot updatedSession = sessionService.getSession(PLAYER_ID).orElseThrow();

        assertEquals(RhythmSessionPhase.READY, updatedSession.phase());
        assertEquals(chart.chartId(), updatedSession.chartId());
    }

    @Test
    void selectionPageBuildValidatesDeterministicUiPayload() {
        RhythmSessionSnapshot session = sessionService.joinOrCreateSession(PLAYER_ID, "UiSelector");
        var songs = songLibraryService.listSongs();
        RhythmSongSelectionPage page = pageFactory.create(null, PLAYER_ID, "UiSelector", session, songs);

        assertDoesNotThrow(() -> page.build(null, new UICommandBuilder(), new UIEventBuilder(), null));
    }

    @Test
    void selectionPageBuildBindsOnlyActivatingToSongChartAndActionButtons() {
        RhythmSessionSnapshot session = sessionService.joinOrCreateSession(PLAYER_ID, "UiSelector");
        var songs = songLibraryService.listSongs();
        RhythmSongSelectionPage page = pageFactory.create(null, PLAYER_ID, "UiSelector", session, songs);
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        page.build(null, uiCommandBuilder, uiEventBuilder, null);

        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#SongList[0] #Button");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ChartList[0] #Button");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#ConfirmButton");
        assertBinding(uiEventBuilder.getEvents(), CustomUIEventBindingType.Activating, "#CancelButton");
        for (CustomUIEventBinding eventBinding : uiEventBuilder.getEvents()) {
            if (eventBinding == null) {
                continue;
            }
            assertTrue(eventBinding.type == CustomUIEventBindingType.Activating);
        }
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
