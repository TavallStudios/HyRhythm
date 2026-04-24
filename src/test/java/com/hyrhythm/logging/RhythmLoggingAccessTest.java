package com.hyrhythm.logging;

import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.debug.interfaces.RhythmDebugAccess;
import com.hyrhythm.dependency.DependencyLoader;
import com.hyrhythm.logging.interfaces.RhythmLoggingAccess;
import com.hyrhythm.logging.interfaces.RhythmLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RhythmLoggingAccessTest {
    private RecordingLoggingService recordingLoggingService;

    @BeforeEach
    void setUp() {
        DependencyLoader.getFallbackDependencyLoader().resetInstances();
        recordingLoggingService = null;
    }

    @Test
    void defaultMethodsDelegateToDiManagedLoggingService() {
        RhythmDebugState debugState = new RhythmDebugState();
        recordingLoggingService = new RecordingLoggingService(debugState);

        DependencyLoader.getFallbackDependencyLoader().registerImportantInstance(RhythmDebugState.class, debugState);
        DependencyLoader.getFallbackDependencyLoader()
            .registerImportantInstance(RhythmLoggingService.class, recordingLoggingService);

        LoggingClient client = new LoggingClient();
        client.logRhythmInfo("bootstrap", "service_loader_ready", Map.of("registryCount", 1));

        assertEquals(1, recordingLoggingService.messages.size());
        assertTrue(recordingLoggingService.messages.getFirst().contains("[bootstrap] service_loader_ready"));
        assertTrue(recordingLoggingService.messages.getFirst().contains("registryCount=1"));
    }

    @Test
    void debugLogsOnlyEmitWhenDebugModeIsEnabled() {
        RhythmDebugState debugState = new RhythmDebugState();
        recordingLoggingService = new RecordingLoggingService(debugState);

        DependencyLoader.getFallbackDependencyLoader().registerImportantInstance(RhythmDebugState.class, debugState);
        DependencyLoader.getFallbackDependencyLoader()
            .registerImportantInstance(RhythmLoggingService.class, recordingLoggingService);

        LoggingClient client = new LoggingClient();
        client.logRhythmDebug("command", "ignored_debug", Map.of("subcommand", "state"));
        client.setRhythmDebugEnabled(true);
        client.logRhythmDebug("command", "emitted_debug", Map.of("subcommand", "state"));

        assertEquals(1, recordingLoggingService.messages.size());
        assertTrue(recordingLoggingService.messages.getFirst().contains("emitted_debug"));
    }

    private static final class LoggingClient implements RhythmLoggingAccess, RhythmDebugAccess {
    }

    private static final class RecordingLoggingService implements RhythmLoggingService {
        private final RhythmDebugState debugState;
        private final List<String> messages = new ArrayList<>();

        private RecordingLoggingService(RhythmDebugState debugState) {
            this.debugState = debugState;
        }

        @Override
        public void info(String phase, String event, Map<String, ?> fields) {
            messages.add(format(phase, event, fields));
        }

        @Override
        public void debug(String phase, String event, Map<String, ?> fields) {
            if (debugState.isDebugEnabled()) {
                messages.add(format(phase, event, fields));
            }
        }

        @Override
        public void warn(String phase, String event, Map<String, ?> fields) {
            messages.add(format(phase, event, fields));
        }

        @Override
        public void error(String phase, String event, Map<String, ?> fields, Throwable throwable) {
            messages.add(format(phase, event, fields));
        }

        private static String format(String phase, String event, Map<String, ?> fields) {
            return "[" + phase + "] " + event + " " + fields;
        }
    }
}
