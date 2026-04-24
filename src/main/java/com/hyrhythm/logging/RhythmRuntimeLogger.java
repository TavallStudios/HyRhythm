package com.hyrhythm.logging;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RhythmRuntimeLogger implements RhythmLoggingService {
    private static final Logger FALLBACK_LOGGER = Logger.getLogger(RhythmRuntimeLogger.class.getName());

    private final HyRhythmPlugin plugin;
    private final RhythmDebugState debugState;

    public RhythmRuntimeLogger(HyRhythmPlugin plugin, RhythmDebugState debugState) {
        this.plugin = plugin;
        this.debugState = debugState;
    }

    @Override
    public void info(String phase, String event, Map<String, ?> fields) {
        emit(Level.INFO, phase, event, fields, null, false);
    }

    @Override
    public void debug(String phase, String event, Map<String, ?> fields) {
        emit(Level.FINE, phase, event, fields, null, true);
    }

    @Override
    public void warn(String phase, String event, Map<String, ?> fields) {
        emit(Level.WARNING, phase, event, fields, null, false);
    }

    @Override
    public void error(String phase, String event, Map<String, ?> fields, Throwable throwable) {
        emit(Level.SEVERE, phase, event, fields, throwable, false);
    }

    private void emit(
        Level level,
        String phase,
        String event,
        Map<String, ?> fields,
        Throwable throwable,
        boolean debugOnly
    ) {
        if (debugOnly && (debugState == null || !debugState.isDebugEnabled())) {
            return;
        }

        String line = buildLine(level, phase, event, fields);
        if (plugin == null) {
            logFallback(level, line, throwable);
            return;
        }

        if (Level.SEVERE.equals(level)) {
            if (throwable == null) {
                plugin.getLogger().atSevere().log(line);
            } else {
                plugin.getLogger().atSevere().withCause(throwable).log(line);
            }
            return;
        }

        if (Level.WARNING.equals(level)) {
            plugin.getLogger().atWarning().log(line);
            return;
        }

        if (Level.FINE.equals(level)) {
            plugin.getLogger().at(Level.FINE).log(line);
            return;
        }

        plugin.getLogger().atInfo().log(line);
    }

    private void logFallback(Level level, String line, Throwable throwable) {
        if (throwable == null) {
            FALLBACK_LOGGER.log(level, line);
            return;
        }
        FALLBACK_LOGGER.log(level, line, throwable);
    }

    private static String buildLine(Level level, String phase, String event, Map<String, ?> fields) {
        String normalizedPhase = phase == null || phase.isBlank() ? "general" : phase;
        String normalizedEvent = event == null || event.isBlank() ? "message" : event;
        Map<String, ?> safeFields = fields == null ? Map.of() : new TreeMap<>(fields);
        StringBuilder builder = new StringBuilder()
            .append("[HyRhythm]")
            .append(" [").append(level.getName()).append(']')
            .append(" [").append(normalizedPhase).append(']')
            .append(' ')
            .append(normalizedEvent);

        if (!safeFields.isEmpty()) {
            builder.append(' ');
            boolean first = true;
            for (Map.Entry<String, ?> entry : safeFields.entrySet()) {
                if (!first) {
                    builder.append(' ');
                }
                builder.append(entry.getKey()).append('=').append(String.valueOf(entry.getValue()));
                first = false;
            }
        }
        return builder.toString();
    }
}
