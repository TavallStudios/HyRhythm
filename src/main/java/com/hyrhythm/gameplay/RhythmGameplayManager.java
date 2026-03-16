package com.hyrhythm.gameplay;

import com.hyrhythm.content.interfaces.RhythmSongLibraryAccess;
import com.hyrhythm.content.model.RhythmChart;
import com.hyrhythm.content.model.RhythmNote;
import com.hyrhythm.gameplay.interfaces.RhythmGameplayService;
import com.hyrhythm.gameplay.model.RhythmGameplaySnapshot;
import com.hyrhythm.gameplay.model.RhythmGameplayNoteView;
import com.hyrhythm.gameplay.model.RhythmInputProcessingResult;
import com.hyrhythm.gameplay.model.RhythmJudgmentResult;
import com.hyrhythm.gameplay.model.RhythmJudgmentType;
import com.hyrhythm.gameplay.model.RhythmLaneInputAction;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.session.interfaces.RhythmSessionAccess;
import com.hyrhythm.settings.interfaces.RhythmSettingsAccess;
import com.hyrhythm.settings.model.RhythmPlayerSettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class RhythmGameplayManager implements
    RhythmGameplayService,
    RhythmLoggingAccess,
    RhythmSettingsAccess,
    RhythmSongLibraryAccess,
    RhythmSessionAccess {

    private static final long PERFECT_WINDOW_MS = 32L;
    private static final long GREAT_WINDOW_MS = 64L;
    private static final long GOOD_WINDOW_MS = 96L;
    private static final long BAD_WINDOW_MS = 128L;
    private static final long MISS_WINDOW_MS = 160L;
    private static final long HOLD_RELEASE_WINDOW_MS = 64L;
    private static final int ACCURACY_UNIT_MAX = 6;

    private final ConcurrentMap<UUID, RhythmRuntimeState> activeByPlayerId = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, RhythmGameplaySnapshot> lastByPlayerId = new ConcurrentHashMap<>();

    @Override
    public RhythmGameplaySnapshot startGameplay(UUID playerId, String playerName, String sessionId, String chartId) {
        validatePlayer(playerId, playerName);
        String normalizedSessionId = requireText(sessionId, "sessionId");
        RhythmChart chart = findRhythmChartById(requireText(chartId, "chartId"))
            .orElseThrow(() -> new IllegalStateException("Selected chart '" + chartId + "' is not registered in the song library."));
        RhythmPlayerSettings settings = getOrCreateRhythmPlayerSettings(playerId, playerName);

        RhythmRuntimeState state = new RhythmRuntimeState(normalizedSessionId, playerId, playerName, chart, settings);
        RhythmRuntimeState existing = activeByPlayerId.put(playerId, state);
        if (existing != null) {
            logRhythmWarn(
                "gameplay",
                "gameplay_replaced",
                fields(state, "previousTraceId", existing.traceId)
            );
        }

        RhythmGameplaySnapshot snapshot = state.snapshot();
        lastByPlayerId.put(playerId, snapshot);
        logRhythmInfo(
            "gameplay",
            "gameplay_started",
            fields(
                state,
                "chartObjects", state.totalObjectCount,
                "offsetMs", settings.globalOffsetMs(),
                "scrollSpeed", settings.scrollSpeed()
            )
        );
        return snapshot;
    }

    @Override
    public RhythmGameplaySnapshot advanceGameplay(UUID playerId, String playerName, long songTimeMillis) {
        RhythmRuntimeState state = requireActiveState(playerId, playerName);
        synchronized (state) {
            RhythmJudgmentResult beforeAdvance = state.lastJudgment;
            state.advanceTo(songTimeMillis);
            logAutoJudgment(state, beforeAdvance);
            return snapshotAfterMutation(state, "time_advanced");
        }
    }

    @Override
    public RhythmInputProcessingResult submitLaneInput(
        UUID playerId,
        String playerName,
        RhythmLaneInputAction action,
        int lane,
        long songTimeMillis
    ) {
        validatePlayer(playerId, playerName);
        Objects.requireNonNull(action, "action");

        RhythmRuntimeState state = requireActiveState(playerId, playerName);
        synchronized (state) {
            validateLane(lane, state.chart.keyMode());

            boolean heldBefore = state.isLaneHeld(lane);
            RhythmJudgmentResult beforeAdvance = state.lastJudgment;
            state.advanceTo(songTimeMillis);
            logAutoJudgment(state, beforeAdvance);

            logRhythmDebug(
                "input",
                "lane_input_received",
                fields(
                    state,
                    "action", action,
                    "lane", lane,
                    "heldBefore", heldBefore,
                    "rawSongTimeMs", songTimeMillis,
                    "effectiveSongTimeMs", state.effectiveSongTimeMillis()
                )
            );

            String candidateBefore = state.describeLaneCandidate(lane);
            RhythmJudgmentResult judgment;
            if (action == RhythmLaneInputAction.DOWN) {
                if (heldBefore) {
                    logRhythmDebug(
                        "input",
                        "duplicate_key_down",
                        fields(state, "lane", lane, "rawSongTimeMs", songTimeMillis)
                    );
                    judgment = null;
                } else {
                    judgment = state.handleDown(lane);
                }
            } else if (!heldBefore) {
                logRhythmDebug(
                    "input",
                    "duplicate_key_up",
                    fields(state, "lane", lane, "rawSongTimeMs", songTimeMillis)
                );
                judgment = null;
            } else {
                judgment = state.handleUp(lane);
            }

            if (judgment != null) {
                logRhythmInfo(
                    "judgment",
                    "lane_judged",
                    fields(
                        state,
                        "action", action,
                        "lane", lane,
                        "noteId", judgment.noteId(),
                        "result", judgment.type(),
                        "deltaMs", judgment.deltaMillis(),
                        "comboAfter", judgment.comboAfter(),
                        "scoreAfter", judgment.scoreAfter(),
                        "accuracyAfter", judgment.accuracyAfter(),
                        "detail", judgment.detail()
                    )
                );
            }

            RhythmGameplaySnapshot snapshot = snapshotAfterMutation(state, "lane_input_processed");
            String debugSummary = state.describeInputOutcome(action, lane, heldBefore, judgment, candidateBefore);
            logRhythmDebug(
                "input",
                "lane_input_applied",
                fields(
                    state,
                    "action", action,
                    "lane", lane,
                    "heldAfter", state.isLaneHeld(lane),
                    "result", judgment == null ? "none" : judgment.type(),
                    "debugSummary", debugSummary
                )
            );
            return new RhythmInputProcessingResult(snapshot, judgment, debugSummary);
        }
    }

    @Override
    public RhythmGameplaySnapshot stopGameplay(UUID playerId, String playerName, String reason) {
        validatePlayer(playerId, playerName);

        RhythmRuntimeState state = activeByPlayerId.remove(playerId);
        if (state == null) {
            return lastByPlayerId.getOrDefault(
                playerId,
                new RhythmGameplaySnapshot(
                    "none",
                    "none",
                    playerId,
                    playerName,
                    "none",
                    "none",
                    0L,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    100.0d,
                    Set.of(),
                    null,
                    null,
                    0,
                    List.of(),
                    false,
                    true,
                    requireText(reason == null ? "stopped" : reason, "reason")
                )
            );
        }

        synchronized (state) {
            state.markCompleted(reason);
            RhythmGameplaySnapshot snapshot = state.snapshot();
            lastByPlayerId.put(playerId, snapshot);
            logRhythmInfo(
                "gameplay",
                "gameplay_stopped",
                fields(state, "reason", snapshot.finishReason())
            );
            return snapshot;
        }
    }

    @Override
    public Optional<RhythmGameplaySnapshot> getActiveGameplay(UUID playerId) {
        RhythmRuntimeState state = activeByPlayerId.get(playerId);
        if (state == null) {
            return Optional.empty();
        }
        synchronized (state) {
            return Optional.of(state.snapshot());
        }
    }

    @Override
    public Optional<RhythmGameplaySnapshot> getLastGameplay(UUID playerId) {
        return Optional.ofNullable(lastByPlayerId.get(playerId));
    }

    private RhythmRuntimeState requireActiveState(UUID playerId, String playerName) {
        validatePlayer(playerId, playerName);
        RhythmRuntimeState state = activeByPlayerId.get(playerId);
        if (state == null) {
            throw new IllegalStateException("No active gameplay session. Use /rhythm start first.");
        }
        return state;
    }

    private RhythmGameplaySnapshot snapshotAfterMutation(RhythmRuntimeState state, String event) {
        if (state.isFinished()) {
            RhythmGameplaySnapshot completedSnapshot = finalizeCompletedState(state, "chart_complete");
            logRhythmInfo("gameplay", event, fields(state, "state", completedSnapshot.summary()));
            return completedSnapshot;
        }

        RhythmGameplaySnapshot snapshot = state.snapshot();
        lastByPlayerId.put(state.playerId, snapshot);
        logRhythmDebug("gameplay", event, fields(state, "state", snapshot.summary()));
        return snapshot;
    }

    private RhythmGameplaySnapshot finalizeCompletedState(RhythmRuntimeState state, String reason) {
        activeByPlayerId.remove(state.playerId, state);
        state.markCompleted(reason);

        RhythmGameplaySnapshot snapshot = state.snapshot();
        lastByPlayerId.put(state.playerId, snapshot);
        logRhythmInfo(
            "gameplay",
            "gameplay_completed",
            fields(
                state,
                "finishReason", snapshot.finishReason(),
                "score", snapshot.score(),
                "accuracy", snapshot.accuracyPercent(),
                "maxCombo", snapshot.maxCombo()
            )
        );

        try {
            completeRhythmSession(state.playerId, state.playerName, reason);
        } catch (IllegalStateException exception) {
            logRhythmWarn(
                "session",
                "session_completion_sync_failed",
                fields(state, "reason", exception.getMessage())
            );
        }

        return snapshot;
    }

    private void logAutoJudgment(RhythmRuntimeState state, RhythmJudgmentResult previousJudgment) {
        if (Objects.equals(previousJudgment, state.lastJudgment) || state.lastJudgment == null) {
            return;
        }
        logRhythmInfo(
            "judgment",
            "lane_judged",
            fields(
                state,
                "action", state.lastJudgment.action() == null ? "auto" : state.lastJudgment.action(),
                "lane", state.lastJudgment.lane(),
                "noteId", state.lastJudgment.noteId(),
                "result", state.lastJudgment.type(),
                "deltaMs", state.lastJudgment.deltaMillis(),
                "comboAfter", state.lastJudgment.comboAfter(),
                "scoreAfter", state.lastJudgment.scoreAfter(),
                "accuracyAfter", state.lastJudgment.accuracyAfter(),
                "detail", state.lastJudgment.detail()
            )
        );
    }

    private static void validatePlayer(UUID playerId, String playerName) {
        Objects.requireNonNull(playerId, "playerId");
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("playerName");
        }
    }

    private static String requireText(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException(fieldName);
        }
        return rawValue.trim();
    }

    private static void validateLane(int lane, int keyMode) {
        if (lane < 1 || lane > keyMode) {
            throw new IllegalArgumentException("Lane must be between 1 and " + keyMode + ".");
        }
    }

    private static RhythmJudgmentType classifyHit(long deltaMillis) {
        long absoluteDelta = Math.abs(deltaMillis);
        if (absoluteDelta <= PERFECT_WINDOW_MS) {
            return RhythmJudgmentType.PERFECT;
        }
        if (absoluteDelta <= GREAT_WINDOW_MS) {
            return RhythmJudgmentType.GREAT;
        }
        if (absoluteDelta <= GOOD_WINDOW_MS) {
            return RhythmJudgmentType.GOOD;
        }
        return RhythmJudgmentType.BAD;
    }

    private static LinkedHashMap<String, Object> fields(RhythmRuntimeState state, Object... extraValues) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        fields.put("traceId", state.traceId);
        fields.put("sessionId", state.sessionId);
        fields.put("playerId", state.playerId);
        fields.put("player", state.playerName);
        fields.put("chartId", state.chart.chartId());
        fields.put("songTimeMs", state.rawSongTimeMillis);
        for (int index = 0; index + 1 < extraValues.length; index += 2) {
            fields.put(String.valueOf(extraValues[index]), extraValues[index + 1]);
        }
        return fields;
    }

    private static final class RhythmRuntimeState {
        private final String traceId;
        private final String sessionId;
        private final UUID playerId;
        private final String playerName;
        private final RhythmChart chart;
        private final RhythmPlayerSettings settings;
        private final Map<Integer, List<RhythmRuntimeNoteState>> notesByLane = new LinkedHashMap<>();
        private final LinkedHashSet<Integer> heldLanes = new LinkedHashSet<>();
        private final int totalObjectCount;

        private long rawSongTimeMillis;
        private int score;
        private int combo;
        private int maxCombo;
        private int hitCount;
        private int missCount;
        private int ghostTapCount;
        private int judgedObjectCount;
        private int judgedAccuracyMaxUnits;
        private int awardedAccuracyUnits;
        private boolean active = true;
        private boolean completed;
        private String finishReason = "";
        private RhythmJudgmentResult lastJudgment;

        private RhythmRuntimeState(
            String sessionId,
            UUID playerId,
            String playerName,
            RhythmChart chart,
            RhythmPlayerSettings settings
        ) {
            this.traceId = sessionId;
            this.sessionId = sessionId;
            this.playerId = playerId;
            this.playerName = playerName;
            this.chart = chart;
            this.settings = settings;
            this.totalObjectCount = chart.notes().size() + Math.toIntExact(chart.holdCount());

            for (int lane = 1; lane <= chart.keyMode(); lane++) {
                notesByLane.put(lane, new ArrayList<>());
            }
            for (RhythmNote note : chart.notes()) {
                notesByLane.get(note.lane()).add(new RhythmRuntimeNoteState(note));
            }
        }

        private RhythmGameplaySnapshot snapshot() {
            return new RhythmGameplaySnapshot(
                traceId,
                sessionId,
                playerId,
                playerName,
                chart.songId(),
                chart.chartId(),
                rawSongTimeMillis,
                score,
                combo,
                maxCombo,
                hitCount,
                missCount,
                ghostTapCount,
                judgedObjectCount,
                totalObjectCount,
                accuracyPercent(),
                Set.copyOf(heldLanes),
                lastJudgment == null ? null : lastJudgment.type(),
                lastJudgment == null ? null : lastJudgment.deltaMillis(),
                Math.max(0, totalObjectCount - judgedObjectCount),
                remainingNoteViews(),
                active,
                completed,
                finishReason
            );
        }

        private List<RhythmGameplayNoteView> remainingNoteViews() {
            List<RhythmGameplayNoteView> views = new ArrayList<>();
            for (List<RhythmRuntimeNoteState> laneNotes : notesByLane.values()) {
                for (RhythmRuntimeNoteState noteState : laneNotes) {
                    if (!noteState.isResolved()) {
                        views.add(noteState.view());
                    }
                }
            }
            return List.copyOf(views);
        }

        private boolean isLaneHeld(int lane) {
            return heldLanes.contains(lane);
        }

        private boolean isFinished() {
            return judgedObjectCount >= totalObjectCount;
        }

        private long effectiveSongTimeMillis() {
            return rawSongTimeMillis + settings.globalOffsetMs();
        }

        private void advanceTo(long nextSongTimeMillis) {
            if (nextSongTimeMillis < 0L) {
                throw new IllegalArgumentException("songTimeMillis must be zero or greater.");
            }
            if (nextSongTimeMillis < rawSongTimeMillis) {
                throw new IllegalArgumentException("songTimeMillis cannot move backwards.");
            }

            rawSongTimeMillis = nextSongTimeMillis;
            processPendingNotes();
        }

        private RhythmJudgmentResult handleDown(int lane) {
            heldLanes.add(lane);

            RhythmRuntimeNoteState candidate = nextCandidate(lane);
            if (candidate == null || candidate.awaitingTail()) {
                heldLanes.remove(lane);
                return judgeGhostTap(lane, 0L, RhythmLaneInputAction.DOWN, "no_candidate");
            }

            long deltaMillis = effectiveSongTimeMillis() - candidate.note.startTimeMillis();
            if (Math.abs(deltaMillis) > MISS_WINDOW_MS) {
                heldLanes.remove(lane);
                return judgeGhostTap(
                    lane,
                    deltaMillis,
                    RhythmLaneInputAction.DOWN,
                    deltaMillis < 0L ? "outside_window_early" : "outside_window_late"
                );
            }

            RhythmJudgmentResult judgment = judgeHead(candidate, classifyHit(deltaMillis), RhythmLaneInputAction.DOWN, deltaMillis, "head_hit");
            if (!candidate.note.hold() || !judgment.type().countsAsHit()) {
                heldLanes.remove(lane);
            }
            return judgment;
        }

        private RhythmJudgmentResult handleUp(int lane) {
            heldLanes.remove(lane);

            RhythmRuntimeNoteState holdState = activeHold(lane);
            if (holdState == null) {
                return null;
            }

            long deltaMillis = effectiveSongTimeMillis() - holdState.note.endTimeMillis();
            if (effectiveSongTimeMillis() >= holdState.note.endTimeMillis() - HOLD_RELEASE_WINDOW_MS) {
                return judgeTail(holdState, RhythmJudgmentType.HOLD_OK, RhythmLaneInputAction.UP, deltaMillis, "hold_release_ok");
            }
            return judgeTail(holdState, RhythmJudgmentType.EARLY_RELEASE, RhythmLaneInputAction.UP, deltaMillis, "hold_release_early");
        }

        private void processPendingNotes() {
            for (int lane = 1; lane <= chart.keyMode(); lane++) {
                while (true) {
                    RhythmRuntimeNoteState candidate = nextCandidate(lane);
                    if (candidate == null) {
                        break;
                    }
                    if (candidate.awaitingHead()) {
                        long deltaMillis = effectiveSongTimeMillis() - candidate.note.startTimeMillis();
                        if (deltaMillis > MISS_WINDOW_MS) {
                            judgeHead(candidate, RhythmJudgmentType.MISS, null, deltaMillis, "auto_miss");
                            continue;
                        }
                        break;
                    }
                    if (candidate.awaitingTail()) {
                        if (candidate.holdActive && effectiveSongTimeMillis() >= candidate.note.endTimeMillis()) {
                            judgeTail(
                                candidate,
                                RhythmJudgmentType.HOLD_OK,
                                null,
                                effectiveSongTimeMillis() - candidate.note.endTimeMillis(),
                                "hold_complete"
                            );
                            continue;
                        }
                        break;
                    }
                    break;
                }
            }
        }

        private RhythmRuntimeNoteState nextCandidate(int lane) {
            List<RhythmRuntimeNoteState> laneNotes = notesByLane.get(lane);
            if (laneNotes == null) {
                return null;
            }
            for (RhythmRuntimeNoteState noteState : laneNotes) {
                if (!noteState.isResolved()) {
                    return noteState;
                }
            }
            return null;
        }

        private RhythmRuntimeNoteState activeHold(int lane) {
            List<RhythmRuntimeNoteState> laneNotes = notesByLane.get(lane);
            if (laneNotes == null) {
                return null;
            }
            for (RhythmRuntimeNoteState noteState : laneNotes) {
                if (noteState.awaitingTail() && noteState.holdActive) {
                    return noteState;
                }
            }
            return null;
        }

        private String describeLaneCandidate(int lane) {
            RhythmRuntimeNoteState candidate = nextCandidate(lane);
            if (candidate == null) {
                return "none";
            }

            long effectiveNow = effectiveSongTimeMillis();
            if (candidate.awaitingHead()) {
                long deltaMillis = effectiveNow - candidate.note.startTimeMillis();
                return candidate.note.noteId()
                    + ":head"
                    + ":delta=" + deltaMillis + "ms"
                    + ":start=" + candidate.note.startTimeMillis() + "ms";
            }

            long deltaMillis = effectiveNow - candidate.note.endTimeMillis();
            return candidate.note.noteId()
                + ":tail"
                + ":delta=" + deltaMillis + "ms"
                + ":end=" + candidate.note.endTimeMillis() + "ms"
                + ":holdActive=" + candidate.holdActive;
        }

        private String describeInputOutcome(
            RhythmLaneInputAction action,
            int lane,
            boolean heldBefore,
            RhythmJudgmentResult judgment,
            String candidateBefore
        ) {
            String candidateAfter = describeLaneCandidate(lane);
            if (judgment != null) {
                return judgment.summary()
                    + " detail=" + judgment.detail()
                    + " heldBefore=" + heldBefore
                    + " heldAfter=" + isLaneHeld(lane)
                    + " candidateBefore=" + candidateBefore
                    + " candidateAfter=" + candidateAfter;
            }

            return action.name().toLowerCase()
                + " lane=" + lane
                + " result=NO_JUDGMENT"
                + " heldBefore=" + heldBefore
                + " heldAfter=" + isLaneHeld(lane)
                + " candidateBefore=" + candidateBefore
                + " candidateAfter=" + candidateAfter;
        }

        private RhythmJudgmentResult judgeGhostTap(
            int lane,
            long deltaMillis,
            RhythmLaneInputAction action,
            String detail
        ) {
            int comboBefore = combo;
            int scoreBefore = score;
            double accuracyBefore = accuracyPercent();

            combo = 0;
            ghostTapCount++;

            RhythmJudgmentResult result = new RhythmJudgmentResult(
                traceId,
                sessionId,
                chart.chartId(),
                null,
                lane,
                action,
                RhythmJudgmentType.GHOST_TAP,
                rawSongTimeMillis,
                deltaMillis,
                comboBefore,
                combo,
                scoreBefore,
                score,
                accuracyBefore,
                accuracyPercent(),
                detail
            );
            lastJudgment = result;
            return result;
        }

        private RhythmJudgmentResult judgeHead(
            RhythmRuntimeNoteState noteState,
            RhythmJudgmentType type,
            RhythmLaneInputAction action,
            long deltaMillis,
            String detail
        ) {
            int comboBefore = combo;
            int scoreBefore = score;
            double accuracyBefore = accuracyPercent();

            noteState.headResolved = true;
            applyScorableJudgment(type);

            if (noteState.note.hold()) {
                if (type.countsAsHit()) {
                    noteState.holdActive = true;
                } else {
                    noteState.tailResolved = true;
                    applyScorableJudgment(RhythmJudgmentType.MISS);
                }
            }

            RhythmJudgmentResult result = new RhythmJudgmentResult(
                traceId,
                sessionId,
                chart.chartId(),
                noteState.note.noteId(),
                noteState.note.lane(),
                action,
                type,
                rawSongTimeMillis,
                deltaMillis,
                comboBefore,
                combo,
                scoreBefore,
                score,
                accuracyBefore,
                accuracyPercent(),
                detail
            );
            lastJudgment = result;
            return result;
        }

        private RhythmJudgmentResult judgeTail(
            RhythmRuntimeNoteState noteState,
            RhythmJudgmentType type,
            RhythmLaneInputAction action,
            long deltaMillis,
            String detail
        ) {
            int comboBefore = combo;
            int scoreBefore = score;
            double accuracyBefore = accuracyPercent();

            heldLanes.remove(noteState.note.lane());
            noteState.holdActive = false;
            noteState.tailResolved = true;
            applyScorableJudgment(type);

            RhythmJudgmentResult result = new RhythmJudgmentResult(
                traceId,
                sessionId,
                chart.chartId(),
                noteState.note.noteId(),
                noteState.note.lane(),
                action,
                type,
                rawSongTimeMillis,
                deltaMillis,
                comboBefore,
                combo,
                scoreBefore,
                score,
                accuracyBefore,
                accuracyPercent(),
                detail
            );
            lastJudgment = result;
            return result;
        }

        private void applyScorableJudgment(RhythmJudgmentType type) {
            judgedObjectCount++;
            if (type.countsTowardAccuracy()) {
                judgedAccuracyMaxUnits += ACCURACY_UNIT_MAX;
                awardedAccuracyUnits += type.accuracyUnits();
            }
            score += type.scoreValue();

            if (type.countsAsHit()) {
                combo++;
                maxCombo = Math.max(maxCombo, combo);
                hitCount++;
                return;
            }

            combo = 0;
            missCount++;
        }

        private double accuracyPercent() {
            if (judgedAccuracyMaxUnits <= 0) {
                return 100.0d;
            }
            return (double) awardedAccuracyUnits * 100.0d / (double) judgedAccuracyMaxUnits;
        }

        private void markCompleted(String reason) {
            active = false;
            completed = true;
            finishReason = reason == null || reason.isBlank() ? "completed" : reason;
            heldLanes.clear();
        }
    }

    private static final class RhythmRuntimeNoteState {
        private final RhythmNote note;
        private boolean headResolved;
        private boolean tailResolved;
        private boolean holdActive;

        private RhythmRuntimeNoteState(RhythmNote note) {
            this.note = note;
            this.tailResolved = !note.hold();
        }

        private boolean awaitingHead() {
            return !headResolved;
        }

        private boolean awaitingTail() {
            return note.hold() && headResolved && !tailResolved;
        }

        private boolean isResolved() {
            return headResolved && tailResolved;
        }

        private RhythmGameplayNoteView view() {
            return new RhythmGameplayNoteView(
                note.noteId(),
                note.lane(),
                note.startTimeMillis(),
                note.endTimeMillis(),
                note.hold(),
                headResolved,
                tailResolved,
                holdActive
            );
        }
    }
}
