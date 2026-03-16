package com.hyrhythm.settings.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public record RhythmKeybinds(
    RhythmInputChannel lane1Input,
    RhythmInputChannel lane2Input,
    RhythmInputChannel lane3Input,
    RhythmInputChannel lane4Input
) {
    public RhythmKeybinds {
        lane1Input = Objects.requireNonNull(lane1Input, "lane1Input");
        lane2Input = Objects.requireNonNull(lane2Input, "lane2Input");
        lane3Input = Objects.requireNonNull(lane3Input, "lane3Input");
        lane4Input = Objects.requireNonNull(lane4Input, "lane4Input");

        if (new LinkedHashSet<>(List.of(lane1Input, lane2Input, lane3Input, lane4Input)).size() != 4) {
            throw new IllegalArgumentException("Keybinds must be unique.");
        }
    }

    public static RhythmKeybinds defaults() {
        return new RhythmKeybinds(
            RhythmInputChannel.defaultForLane(1),
            RhythmInputChannel.defaultForLane(2),
            RhythmInputChannel.defaultForLane(3),
            RhythmInputChannel.defaultForLane(4)
        );
    }

    public static RhythmKeybinds fromStoredValues(String lane1Value, String lane2Value, String lane3Value, String lane4Value) {
        try {
            return new RhythmKeybinds(
                parseStoredValue(lane1Value, 1),
                parseStoredValue(lane2Value, 2),
                parseStoredValue(lane3Value, 3),
                parseStoredValue(lane4Value, 4)
            );
        } catch (IllegalArgumentException exception) {
            return defaults();
        }
    }

    public RhythmInputChannel inputForLane(int lane) {
        return switch (lane) {
            case 1 -> lane1Input;
            case 2 -> lane2Input;
            case 3 -> lane3Input;
            case 4 -> lane4Input;
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    public int laneForInteraction(RhythmInputChannel inputChannel) {
        Objects.requireNonNull(inputChannel, "inputChannel");
        if (lane1Input == inputChannel) {
            return 1;
        }
        if (lane2Input == inputChannel) {
            return 2;
        }
        if (lane3Input == inputChannel) {
            return 3;
        }
        if (lane4Input == inputChannel) {
            return 4;
        }
        return 0;
    }

    public RhythmKeybinds withLaneBinding(int lane, String rawInputChannel) {
        return withLaneBinding(lane, RhythmInputChannel.parse(rawInputChannel));
    }

    public RhythmKeybinds withLaneBinding(int lane, RhythmInputChannel inputChannel) {
        RhythmInputChannel currentBinding = inputForLane(lane);
        RhythmInputChannel updatedLane1 = swapBinding(lane1Input, currentBinding, inputChannel);
        RhythmInputChannel updatedLane2 = swapBinding(lane2Input, currentBinding, inputChannel);
        RhythmInputChannel updatedLane3 = swapBinding(lane3Input, currentBinding, inputChannel);
        RhythmInputChannel updatedLane4 = swapBinding(lane4Input, currentBinding, inputChannel);
        return switch (lane) {
            case 1 -> new RhythmKeybinds(inputChannel, updatedLane2, updatedLane3, updatedLane4);
            case 2 -> new RhythmKeybinds(updatedLane1, inputChannel, updatedLane3, updatedLane4);
            case 3 -> new RhythmKeybinds(updatedLane1, updatedLane2, inputChannel, updatedLane4);
            case 4 -> new RhythmKeybinds(updatedLane1, updatedLane2, updatedLane3, inputChannel);
            default -> throw new IllegalArgumentException("Lane must be between 1 and 4.");
        };
    }

    public String toDisplayString() {
        return "1=" + lane1Input.displayName()
            + " 2=" + lane2Input.displayName()
            + " 3=" + lane3Input.displayName()
            + " 4=" + lane4Input.displayName();
    }

    private static RhythmInputChannel parseStoredValue(String rawValue, int lane) {
        if (rawValue == null || rawValue.isBlank()) {
            return RhythmInputChannel.defaultForLane(lane);
        }
        try {
            return RhythmInputChannel.parse(rawValue);
        } catch (IllegalArgumentException exception) {
            return RhythmInputChannel.defaultForLane(lane);
        }
    }

    private static RhythmInputChannel swapBinding(
        RhythmInputChannel existingBinding,
        RhythmInputChannel currentBinding,
        RhythmInputChannel requestedBinding
    ) {
        if (existingBinding == requestedBinding) {
            return currentBinding;
        }
        return existingBinding;
    }
}
