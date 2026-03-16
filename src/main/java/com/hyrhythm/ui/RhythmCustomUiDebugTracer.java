package com.hyrhythm.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBinding;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RhythmCustomUiDebugTracer {
    private RhythmCustomUiDebugTracer() {
    }

    static LinkedHashMap<String, Object> extend(Map<String, ?> baseFields, Object... extraValues) {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        if (baseFields != null) {
            fields.putAll(baseFields);
        }
        for (int index = 0; index + 1 < extraValues.length; index += 2) {
            fields.put(String.valueOf(extraValues[index]), extraValues[index + 1]);
        }
        return fields;
    }

    static void tracePayload(
        com.hyrhythm.logging.interfaces.RhythmLoggingService loggingService,
        String event,
        String pageKey,
        Map<String, ?> baseFields,
        UICommandBuilder uiCommandBuilder,
        UIEventBuilder uiEventBuilder
    ) {
        if (loggingService == null || uiCommandBuilder == null || uiEventBuilder == null) {
            return;
        }
        CustomUICommand[] commands = uiCommandBuilder.getCommands();
        CustomUIEventBinding[] eventBindings = uiEventBuilder.getEvents();
        loggingService.debug(
            "ui",
            event,
            extend(
                baseFields,
                "page", pageKey,
                "commandCount", commands.length,
                "eventCount", eventBindings.length,
                "commands", summarizeCommands(commands),
                "events", summarizeEvents(eventBindings)
            )
        );
    }

    private static List<String> summarizeCommands(CustomUICommand[] commands) {
        List<String> summary = new ArrayList<>(commands.length);
        for (CustomUICommand command : commands) {
            if (command == null) {
                continue;
            }
            StringBuilder builder = new StringBuilder(command.type.name());
            if (command.selector != null) {
                builder.append(' ').append(command.selector);
            }
            if (command.text != null && !command.text.isBlank()) {
                builder.append(" text=").append(command.text);
            }
            summary.add(builder.toString());
        }
        return List.copyOf(summary);
    }

    private static List<String> summarizeEvents(CustomUIEventBinding[] eventBindings) {
        List<String> summary = new ArrayList<>(eventBindings.length);
        for (CustomUIEventBinding eventBinding : eventBindings) {
            if (eventBinding == null) {
                continue;
            }
            StringBuilder builder = new StringBuilder(eventBinding.type.name());
            if (eventBinding.selector != null) {
                builder.append(' ').append(eventBinding.selector);
            }
            if (eventBinding.data != null && !eventBinding.data.isBlank()) {
                builder.append(" data=").append(eventBinding.data);
            }
            summary.add(builder.toString());
        }
        return List.copyOf(summary);
    }
}
