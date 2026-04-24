package com.hyrhythm.logging.interfaces;

import java.util.Map;

public interface RhythmLoggingService {
    void info(String phase, String event, Map<String, ?> fields);

    void debug(String phase, String event, Map<String, ?> fields);

    void warn(String phase, String event, Map<String, ?> fields);

    void error(String phase, String event, Map<String, ?> fields, Throwable throwable);
}
