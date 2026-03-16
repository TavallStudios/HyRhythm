package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmSong;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionPhase;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class RhythmSongSelectionPage extends InteractiveCustomUIPage<RhythmSongSelectionPage.SelectionEventData> {
    private static final String PAGE_DOCUMENT = "Pages/RhythmSongSelectionPage.ui";
    private static final String BUTTON_DOCUMENT = "Pages/RhythmSelectionButton.ui";
    private static final Value<String> BUTTON_LABEL_STYLE = Value.ref(BUTTON_DOCUMENT, "LabelStyle");
    private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref(BUTTON_DOCUMENT, "SelectedLabelStyle");

    private final UUID playerId;
    private final String playerName;
    private final String sessionId;
    private final List<RhythmSong> songs;
    private final RhythmSessionService sessionService;
    private final RhythmLoggingService loggingService;

    private RhythmSessionPhase sessionPhase;
    private String selectedSongId;
    private String selectedChartId;
    private String confirmedChartId;

    public RhythmSongSelectionPage(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmSessionSnapshot session,
        List<RhythmSong> songs,
        RhythmSessionService sessionService,
        RhythmLoggingService loggingService
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, SelectionEventData.CODEC);
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.playerName = Objects.requireNonNull(playerName, "playerName");
        this.sessionId = Objects.requireNonNull(session, "session").sessionId();
        this.songs = List.copyOf(songs);
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
        this.sessionPhase = session.phase();
        this.confirmedChartId = session.chartId();
        initializeSelection(session.chartId());
    }

    @Override
    public void build(
        Ref<EntityStore> entityRef,
        UICommandBuilder uiCommandBuilder,
        UIEventBuilder uiEventBuilder,
        Store<EntityStore> entityStore
    ) {
        ensureSelection();
        uiCommandBuilder.append(PAGE_DOCUMENT);
        buildSongList(uiCommandBuilder, uiEventBuilder);
        buildChartList(uiCommandBuilder, uiEventBuilder);
        bindActionButtons(uiEventBuilder);
        applySummary(uiCommandBuilder);
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder);
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "song_selection_ui_payload_built",
            getClass().getName(),
            baseFields(),
            uiCommandBuilder,
            uiEventBuilder
        );
        loggingService.debug("ui", "song_selection_ui_built", baseFields());
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> entityRef, Store<EntityStore> entityStore, SelectionEventData eventData) {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        if (eventData.songId != null) {
            if (selectSongPreview(eventData.songId)) {
                buildSongList(uiCommandBuilder, uiEventBuilder);
                buildChartList(uiCommandBuilder, uiEventBuilder);
                applySummary(uiCommandBuilder);
                loggingService.info("ui", "song_highlighted", baseFields());
                pushPreviewUpdate(uiCommandBuilder, uiEventBuilder);
            }
            return;
        }

        if (eventData.chartId != null) {
            if (selectChartPreview(eventData.chartId)) {
                buildSongList(uiCommandBuilder, uiEventBuilder);
                buildChartList(uiCommandBuilder, uiEventBuilder);
                applySummary(uiCommandBuilder);
                loggingService.info("ui", "chart_highlighted", baseFields());
                pushPreviewUpdate(uiCommandBuilder, uiEventBuilder);
            }
            return;
        }

        if ("Confirm".equals(eventData.action)) {
            confirmSelection(entityStore);
            return;
        }

        if ("Close".equals(eventData.action)) {
            loggingService.info("ui", "song_selection_ui_close_requested", baseFields());
            closePage();
        }
    }

    @Override
    public void onDismiss(Ref<EntityStore> entityRef, Store<EntityStore> entityStore) {
        loggingService.info("ui", "song_selection_ui_closed", baseFields());
    }

    private void initializeSelection(String initialChartId) {
        if (initialChartId != null) {
            RhythmSong selectedSong = findSongByChartId(initialChartId);
            if (selectedSong != null) {
                selectedSongId = selectedSong.songId();
                selectedChartId = initialChartId;
            }
        }
        ensureSelection();
    }

    private void ensureSelection() {
        if (songs.isEmpty()) {
            selectedSongId = null;
            selectedChartId = null;
            return;
        }

        RhythmSong selectedSong = currentSong();
        if (selectedSong == null) {
            selectedSong = songs.getFirst();
            selectedSongId = selectedSong.songId();
        }

        if (selectedSong.charts().isEmpty()) {
            selectedChartId = null;
            return;
        }

        RhythmChart selectedChart = currentChart();
        if (selectedChart == null) {
            selectedChartId = selectedSong.charts().getFirst().chartId();
        }
    }

    private void buildSongList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.clear("#SongList");
        for (int index = 0; index < songs.size(); index++) {
            RhythmSong song = songs.get(index);
            String buttonSelector = buttonSelector("#SongList", index);
            uiCommandBuilder.append("#SongList", BUTTON_DOCUMENT);
            uiCommandBuilder.set(buttonSelector + ".Text", song.title() + " - " + song.artist());
            uiCommandBuilder.set(
                buttonSelector + ".Style",
                song.songId().equals(selectedSongId) ? BUTTON_LABEL_STYLE_SELECTED : BUTTON_LABEL_STYLE
            );
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                buttonSelector,
                EventData.of(SelectionEventData.KEY_SONG, song.songId()),
                false
            );
        }
    }

    private void buildChartList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.clear("#ChartList");
        RhythmSong song = currentSong();
        if (song == null) {
            return;
        }

        for (int index = 0; index < song.charts().size(); index++) {
            RhythmChart chart = song.charts().get(index);
            String buttonSelector = buttonSelector("#ChartList", index);
            uiCommandBuilder.append("#ChartList", BUTTON_DOCUMENT);
            uiCommandBuilder.set(buttonSelector + ".Text", chartLabel(chart));
            uiCommandBuilder.set(
                buttonSelector + ".Style",
                chart.chartId().equals(selectedChartId) ? BUTTON_LABEL_STYLE_SELECTED : BUTTON_LABEL_STYLE
            );
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                buttonSelector,
                EventData.of(SelectionEventData.KEY_CHART, chart.chartId()),
                false
            );
        }
    }

    private void bindActionButtons(UIEventBuilder uiEventBuilder) {
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ConfirmButton",
            EventData.of(SelectionEventData.KEY_ACTION, "Confirm"),
            false
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CancelButton",
            EventData.of(SelectionEventData.KEY_ACTION, "Close"),
            false
        );
    }

    private void applySummary(UICommandBuilder uiCommandBuilder) {
        RhythmSong song = currentSong();
        RhythmChart chart = currentChart();

        uiCommandBuilder.set("#SongTitle.Text", song == null ? "No songs available" : song.title());
        uiCommandBuilder.set("#SongArtist.Text", song == null ? "" : "Artist: " + song.artist());
        uiCommandBuilder.set("#SessionSummary.Text", "Session: " + sessionId + " | Phase: " + sessionPhase.name());
        uiCommandBuilder.set("#DifficultyName.Text", chart == null ? "Difficulty: -" : "Difficulty: " + chart.metadata().difficultyName());
        uiCommandBuilder.set("#ChartIdentifier.Text", chart == null ? "Chart ID: -" : "Chart ID: " + chart.chartId());
        uiCommandBuilder.set("#KeyMode.Text", chart == null ? "Key Mode: -" : "Key Mode: " + chart.keyMode() + "K");
        uiCommandBuilder.set(
            "#NoteSummary.Text",
            chart == null
                ? "Notes: -"
                : "Notes: " + chart.notes().size() + " | Holds: " + chart.holdCount()
        );
        uiCommandBuilder.set(
            "#SourceName.Text",
            chart == null ? "Source: -" : "Source: " + chart.metadata().sourceName()
        );
        uiCommandBuilder.set("#StatusText.Text", buildStatusText(chart));
        uiCommandBuilder.set("#ConfirmButton.Text", chart == null ? "No Chart Selected" : "Select Chart");
        uiCommandBuilder.set("#CancelButton.Text", "Close");
    }

    private String buildStatusText(RhythmChart chart) {
        if (chart == null) {
            return "No playable 4K chart is available.";
        }
        if (Objects.equals(chart.chartId(), confirmedChartId)) {
            return "Chart is selected for this session. Use /rhythm start to begin.";
        }
        return "Previewing " + chart.metadata().difficultyName() + ". Confirm to ready the session.";
    }

    private boolean selectSongPreview(String songId) {
        RhythmSong song = findSongById(songId);
        if (song == null) {
            return false;
        }

        selectedSongId = song.songId();
        if (song.charts().isEmpty()) {
            selectedChartId = null;
        } else if (currentChart() == null || !song.songId().equals(currentChart().songId())) {
            selectedChartId = song.charts().getFirst().chartId();
        }
        return true;
    }

    private boolean selectChartPreview(String chartId) {
        RhythmSong song = findSongByChartId(chartId);
        if (song == null) {
            return false;
        }

        selectedSongId = song.songId();
        selectedChartId = chartId;
        return true;
    }

    private void confirmSelection(Store<EntityStore> entityStore) {
        RhythmChart chart = currentChart();
        if (chart == null) {
            sendPlayerMessage(entityStore, "No chart is selected.", "red");
            return;
        }

        try {
            RhythmSessionSnapshot updatedSession = sessionService.selectChart(playerId, playerName, chart.chartId());
            confirmedChartId = updatedSession.chartId();
            sessionPhase = updatedSession.phase();
            sendPlayerMessage(entityStore, "Selected chart " + updatedSession.chartId() + ". Use /rhythm start.", "green");
            loggingService.info("ui", "chart_confirmed", baseFields());
            closePage();
        } catch (IllegalStateException exception) {
            sendPlayerMessage(entityStore, exception.getMessage(), "red");
            loggingService.warn(
                "ui",
                "chart_confirm_failed",
                new LinkedHashMap<>() {{
                    putAll(baseFields());
                    put("reason", exception.getMessage());
                }}
            );
        }
    }

    private void closePage() {
        if (playerRef == null || playerRef.getReference() == null) {
            return;
        }
        close();
    }

    private void pushPreviewUpdate(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        if (playerRef == null) {
            return;
        }
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder);
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "song_selection_ui_payload_updated",
            getClass().getName(),
            baseFields(),
            uiCommandBuilder,
            uiEventBuilder
        );
        sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    private void sendPlayerMessage(Store<EntityStore> entityStore, String message, String color) {
        if (entityStore == null || playerRef == null || playerRef.getReference() == null) {
            return;
        }
        Player player = entityStore.getComponent(playerRef.getReference(), Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw(message).color(color));
        }
    }

    private RhythmSong currentSong() {
        return findSongById(selectedSongId);
    }

    private RhythmChart currentChart() {
        RhythmSong song = currentSong();
        if (song == null || selectedChartId == null) {
            return null;
        }
        return findChartById(song, selectedChartId);
    }

    private RhythmSong findSongById(String songId) {
        if (songId == null) {
            return null;
        }
        for (RhythmSong song : songs) {
            if (song.songId().equals(songId)) {
                return song;
            }
        }
        return null;
    }

    private RhythmSong findSongByChartId(String chartId) {
        if (chartId == null) {
            return null;
        }
        for (RhythmSong song : songs) {
            if (findChartById(song, chartId) != null) {
                return song;
            }
        }
        return null;
    }

    private static RhythmChart findChartById(RhythmSong song, String chartId) {
        for (RhythmChart chart : song.charts()) {
            if (chart.chartId().equals(chartId)) {
                return chart;
            }
        }
        return null;
    }

    private LinkedHashMap<String, Object> baseFields() {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("sessionId", sessionId);
        fields.put("playerId", playerId);
        fields.put("player", playerName);
        fields.put("phase", sessionPhase);
        fields.put("songId", selectedSongId == null ? "none" : selectedSongId);
        fields.put("chartId", selectedChartId == null ? "none" : selectedChartId);
        fields.put("confirmedChartId", confirmedChartId == null ? "none" : confirmedChartId);
        return fields;
    }

    private static String buttonSelector(String listSelector, int index) {
        return listSelector + "[" + index + "] #Button";
    }

    private static String chartLabel(RhythmChart chart) {
        return chart.metadata().difficultyName()
            + " | "
            + chart.keyMode()
            + "K | OD "
            + String.format(Locale.ROOT, "%.1f", chart.overallDifficulty());
    }

    public static final class SelectionEventData {
        static final String KEY_SONG = "Song";
        static final String KEY_CHART = "Chart";
        static final String KEY_ACTION = "Action";

        public static final BuilderCodec<SelectionEventData> CODEC = BuilderCodec.builder(
            SelectionEventData.class,
            SelectionEventData::new
        )
            .append(new KeyedCodec<>(KEY_SONG, Codec.STRING), (value, field) -> value.songId = field, value -> value.songId)
            .add()
            .append(new KeyedCodec<>(KEY_CHART, Codec.STRING), (value, field) -> value.chartId = field, value -> value.chartId)
            .add()
            .append(new KeyedCodec<>(KEY_ACTION, Codec.STRING), (value, field) -> value.action = field, value -> value.action)
            .add()
            .build();

        private String songId;
        private String chartId;
        private String action;

        public SelectionEventData() {
        }

        static SelectionEventData song(String songId) {
            SelectionEventData eventData = new SelectionEventData();
            eventData.songId = songId;
            return eventData;
        }

        static SelectionEventData chart(String chartId) {
            SelectionEventData eventData = new SelectionEventData();
            eventData.chartId = chartId;
            return eventData;
        }

        static SelectionEventData action(String action) {
            SelectionEventData eventData = new SelectionEventData();
            eventData.action = action;
            return eventData;
        }
    }
}
