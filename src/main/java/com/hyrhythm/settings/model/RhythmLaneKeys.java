package com.hyrhythm.settings.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record RhythmLaneKeys(
    String lane1Key,
    String lane2Key,
    String lane3Key,
    String lane4Key
) {
    public RhythmLaneKeys {
        lane1Key = normalize(lane1Key);
        lane2Key = normalize(lane2Key);
        lane3Key = normalize(lane3Key);
        lane4Key = normalize(lane4Key);

        if (new LinkedHashSet<>(List.of(lane1Key, lane2Key, lane3Key, lane4Key)).size() != 4) {
            throw new IllegalArgumentException("Lane keys must be unique.");
        }
    }

    public static RhythmLaneKeys defaults() {
        return new RhythmLaneKeys(defaultKeyForLane(1), defaultKeyForLane(2), defaultKeyForLane(3), defaultKeyForLane(4));
    }

    public static RhythmLaneKeys fromStoredValues(String lane1Value, String lane2Value, String lane3Value, String lane4Value) {
        try {
            return new RhythmLaneKeys(
                parseStoredValue(lane1Value, 1),
                parseStoredValue(lane2Value, 2),
                parseStoredValue(lane3Value, 3),
                parseStoredValue(lane4Value, 4)
            );
        } catch (IllegalArgumentException exception) {
            return defaults();
        }
    }

    public String keyForLane(int lane) {
        return switch (lane) {
            case 1 -> lane1Key;
            case 2 -> lane2Key;
            case 3 -> lane3Key;
            case 4 -> lane4Key;
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    public RhythmLaneKeys withLaneKey(int lane, String rawKey) {
        String requestedKey = normalize(rawKey);
        String currentKey = keyForLane(lane);
        String updatedLane1 = swapKey(lane1Key, currentKey, requestedKey);
        String updatedLane2 = swapKey(lane2Key, currentKey, requestedKey);
        String updatedLane3 = swapKey(lane3Key, currentKey, requestedKey);
        String updatedLane4 = swapKey(lane4Key, currentKey, requestedKey);
        return switch (lane) {
            case 1 -> new RhythmLaneKeys(requestedKey, updatedLane2, updatedLane3, updatedLane4);
            case 2 -> new RhythmLaneKeys(updatedLane1, requestedKey, updatedLane3, updatedLane4);
            case 3 -> new RhythmLaneKeys(updatedLane1, updatedLane2, requestedKey, updatedLane4);
            case 4 -> new RhythmLaneKeys(updatedLane1, updatedLane2, updatedLane3, requestedKey);
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    public int laneForKey(String rawKey) {
        String normalizedKey = normalize(rawKey);
        if (lane1Key.equals(normalizedKey)) {
            return 1;
        }
        if (lane2Key.equals(normalizedKey)) {
            return 2;
        }
        if (lane3Key.equals(normalizedKey)) {
            return 3;
        }
        if (lane4Key.equals(normalizedKey)) {
            return 4;
        }
        return 0;
    }

    public String toDisplayString() {
        return "1=" + lane1Key
            + " 2=" + lane2Key
            + " 3=" + lane3Key
            + " 4=" + lane4Key;
    }

    public static String normalize(String rawKey) {
        Objects.requireNonNull(rawKey, "rawKey");
        String trimmed = rawKey.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Lane key cannot be blank.");
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private static String parseStoredValue(String rawValue, int lane) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultKeyForLane(lane);
        }
        try {
            return normalize(rawValue);
        } catch (IllegalArgumentException exception) {
            return defaultKeyForLane(lane);
        }
    }

    private static String defaultKeyForLane(int lane) {
        return switch (lane) {
            case 1 -> "D";
            case 2 -> "F";
            case 3 -> "J";
            case 4 -> "K";
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    private static String swapKey(String existingKey, String currentKey, String requestedKey) {
        if (existingKey.equals(requestedKey)) {
            return currentKey;
        }
        return existingKey;
    }
}
