package com.hyrhythm.settings.model;

import com.hypixel.hytale.protocol.InteractionType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public enum RhythmInputChannel {
    ABILITY1(InteractionType.Ability1, "Ability1"),
    ABILITY2(InteractionType.Ability2, "Ability2"),
    ABILITY3(InteractionType.Ability3, "Ability3"),
    USE(InteractionType.Use, "Use");

    private final InteractionType interactionType;
    private final String displayName;

    RhythmInputChannel(InteractionType interactionType, String displayName) {
        this.interactionType = Objects.requireNonNull(interactionType, "interactionType");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public static RhythmInputChannel defaultForLane(int lane) {
        return switch (lane) {
            case 1 -> ABILITY1;
            case 2 -> ABILITY2;
            case 3 -> ABILITY3;
            case 4 -> USE;
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    public static RhythmInputChannel parse(String rawValue) {
        String normalized = normalize(rawValue);
        for (RhythmInputChannel channel : values()) {
            if (channel.name().equals(normalized) || normalize(channel.displayName).equals(normalized)) {
                return channel;
            }
        }
        throw new IllegalArgumentException(
            "Unsupported input binding '" + rawValue + "'. Supported bindings: " + supportedBindingsDisplay() + "."
        );
    }

    public static Optional<RhythmInputChannel> fromInteractionType(InteractionType interactionType) {
        if (interactionType == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
            .filter(channel -> channel.interactionType == interactionType)
            .findFirst();
    }

    public InteractionType interactionType() {
        return interactionType;
    }

    public String displayName() {
        return displayName;
    }

    public static String supportedBindingsDisplay() {
        return Arrays.stream(values())
            .map(RhythmInputChannel::displayName)
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }

    private static String normalize(String rawValue) {
        String normalized = Objects.requireNonNull(rawValue, "rawValue").trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Input binding cannot be blank.");
        }
        return normalized.replace("-", "").replace("_", "").replace(" ", "");
    }
}
