package com.hyrhythm.ui;

import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.gameplay.model.RhythmGameplayNoteView;
import com.hyrhythm.gameplay.model.RhythmGameplaySnapshot;
import com.hyrhythm.gameplay.model.RhythmInputProcessingResult;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.session.interfaces.RhythmSessionService;
import com.hyrhythm.session.model.RhythmSessionSnapshot;
import com.hyrhythm.settings.model.RhythmLaneKeys;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RhythmGameplayPage extends InteractiveCustomUIPage<RhythmGameplayPage.RawEventData> {
    private static final String PAGE_DOCUMENT = "Pages/RhythmGameplayPage.ui";
    private static final String LANE_BUTTON_DOCUMENT = "Pages/RhythmGameplayLaneButton.ui";
    private static final String ARROW_TEXTURE = "RhythmGameplayArrow.png";
    private static final String CLOSE_BUTTON_SELECTOR = "#ExitGameplayButton";
    private static final String LANE_TAP_ACTION = "LaneTap";
    private static final long DEFAULT_REFRESH_INTERVAL_MS = 16L;
    private static final String REFRESH_INTERVAL_ENV = "HYRHYTHM_GAMEPLAY_UI_REFRESH_INTERVAL_MS";
    private static final int UNKNOWN_SOUND_EVENT_INDEX = Integer.MIN_VALUE;
    private static final long TRACK_WINDOW_AHEAD_MS = 3000L;
    private static final long TRACK_LATE_GRACE_MS = 200L;
    private static final int TRACK_HEIGHT_PX = 408;
    private static final int NOTE_HEIGHT_PX = 32;
    private static final int NOTE_MARGIN_PX = 4;
    private static final Pattern FIELD_PATTERN = Pattern.compile("\"([@A-Za-z0-9_]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}\\s]+))");

    private final UUID playerId;
    private final String playerName;
    private final String sessionId;
    private final RhythmChart chart;
    private final RhythmPlayerSettings settings;
    private final RhythmGameplayService gameplayService;
    private final RhythmSessionService sessionService;
    private final RhythmGameplayUiScheduler uiScheduler;
    private final RhythmLoggingService loggingService;
    private final long refreshIntervalMs;
    private final String soundEventId;

    private final AtomicBoolean refreshStarted = new AtomicBoolean(false);
    private final AtomicBoolean audioStarted = new AtomicBoolean(false);

    private volatile ScheduledFuture<?> refreshFuture;
    private volatile RhythmGameplaySnapshot lastSnapshot;
    private volatile long baselineEpochMillis;
    private volatile boolean dismissed;
    private volatile String lastCaptureStatus;
    private volatile String lastCaptureDebug;

    private final Map<Integer, String> laneInputFeedByLane = new ConcurrentHashMap<>();

    public RhythmGameplayPage(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmSessionSnapshot session,
        RhythmChart chart,
        RhythmPlayerSettings settings,
        RhythmGameplayService gameplayService,
        RhythmSessionService sessionService,
        RhythmGameplayUiScheduler uiScheduler,
        RhythmLoggingService loggingService,
        String soundEventId
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, RawEventData.CODEC);
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.playerName = Objects.requireNonNull(playerName, "playerName");
        this.sessionId = Objects.requireNonNull(session, "session").sessionId();
        this.chart = Objects.requireNonNull(chart, "chart");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.gameplayService = Objects.requireNonNull(gameplayService, "gameplayService");
        this.sessionService = Objects.requireNonNull(sessionService, "sessionService");
        this.uiScheduler = Objects.requireNonNull(uiScheduler, "uiScheduler");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
        this.refreshIntervalMs = resolveRefreshIntervalMs();
        this.soundEventId = soundEventId == null || soundEventId.isBlank() ? null : soundEventId;

        this.lastSnapshot = gameplayService.getActiveGameplay(playerId)
            .or(() -> gameplayService.getLastGameplay(playerId))
            .orElse(null);
        this.baselineEpochMillis = System.currentTimeMillis() - (lastSnapshot == null ? 0L : lastSnapshot.songTimeMillis());
        this.lastCaptureStatus = "Last input: waiting";
        this.lastCaptureDebug = "Capture debug: waiting";
    }

    @Override
    public void build(
        Ref<EntityStore> entityRef,
        UICommandBuilder uiCommandBuilder,
        UIEventBuilder uiEventBuilder,
        Store<EntityStore> entityStore
    ) {
        dismissed = false;
        RhythmGameplaySnapshot snapshot = currentSnapshot();
        uiCommandBuilder.append(PAGE_DOCUMENT);
        buildLaneHud(uiCommandBuilder);
        buildBindings(uiEventBuilder);
        applySnapshot(uiCommandBuilder, snapshot);
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder);
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "gameplay_ui_payload_built",
            getClass().getName(),
            baseFields(),
            uiCommandBuilder,
            uiEventBuilder
        );
        startRefreshLoop();
        playChartAudio(snapshot);
        loggingService.info("ui", "gameplay_ui_built", baseFields());
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> entityRef, Store<EntityStore> entityStore, String rawEventData) {
        Map<String, String> fields = parseFields(rawEventData);
        loggingService.debug(
            "ui",
            "gameplay_ui_event_received",
            extend(baseFields(), "raw", rawEventData, "fields", fields)
        );

        String request = firstPresent(fields, "Request", "request");
        if (request != null) {
            handleRequest(request, entityStore);
            return;
        }

        String action = firstPresent(fields, "Action", "action");
        if (LANE_TAP_ACTION.equalsIgnoreCase(action)) {
            handleLaneTap(fields, entityStore);
            return;
        }
        if ("CaptureKey".equalsIgnoreCase(action)) {
            handleCapturedKey(fields, entityStore);
            return;
        }

        loggingService.debug("ui", "gameplay_ui_event_ignored", extend(baseFields(), "raw", rawEventData));
    }

    @Override
    public void onDismiss(Ref<EntityStore> entityRef, Store<EntityStore> entityStore) {
        dismissed = true;
        stopRefreshLoop();
        loggingService.info("ui", "gameplay_ui_closed", baseFields());
    }

    private void handleRequest(String request, Store<EntityStore> entityStore) {
        switch (request.trim().toUpperCase(Locale.ROOT)) {
            case "STOP" -> {
                sessionService.stopSession(playerId, playerName, "ui_stop");
                lastSnapshot = gameplayService.getLastGameplay(playerId).orElse(lastSnapshot);
                stopRefreshLoop();
                loggingService.info("ui", "gameplay_stop_requested", baseFields());
                sendPlayerMessage(entityStore, "Stopped rhythm session " + sessionId + ".", "yellow");
                pushSnapshotUpdate(currentSnapshot(), false);
            }
            case "CLOSE" -> {
                loggingService.info("ui", "gameplay_close_requested", baseFields());
                close();
            }
            default -> loggingService.debug("ui", "gameplay_unknown_request", extend(baseFields(), "request", request));
        }
    }

    private void buildLaneHud(UICommandBuilder uiCommandBuilder) {
        for (int lane = 1; lane <= chart.keyMode(); lane++) {
            uiCommandBuilder.append(laneControlRowSelector(lane), LANE_BUTTON_DOCUMENT);
            uiCommandBuilder.set(laneButtonTextSelector(lane), laneButtonText(lane));
        }
    }

    private void buildBindings(UIEventBuilder uiEventBuilder) {
        for (int lane = 1; lane <= chart.keyMode(); lane++) {
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                laneButtonSelector(lane),
                new EventData()
                    .append("Action", LANE_TAP_ACTION)
                    .append("Lane", Integer.toString(lane)),
                false
            );
        }
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#StopButton",
            EventData.of("Request", "Stop"),
            false
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            CLOSE_BUTTON_SELECTOR,
            EventData.of("Request", "Close"),
            false
        );
    }

    private void handleLaneTap(Map<String, String> fields, Store<EntityStore> entityStore) {
        int lane = parseLane(fields);
        if (lane <= 0 || lane > chart.keyMode()) {
            loggingService.debug("ui", "gameplay_lane_tap_ignored", extend(baseFields(), "fields", fields));
            return;
        }
        submitLaneInput(lane, settings.laneKeys().keyForLane(lane), "button", entityStore);
    }

    private RhythmGameplaySnapshot currentSnapshot() {
        RhythmGameplaySnapshot activeSnapshot = gameplayService.getActiveGameplay(playerId).orElse(null);
        if (activeSnapshot != null) {
            lastSnapshot = activeSnapshot;
            baselineEpochMillis = System.currentTimeMillis() - activeSnapshot.songTimeMillis();
            return activeSnapshot;
        }

        RhythmGameplaySnapshot completedSnapshot = gameplayService.getLastGameplay(playerId).orElse(lastSnapshot);
        if (completedSnapshot != null) {
            lastSnapshot = completedSnapshot;
        }
        return completedSnapshot;
    }

    private void pushSnapshotUpdate(RhythmGameplaySnapshot snapshot, boolean clearCaptureValue) {
        if (dismissed || playerRef == null) {
            return;
        }

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        if (snapshot != null) {
            applySnapshot(uiCommandBuilder, snapshot);
        }
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, new UIEventBuilder());
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "gameplay_ui_payload_updated",
            getClass().getName(),
            baseFields(),
            uiCommandBuilder,
            null
        );
        sendUpdate(uiCommandBuilder, false);
    }

    private void handleCapturedKey(Map<String, String> fields, Store<EntityStore> entityStore) {
        String rawCaptureValue = firstPresent(fields, "@CaptureValue", "CaptureValue");
        List<String> normalizedKeys = extractCapturedKeys(rawCaptureValue);
        if (normalizedKeys.isEmpty()) {
            return;
        }

        RhythmGameplaySnapshot latestSnapshot = currentSnapshot();
        if (latestSnapshot == null || latestSnapshot.completed()) {
            loggingService.debug(
                "ui",
                "gameplay_capture_ignored_inactive",
                extend(
                    baseFields(),
                    "rawCaptureValue", rawCaptureValue
                )
            );
            return;
        }
        for (String normalizedKey : normalizedKeys) {
            int lane = settings.laneKeys().laneForKey(normalizedKey);
            if (lane == 0) {
                lastCaptureStatus = "Last input: " + normalizedKey + " -> unmapped";
                lastCaptureDebug = "Capture debug: key=" + normalizedKey + " lane=0";
                loggingService.debug(
                    "ui",
                    "gameplay_capture_unmapped",
                    extend(baseFields(), "capturedKey", normalizedKey)
                );
                sendPlayerMessage(entityStore, "Rhythm input: " + normalizedKey + " -> unmapped", "yellow");
                continue;
            }
            submitLaneInput(lane, normalizedKey, "capture", entityStore);
        }

        if (latestSnapshot != null && latestSnapshot.completed()) {
            stopRefreshLoop();
        }
    }

    private void applySnapshot(UICommandBuilder uiCommandBuilder, RhythmGameplaySnapshot snapshot) {
        String statusText;
        String judgmentText;
        String debugText;

        if (snapshot == null) {
            statusText = "No active gameplay snapshot is available.";
            judgmentText = "Last Judgment: -";
            debugText = "Waiting for gameplay state.";
            uiCommandBuilder.set("#ClockValue.Text", "0ms");
            uiCommandBuilder.set("#ScoreValue.Text", "0");
            uiCommandBuilder.set("#ComboValue.Text", "0");
            uiCommandBuilder.set("#AccuracyValue.Text", "100.00%");
            for (int lane = 1; lane <= chart.keyMode(); lane++) {
                applyLaneState(uiCommandBuilder, lane, false);
                uiCommandBuilder.set(laneFeedSelector(lane), laneFeedText(lane, null));
                renderLaneNotes(uiCommandBuilder, lane, List.of(), 0L);
            }
        } else {
            statusText = snapshot.completed()
                ? "Finished: " + snapshot.finishReason() + ". Use /rhythm ui to reselect a chart."
                : "Playing " + chart.metadata().difficultyName() + " | " + chart.keyMode() + "K | session " + sessionId;
            judgmentText = "Last Judgment: " + (snapshot.lastJudgment() == null ? "-" : snapshot.lastJudgment().name())
                + " | delta=" + (snapshot.lastDeltaMillis() == null ? "-" : snapshot.lastDeltaMillis() + "ms");
            debugText = snapshot.summary();
            long displayTimeMillis = displaySongTimeMillis(snapshot.songTimeMillis());
            uiCommandBuilder.set("#ClockValue.Text", displayTimeMillis + "ms");
            uiCommandBuilder.set("#ScoreValue.Text", Integer.toString(snapshot.score()));
            uiCommandBuilder.set("#ComboValue.Text", Integer.toString(snapshot.combo()));
            uiCommandBuilder.set("#AccuracyValue.Text", String.format(Locale.ROOT, "%.2f%%", snapshot.accuracyPercent()));
            for (int lane = 1; lane <= chart.keyMode(); lane++) {
                applyLaneState(uiCommandBuilder, lane, snapshot.heldLanes().contains(lane));
                uiCommandBuilder.set(laneFeedSelector(lane), laneFeedText(lane, snapshot));
                renderLaneNotes(uiCommandBuilder, lane, snapshot.remainingNotes(), displaySongTimeMillis(snapshot.songTimeMillis()));
            }
        }

        uiCommandBuilder.set("#SongTitle.Text", chart.metadata().title());
        uiCommandBuilder.set("#SongMeta.Text", chart.metadata().artist() + " | " + chart.metadata().difficultyName() + " | " + chart.keyMode() + "K");
        uiCommandBuilder.set("#BindSummary.Text", "Keys: " + settings.laneKeys().toDisplayString());
        uiCommandBuilder.set("#StatusText.Text", statusText);
        uiCommandBuilder.set("#JudgmentText.Text", judgmentText);
        uiCommandBuilder.set("#DebugText.Text", debugText + " | " + lastCaptureStatus + " | " + lastCaptureDebug);
        uiCommandBuilder.set("#StopButton.Text", snapshot != null && snapshot.completed() ? "Stop Closed" : "Stop Match");
    }

    private void applyLaneState(UICommandBuilder uiCommandBuilder, int lane, boolean held) {
        uiCommandBuilder.set(
            laneStatusSelector(lane),
            held ? "HELD" : "READY"
        );
    }

    private void renderLaneNotes(UICommandBuilder uiCommandBuilder, int lane, List<RhythmGameplayNoteView> remainingNotes, long songTimeMillis) {
        String trackSelector = laneTrackSelector(lane);
        uiCommandBuilder.clear(trackSelector);

        List<RhythmGameplayNoteView> laneNotes = remainingNotes.stream()
            .filter(note -> note.lane() == lane)
            .filter(note -> displayTime(note) >= songTimeMillis - TRACK_LATE_GRACE_MS)
            .filter(note -> displayTime(note) <= songTimeMillis + TRACK_WINDOW_AHEAD_MS)
            .sorted(Comparator.comparingLong(RhythmGameplayPage::displayTime).reversed())
            .toList();

        int cursorY = 0;
        for (RhythmGameplayNoteView note : laneNotes) {
            int noteHeight = noteHeight(note);
            int y = lanePosition(songTimeMillis, note, noteHeight);
            int spacerHeight = Math.max(0, y - cursorY);
            if (spacerHeight > 0) {
                uiCommandBuilder.appendInline(trackSelector, spacerInline(spacerHeight));
            }
            uiCommandBuilder.appendInline(trackSelector, noteInline(note, songTimeMillis, noteHeight));
            cursorY = y + noteHeight + NOTE_MARGIN_PX;
        }
    }

    private long currentSongTimeMillis() {
        return Math.max(0L, System.currentTimeMillis() - baselineEpochMillis);
    }

    private long displaySongTimeMillis(long rawSongTimeMillis) {
        return Math.max(0L, rawSongTimeMillis + settings.globalOffsetMs());
    }

    private void startRefreshLoop() {
        if (!refreshStarted.compareAndSet(false, true)) {
            return;
        }
        if (refreshIntervalMs <= 0L) {
            refreshStarted.set(false);
            loggingService.info("ui", "gameplay_ui_refresh_disabled", extend(baseFields(), "refreshIntervalMs", refreshIntervalMs));
            return;
        }
        refreshFuture = uiScheduler.scheduleAtFixedRate(this::refreshFromClock, refreshIntervalMs, refreshIntervalMs);
    }

    private void stopRefreshLoop() {
        ScheduledFuture<?> future = refreshFuture;
        refreshFuture = null;
        refreshStarted.set(false);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void refreshFromClock() {
        if (dismissed) {
            stopRefreshLoop();
            return;
        }

        try {
            RhythmGameplaySnapshot snapshot = gameplayService.getActiveGameplay(playerId)
                .map(ignored -> gameplayService.advanceGameplay(playerId, playerName, currentSongTimeMillis()))
                .orElseGet(() -> gameplayService.getLastGameplay(playerId).orElse(lastSnapshot));
            if (snapshot == null) {
                return;
            }
            lastSnapshot = snapshot;
            pushSnapshotUpdate(snapshot, false);
            if (snapshot.completed()) {
                stopRefreshLoop();
            }
        } catch (IllegalStateException exception) {
            loggingService.warn(
                "ui",
                "gameplay_refresh_failed",
                extend(baseFields(), "reason", exception.getMessage())
            );
            stopRefreshLoop();
        }
    }

    private long resolveRefreshIntervalMs() {
        String rawValue = System.getenv(REFRESH_INTERVAL_ENV);
        if (rawValue == null || rawValue.isBlank()) {
            return DEFAULT_REFRESH_INTERVAL_MS;
        }
        try {
            return Math.max(0L, Long.parseLong(rawValue.trim()));
        } catch (NumberFormatException exception) {
            loggingService.warn(
                "ui",
                "gameplay_ui_refresh_interval_invalid",
                extend(baseFields(), "env", REFRESH_INTERVAL_ENV, "rawValue", rawValue)
            );
            return DEFAULT_REFRESH_INTERVAL_MS;
        }
    }

    private static Map<String, String> parseFields(String rawEventData) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        if (rawEventData == null || rawEventData.isBlank()) {
            return fields;
        }

        Matcher matcher = FIELD_PATTERN.matcher(rawEventData);
        while (matcher.find()) {
            String key = matcher.group(1);
            String quotedValue = matcher.group(2);
            String rawValue = matcher.group(3);
            fields.put(key, quotedValue != null ? quotedValue : rawValue);
        }
        return fields;
    }

    private static String firstPresent(Map<String, String> fields, String... keys) {
        for (String key : keys) {
            String value = fields.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String normalizeCapturedKey(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return RhythmLaneKeys.normalize(trimmed.substring(trimmed.length() - 1));
    }

    private List<String> extractCapturedKeys(String rawValue) {
        String normalizedKey = normalizeCapturedKey(rawValue);
        if (normalizedKey == null) {
            return List.of();
        }
        return List.of(normalizedKey);
    }

    private String laneFeedText(int lane, RhythmGameplaySnapshot snapshot) {
        String nextNoteFeed = nextNoteFeed(lane, snapshot);
        String inputFeed = laneInputFeedByLane.getOrDefault(lane, "-");
        return "Next " + nextNoteFeed + " | Input " + inputFeed;
    }

    private void submitLaneInput(int lane, String inputLabel, String inputSource, Store<EntityStore> entityStore) {
        try {
            long songTimeMillis = currentSongTimeMillis();
            RhythmInputProcessingResult result = gameplayService.submitLaneInput(
                playerId,
                playerName,
                RhythmLaneInputAction.DOWN,
                lane,
                songTimeMillis
            );
            RhythmGameplaySnapshot snapshot = result.snapshot();
            lastSnapshot = snapshot;
            baselineEpochMillis = System.currentTimeMillis() - snapshot.songTimeMillis();
            String laneFeed = compactLaneFeed(inputLabel, result, songTimeMillis);
            laneInputFeedByLane.put(lane, laneFeed);
            lastCaptureStatus = "Last input: " + laneFeed;
            lastCaptureDebug = "Capture debug: " + result.debugSummary() + " source=" + inputSource;
            loggingService.info(
                "ui",
                "gameplay_lane_input_submitted",
                extend(
                    baseFields(),
                    "inputSource", inputSource,
                    "inputLabel", inputLabel,
                    "lane", lane,
                    "songTimeMs", songTimeMillis,
                    "result", result.judgment() == null ? "none" : result.judgment().type(),
                    "debugSummary", result.debugSummary()
                )
            );
            sendPlayerMessage(
                entityStore,
                "Rhythm input debug: " + result.debugSummary(),
                debugMessageColor(result)
            );
        } catch (IllegalStateException exception) {
            lastCaptureStatus = "Last input: " + inputLabel + " -> failed";
            lastCaptureDebug = "Capture debug: key=" + inputLabel + " failed=" + exception.getMessage() + " source=" + inputSource;
            loggingService.warn(
                "ui",
                "gameplay_lane_input_failed",
                extend(baseFields(), "inputSource", inputSource, "inputLabel", inputLabel, "lane", lane, "reason", exception.getMessage())
            );
            sendPlayerMessage(entityStore, "Rhythm input failed for " + inputLabel + ": " + exception.getMessage(), "red");
        }
    }

    private String nextNoteFeed(int lane, RhythmGameplaySnapshot snapshot) {
        if (snapshot == null) {
            return "-";
        }
        long displaySongTimeMillis = displaySongTimeMillis(snapshot.songTimeMillis());
        return snapshot.remainingNotes().stream()
            .filter(note -> note.lane() == lane)
            .min(Comparator.comparingLong(RhythmGameplayPage::displayTime))
            .map(note -> formatNextNoteFeed(note, displaySongTimeMillis))
            .orElse("-");
    }

    private static String formatNextNoteFeed(RhythmGameplayNoteView note, long displaySongTimeMillis) {
        long etaMillis = Math.max(0L, displayTime(note) - displaySongTimeMillis);
        if (note.hold() && note.holdActive()) {
            return "tail " + etaMillis + "ms";
        }
        return (note.hold() ? "hold " : "tap ") + etaMillis + "ms";
    }

    private static String compactLaneFeed(String normalizedKey, RhythmInputProcessingResult result, long songTimeMillis) {
        if (result.judgment() == null) {
            return normalizedKey + " -> NONE @" + songTimeMillis + "ms";
        }
        return normalizedKey
            + " -> "
            + result.judgment().type().name()
            + " "
            + result.judgment().detail()
            + " @"
            + songTimeMillis
            + "ms";
    }

    private static String debugMessageColor(RhythmInputProcessingResult result) {
        if (result.judgment() == null) {
            return "yellow";
        }
        if (result.judgment().type().countsAsHit()) {
            return "green";
        }
        if (result.judgment().type() == com.hyrhythm.gameplay.model.RhythmJudgmentType.GHOST_TAP) {
            return "yellow";
        }
        return "red";
    }

    private static long displayTime(RhythmGameplayNoteView note) {
        return note.headResolved() ? note.endTimeMillis() : note.startTimeMillis();
    }

    private static int noteHeight(RhythmGameplayNoteView note) {
        if (!note.hold()) {
            return NOTE_HEIGHT_PX;
        }
        long durationMillis = Math.max(120L, note.endTimeMillis() - note.startTimeMillis());
        return NOTE_HEIGHT_PX + (int) Math.min(168L, durationMillis / 8L);
    }

    private static int lanePosition(long songTimeMillis, RhythmGameplayNoteView note, int noteHeight) {
        long deltaMillis = Math.max(0L, displayTime(note) - songTimeMillis);
        double distanceRatio = Math.min(1.0d, (double) deltaMillis / (double) TRACK_WINDOW_AHEAD_MS);
        return (int) Math.max(
            0,
            Math.round((TRACK_HEIGHT_PX - noteHeight) * (1.0d - distanceRatio))
        );
    }

    private static String spacerInline(int spacerHeight) {
        return "Group { Anchor: (Height: " + spacerHeight + "); }";
    }

    private static String noteInline(RhythmGameplayNoteView note, long songTimeMillis, int noteHeight) {
        String etaText = note.hold()
            ? "Hold " + Math.max(0L, note.endTimeMillis() - songTimeMillis) + "ms"
            : Math.max(0L, note.startTimeMillis() - songTimeMillis) + "ms";
        String backgroundColor = note.holdActive()
            ? "#4aa3ff(0.45)"
            : note.hold()
                ? "#f2c14a(0.35)"
                : "#ffffff(0.18)";

        return String.format(
            Locale.ROOT,
            """
                Group {
                  Anchor: (Height: %d, Bottom: %d);
                  Background: (Color: %s);

                  Sprite {
                    Anchor: (Width: 28, Height: 28, Left: 6, Top: 2);
                    TexturePath: "%s";
                    Frame: (Width: 32, Height: 32, PerRow: 1, Count: 1);
                  }

                  Label {
                    Text: "%s";
                    Style: (FontSize: 11, TextColor: #d9e4ef, VerticalAlignment: Center, HorizontalAlignment: End);
                    Padding: (Right: 8, Left: 40);
                  }
                }
                """,
            noteHeight,
            NOTE_MARGIN_PX,
            backgroundColor,
            ARROW_TEXTURE,
            etaText
        );
    }

    private void sendPlayerMessage(Store<EntityStore> entityStore, String message, String color) {
        if (playerRef != null) {
            playerRef.sendMessage(Message.raw(message).color(color));
            return;
        }
        if (entityStore == null || playerRef == null || playerRef.getReference() == null) {
            return;
        }
        Player player = entityStore.getComponent(playerRef.getReference(), Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw(message).color(color));
        }
    }

    private LinkedHashMap<String, Object> baseFields() {
        return extend(
            new LinkedHashMap<>(),
            "sessionId", sessionId,
            "playerId", playerId,
            "player", playerName,
            "chartId", chart.chartId()
        );
    }

    private static LinkedHashMap<String, Object> extend(LinkedHashMap<String, Object> fields, Object... extraValues) {
        for (int index = 0; index + 1 < extraValues.length; index += 2) {
            fields.put(String.valueOf(extraValues[index]), extraValues[index + 1]);
        }
        return fields;
    }

    private static String laneControlRowSelector(int lane) {
        return "#Lane" + lane + "ControlRow";
    }

    private static String laneButtonSelector(int lane) {
        return laneControlRowSelector(lane) + "[0] #LaneButton";
    }

    private static String laneButtonTextSelector(int lane) {
        return laneButtonSelector(lane) + ".Text";
    }

    private static String laneTrackSelector(int lane) {
        return "#Lane" + lane + "Track";
    }

    private static String laneStatusSelector(int lane) {
        return "#Lane" + lane + "Status.Text";
    }

    private static String laneFeedSelector(int lane) {
        return "#Lane" + lane + "Feed.Text";
    }

    private static int parseLane(Map<String, String> fields) {
        String rawLane = firstPresent(fields, "Lane", "lane");
        if (rawLane == null) {
            return 0;
        }
        try {
            return Integer.parseInt(rawLane.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String laneButtonText(int lane) {
        return "Lane " + lane + "  [" + settings.laneKeys().keyForLane(lane) + "]";
    }

    private void playChartAudio(RhythmGameplaySnapshot snapshot) {
        if (playerRef == null || soundEventId == null || !audioStarted.compareAndSet(false, true)) {
            return;
        }
        long songTimeAtAudioStart = snapshot == null ? 0L : Math.max(0L, snapshot.songTimeMillis());
        String resolvedSoundEventId = null;
        int resolvedSoundEventIndex = UNKNOWN_SOUND_EVENT_INDEX;
        SoundEvent resolvedSoundEvent = null;
        List<String> lookupCandidates = soundEventLookupCandidates(soundEventId);
        StringBuilder lookupSummary = new StringBuilder();
        for (String candidate : lookupCandidates) {
            SoundEvent candidateAsset = SoundEvent.getAssetMap().getAsset(candidate);
            int candidateIndex = SoundEvent.getAssetMap().getIndex(candidate);
            if (lookupSummary.length() > 0) {
                lookupSummary.append(" | ");
            }
            lookupSummary.append(candidate)
                .append(":index=")
                .append(candidateIndex)
                .append(":present=")
                .append(candidateAsset != null);
            if (resolvedSoundEvent == null && isValidSoundEventIndex(candidateIndex) && candidateAsset != null) {
                resolvedSoundEventId = candidate;
                resolvedSoundEventIndex = candidateIndex;
                resolvedSoundEvent = candidateAsset;
            }
        }
        loggingService.info(
            "audio",
            "gameplay_chart_audio_lookup",
            extend(
                baseFields(),
                "requestedSoundEventId", soundEventId,
                "lookupCandidates", String.join(" | ", lookupCandidates),
                "lookupSummary", lookupSummary.toString(),
                "songTimeAtAudioStartMs", songTimeAtAudioStart,
                "resolvedSoundEventId", resolvedSoundEventId == null ? "missing" : resolvedSoundEventId,
                "resolvedSoundEventIndex", resolvedSoundEventIndex
            )
        );
        if (!isValidSoundEventIndex(resolvedSoundEventIndex) || resolvedSoundEvent == null) {
            loggingService.warn(
                "audio",
                "gameplay_chart_audio_lookup_failed",
                extend(
                    baseFields(),
                    "requestedSoundEventId", soundEventId,
                    "lookupSummary", lookupSummary.toString(),
                    "songTimeAtAudioStartMs", songTimeAtAudioStart
                )
            );
            return;
        }
        SoundUtil.playSoundEvent2dToPlayer(playerRef, resolvedSoundEventIndex, SoundCategory.Music);
        loggingService.info(
            "audio",
            "gameplay_chart_audio_started",
            extend(
                baseFields(),
                "requestedSoundEventId", soundEventId,
                "resolvedSoundEventId", resolvedSoundEventId,
                "soundEventIndex", resolvedSoundEventIndex,
                "songTimeAtAudioStartMs", songTimeAtAudioStart,
                "audioCategoryId", resolvedSoundEvent.getAudioCategoryId(),
                "audioCategoryIndex", resolvedSoundEvent.getAudioCategoryIndex(),
                "layerCount", resolvedSoundEvent.getLayers() == null ? 0 : resolvedSoundEvent.getLayers().length
            )
        );
    }

    static List<String> soundEventLookupCandidates(String requestedSoundEventId) {
        if (requestedSoundEventId == null || requestedSoundEventId.isBlank()) {
            return List.of();
        }
        String trimmedId = requestedSoundEventId.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(trimmedId);
        String bareId = trimmedId;
        int lastSlash = trimmedId.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash + 1 < trimmedId.length()) {
            bareId = trimmedId.substring(lastSlash + 1);
            candidates.add(bareId);
        }
        candidates.add(assetKeyCandidate(bareId));
        return List.copyOf(candidates);
    }

    private static String assetKeyCandidate(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return "Unknown";
        }
        String trimmedId = rawId.trim();
        return Character.toUpperCase(trimmedId.charAt(0)) + trimmedId.substring(1);
    }

    private static boolean isValidSoundEventIndex(int soundEventIndex) {
        return soundEventIndex != SoundEvent.EMPTY_ID && soundEventIndex != UNKNOWN_SOUND_EVENT_INDEX;
    }

    public static final class RawEventData {
        public static final BuilderCodec<RawEventData> CODEC = BuilderCodec.builder(RawEventData.class, RawEventData::new)
            .append(new KeyedCodec<>("Action", Codec.STRING), (value, field) -> value.action = field, value -> value.action)
            .add()
            .build();

        private String action;

        public RawEventData() {
        }
    }
}
