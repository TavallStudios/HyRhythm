package com.hyrhythm.dependency;

import com.hyrhythm.HyRhythmPlugin;
import com.hyrhythm.debug.RhythmDebugState;
import com.hyrhythm.logging.RhythmRuntimeLogger;

public interface CoreDependencyAccess {
    default HyRhythmPlugin getHyRhythmPlugin() {
        return DependencyLoaderAccess.findInstance(HyRhythmPlugin.class);
    }

    default RhythmDebugState getRhythmDebugState() {
        return DependencyLoaderAccess.findInstance(RhythmDebugState.class);
    }

    default RhythmRuntimeLogger getRhythmRuntimeLogger() {
        return DependencyLoaderAccess.findInstance(RhythmRuntimeLogger.class);
    }
}
