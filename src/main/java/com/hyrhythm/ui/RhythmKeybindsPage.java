package com.hyrhythm.ui;

import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import com.hyrhythm.settings.interfaces.RhythmSettingsService;
import com.hyrhythm.settings.model.RhythmLaneKeys;
import com.hyrhythm.settings.model.RhythmPlayerSettings;
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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class RhythmKeybindsPage extends InteractiveCustomUIPage<RhythmKeybindsPage.KeybindsEventData> {
    private static final String PAGE_DOCUMENT = "Pages/RhythmKeybindsPage.ui";
    private static final String CAPTURE_DOCUMENT = "Pages/RhythmKeybindCaptureField.ui";
    private static final String CLOSE_BUTTON_SELECTOR = "#ExitKeybindsButton";

    private final UUID playerId;
    private final String playerName;
    private final RhythmSettingsService settingsService;
    private final RhythmLoggingService loggingService;

    private RhythmLaneKeys laneKeys;
    private int awaitingLane;

    public RhythmKeybindsPage(
        PlayerRef playerRef,
        UUID playerId,
        String playerName,
        RhythmPlayerSettings settings,
        RhythmSettingsService settingsService,
        RhythmLoggingService loggingService
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, KeybindsEventData.CODEC);
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.playerName = Objects.requireNonNull(playerName, "playerName");
        this.settingsService = Objects.requireNonNull(settingsService, "settingsService");
        this.loggingService = Objects.requireNonNull(loggingService, "loggingService");
        this.laneKeys = Objects.requireNonNull(settings, "settings").laneKeys();
    }

    @Override
    public void build(
        Ref<EntityStore> entityRef,
        UICommandBuilder uiCommandBuilder,
        UIEventBuilder uiEventBuilder,
        Store<EntityStore> entityStore
    ) {
        uiCommandBuilder.append(PAGE_DOCUMENT);
        applyState(uiCommandBuilder);
        bindActions(uiEventBuilder);
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder);
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "keybinds_ui_payload_built",
            getClass().getName(),
            baseFields(),
            uiCommandBuilder,
            uiEventBuilder
        );
        loggingService.info("ui", "keybinds_ui_built", baseFields());
    }

    @Override
    public void handleDataEvent(Ref<EntityStore> entityRef, Store<EntityStore> entityStore, KeybindsEventData eventData) {
        if (eventData == null || eventData.action == null || eventData.action.isBlank()) {
            return;
        }

        switch (eventData.action.trim().toUpperCase(Locale.ROOT)) {
            case "AWAIT" -> {
                awaitingLane = parseLane(eventData.lane);
                loggingService.info("ui", "keybind_lane_awaiting", extend(baseFields(), "lane", awaitingLane));
                pushStateUpdate();
            }
            case "CAPTURE" -> captureLaneKey(entityStore, eventData.captureValue);
            case "RESET" -> {
                RhythmPlayerSettings updatedSettings = settingsService.resetLaneKeys(playerId, playerName);
                laneKeys = updatedSettings.laneKeys();
                awaitingLane = 0;
                loggingService.info("ui", "keybinds_reset_from_ui", baseFields());
                sendPlayerMessage(entityStore, "Lane keys reset to " + laneKeys.toDisplayString() + ".", "green");
                pushStateUpdate();
            }
            case "CLOSE" -> {
                loggingService.info("ui", "keybinds_ui_close_requested", baseFields());
                close();
            }
            default -> loggingService.debug(
                "ui",
                "keybinds_ui_event_ignored",
                extend(baseFields(), "action", eventData.action)
            );
        }
    }

    @Override
    public void onDismiss(Ref<EntityStore> entityRef, Store<EntityStore> entityStore) {
        loggingService.info("ui", "keybinds_ui_closed", baseFields());
    }

    private void captureLaneKey(Store<EntityStore> entityStore, String rawValue) {
        if (awaitingLane == 0) {
            return;
        }

        String normalizedKey = normalizeCapturedKey(rawValue);
        if (normalizedKey == null) {
            return;
        }

        int savedLane = awaitingLane;
        RhythmPlayerSettings updatedSettings = settingsService.updateLaneKey(playerId, playerName, savedLane, normalizedKey);
        laneKeys = updatedSettings.laneKeys();
        awaitingLane = 0;
        loggingService.info(
            "ui",
            "lane_key_captured",
            extend(baseFields(), "lane", savedLane, "key", laneKeys.keyForLane(savedLane))
        );
        sendPlayerMessage(entityStore, "Lane " + savedLane + " is now bound to " + laneKeys.keyForLane(savedLane) + ".", "green");
        pushStateUpdate();
    }

    private void applyState(UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.set("#Lane1Value.Text", "Lane 1: " + laneKeys.lane1Key());
        uiCommandBuilder.set("#Lane2Value.Text", "Lane 2: " + laneKeys.lane2Key());
        uiCommandBuilder.set("#Lane3Value.Text", "Lane 3: " + laneKeys.lane3Key());
        uiCommandBuilder.set("#Lane4Value.Text", "Lane 4: " + laneKeys.lane4Key());
        uiCommandBuilder.set(
            "#AwaitingValue.Text",
            awaitingLane == 0 ? "Awaiting input: none" : "Awaiting input: Lane " + awaitingLane
        );
        uiCommandBuilder.set(
            "#CaptureStatus.Text",
            awaitingLane == 0
                ? "Click a Change button, then type a single key in the capture field."
                : "Press a key now to rebind Lane " + awaitingLane + "."
        );
        uiCommandBuilder.clear("#CaptureFieldHost");
        uiCommandBuilder.append("#CaptureFieldHost", CAPTURE_DOCUMENT);
        uiCommandBuilder.set(captureInputSelector() + ".Value", "");
    }

    private void bindActions(UIEventBuilder uiEventBuilder) {
        for (int lane = 1; lane <= 4; lane++) {
            uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ChangeLane" + lane + "Button",
                new EventData().append("Action", "Await").append("Lane", Integer.toString(lane)),
                false
            );
        }
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            captureInputSelector(),
            new EventData().append("Action", "Capture").append("@CaptureValue", captureInputSelector() + ".Value"),
            false
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#ResetKeybindsButton",
            EventData.of("Action", "Reset"),
            false
        );
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            CLOSE_BUTTON_SELECTOR,
            EventData.of("Action", "Close"),
            false
        );
    }

    private void pushStateUpdate() {
        if (playerRef == null) {
            return;
        }
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        applyState(uiCommandBuilder);
        bindActions(uiEventBuilder);
        RhythmCustomUiCommandValidator.validate(uiCommandBuilder, uiEventBuilder);
        RhythmCustomUiDebugTracer.tracePayload(
            loggingService,
            "keybinds_ui_payload_updated",
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

    private LinkedHashMap<String, Object> baseFields() {
        return extend(
            new LinkedHashMap<>(),
            "playerId", playerId,
            "player", playerName,
            "laneKeys", laneKeys.toDisplayString(),
            "awaitingLane", awaitingLane == 0 ? "none" : awaitingLane
        );
    }

    private static LinkedHashMap<String, Object> extend(LinkedHashMap<String, Object> fields, Object... extraValues) {
        for (int index = 0; index + 1 < extraValues.length; index += 2) {
            fields.put(String.valueOf(extraValues[index]), extraValues[index + 1]);
        }
        return fields;
    }

    private static int parseLane(String rawLane) {
        if (rawLane == null || rawLane.isBlank()) {
            throw new IllegalArgumentException("Lane must be provided.");
        }
        int lane = Integer.parseInt(rawLane.trim());
        if (lane < 1 || lane > 4) {
            throw new IllegalArgumentException("Lane must be between 1 and 4.");
        }
        return lane;
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

    private static String captureInputSelector() {
        return "#CaptureFieldHost[0] #CaptureInput";
    }

    public static final class KeybindsEventData {
        public static final BuilderCodec<KeybindsEventData> CODEC = BuilderCodec.builder(
            KeybindsEventData.class,
            KeybindsEventData::new
        )
            .append(new KeyedCodec<>("Action", Codec.STRING), (value, field) -> value.action = field, value -> value.action)
            .add()
            .append(new KeyedCodec<>("Lane", Codec.STRING), (value, field) -> value.lane = field, value -> value.lane)
            .add()
            .append(
                new KeyedCodec<>("@CaptureValue", Codec.STRING),
                (value, field) -> value.captureValue = field,
                value -> value.captureValue
            )
            .add()
            .build();

        private String action;
        private String lane;
        private String captureValue;

        public KeybindsEventData() {
        }
    }
}
