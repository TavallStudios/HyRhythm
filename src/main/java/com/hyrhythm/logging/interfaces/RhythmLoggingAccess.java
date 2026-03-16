package com.hyrhythm.logging.interfaces;

import com.hyrhythm.dependency.CoreDependencyAccess;
import com.hyrhythm.dependency.DependencyLoaderAccess;

import java.util.Map;

public interface RhythmLoggingAccess extends CoreDependencyAccess {
    default RhythmLoggingService getRhythmLoggingService() {
        return DependencyLoaderAccess.findInstance(RhythmLoggingService.class);
    }

    default void logRhythmInfo(String phase, String event, Map<String, ?> fields) {
        RhythmLoggingService loggingService = getRhythmLoggingService();
        if (loggingService != null) {
            loggingService.info(phase, event, fields);
        }
    }

    default void logRhythmDebug(String phase, String event, Map<String, ?> fields) {
        RhythmLoggingService loggingService = getRhythmLoggingService();
        if (loggingService != null) {
            loggingService.debug(phase, event, fields);
        }
    }

    default void logRhythmWarn(String phase, String event, Map<String, ?> fields) {
        RhythmLoggingService loggingService = getRhythmLoggingService();
        if (loggingService != null) {
            loggingService.warn(phase, event, fields);
        }
    }

    default void logRhythmError(String phase, String event, Map<String, ?> fields, Throwable throwable) {
        RhythmLoggingService loggingService = getRhythmLoggingService();
        if (loggingService != null) {
            loggingService.error(phase, event, fields, throwable);
        }
    }
}
